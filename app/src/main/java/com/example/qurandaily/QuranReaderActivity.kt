package com.example.qurandaily

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonParser
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.content.Intent
import android.widget.ImageButton

// ---------------- CONFIG ----------------

private const val CLIENT_ID = "f09c3d3c-4f3d-4348-95e2-e7de1671e948"
private const val CLIENT_SECRET = "40_nOTEmKGQ6~I4gUETqopbqfb"

private const val OAUTH_BASE = "https://oauth2.quran.foundation/"
private const val API_BASE = "https://api.quran.com/api/v4/"

// --------------------------------------------------
// Activity
// --------------------------------------------------

class QuranReaderActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    private lateinit var rvAyahs: RecyclerView
    private lateinit var rvChapters: RecyclerView

    private lateinit var etSearchChapters: EditText
    private lateinit var btnCloseChapters: ImageView
    private lateinit var btnOpenDrawer: ImageView
    private lateinit var layoutSurahSelector: LinearLayout

    private lateinit var tvCurrentSurah: TextView
    private lateinit var tvSurahNameAr: TextView
    private lateinit var tvSurahMeta: TextView

    private lateinit var ayahAdapter: AyahAdapter
    private lateinit var chapterAdapter: ChapterAdapter

    private lateinit var btnBookmarksTop: ImageButton   // <– ADD THIS


    private val allChapters = mutableListOf<ChapterUiModel>()
    private val ayahsMap = mutableMapOf<Int, List<AyahUiModel>>()  // surahNumber -> ayahs

    private var mediaPlayer: MediaPlayer? = null
    private var accessToken: String? = null

    // ---------- Firebase ----------
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(API_BASE)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val api by lazy {
        retrofit.create(QuranApiService::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quran_reader)

        initViews()
        setupRecyclerViews()
        setupDrawerAndSelectors()
        setupSearch()

        lifecycleScope.launch {
            try {
                fetchAccessToken()
                fetchChaptersAndInit()
            } catch (e: Exception) {
                Toast.makeText(
                    this@QuranReaderActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // ---------------- View init ----------------

    private fun initViews() {
        drawerLayout = findViewById(R.id.drawer_layout)

        rvAyahs = findViewById(R.id.rv_ayahs)
        rvChapters = findViewById(R.id.rv_chapters)

        etSearchChapters = findViewById(R.id.et_search_chapters)
        btnCloseChapters = findViewById(R.id.btn_close_chapters)
        btnOpenDrawer = findViewById(R.id.btn_open_drawer)
        layoutSurahSelector = findViewById(R.id.layout_surah_selector)

        tvCurrentSurah = findViewById(R.id.tv_current_surah)
        tvSurahNameAr = findViewById(R.id.tv_surah_name_ar)
        tvSurahMeta = findViewById(R.id.tv_surah_meta)

        btnBookmarksTop = findViewById(R.id.btn_bookmarks)

        btnBookmarksTop.setOnClickListener {
            // open bookmarks screen
            startActivity(Intent(this, BookmarksActivity::class.java))
        }
    }

    private fun setupRecyclerViews() {
        ayahAdapter = AyahAdapter(
            onBookmarkClick = { ayah ->
                bookmarkAyah(ayah)
            },
            onTafseerClick = { /* reserved for future tafsir calls */ },
            onPlayClick = { ayah ->
                playAyahAudio(ayah)
            }
        )

        rvAyahs.layoutManager = LinearLayoutManager(this)
        rvAyahs.adapter = ayahAdapter

        chapterAdapter = ChapterAdapter(
            chapters = allChapters.toMutableList(),
            onChapterClick = { chapter ->
                drawerLayout.closeDrawer(GravityCompat.START)
                loadSurah(chapter)
            }
        )
        rvChapters.layoutManager = LinearLayoutManager(this)
        rvChapters.adapter = chapterAdapter
    }

    private fun setupDrawerAndSelectors() {
        btnOpenDrawer.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        layoutSurahSelector.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        btnCloseChapters.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
        }
    }

    private fun setupSearch() {
        etSearchChapters.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.trim()?.lowercase() ?: ""
                val filtered = if (query.isEmpty()) {
                    allChapters
                } else {
                    allChapters.filter { chapter ->
                        chapter.englishName.lowercase().contains(query) ||
                                chapter.arabicName.contains(query) ||
                                chapter.number.toString().startsWith(query)
                    }
                }
                chapterAdapter.updateList(filtered)
            }
        })
    }

    // ---------------- OAuth + chapters + verses ----------------

    private suspend fun fetchAccessToken() = withContext(Dispatchers.IO) {
        if (accessToken != null) return@withContext

        val client = OkHttpClient()

        val basic = android.util.Base64.encodeToString(
            "$CLIENT_ID:$CLIENT_SECRET".toByteArray(),
            android.util.Base64.NO_WRAP
        )

        val body = FormBody.Builder()
            .add("grant_type", "client_credentials")
            .add("scope", "content")
            .build()

        val req = Request.Builder()
            .url("${OAUTH_BASE}oauth2/token")
            .addHeader("Authorization", "Basic $basic")
            .addHeader("Accept", "application/json")
            .post(body)
            .build()

        val resp = client.newCall(req).execute()
        val respBody = resp.body?.string() ?: throw Exception("Empty token response")

        val json = JsonParser.parseString(respBody).asJsonObject
        accessToken = json["access_token"].asString
    }

    private suspend fun fetchChaptersAndInit() {
        val token = accessToken ?: throw Exception("No access token")

        val result = api.getChapters("Bearer $token")

        allChapters.clear()
        allChapters.addAll(
            result.chapters.map { ch ->
                ChapterUiModel(
                    number = ch.id,
                    englishName = ch.nameSimple,
                    arabicName = ch.nameArabic,
                    meta = "${ch.versesCount} verses • ${ch.revelationPlace}"
                )
            }
        )

        chapterAdapter.updateList(allChapters)

        if (allChapters.isNotEmpty()) {
            loadSurah(allChapters.first())
        }
    }

    private fun loadSurah(chapter: ChapterUiModel) {
        tvCurrentSurah.text = "${chapter.englishName} (${chapter.number})"
        tvSurahNameAr.text = chapter.arabicName
        tvSurahMeta.text = chapter.meta

        val cached = ayahsMap[chapter.number]
        if (cached != null) {
            ayahAdapter.submitList(cached)
            return
        }

        lifecycleScope.launch {
            try {
                val token = accessToken ?: return@launch
                val resp = api.getVersesByChapter("Bearer $token", chapter.number)

                val mapped = resp.verses.map { v ->
                    AyahUiModel(
                        surahNumber = chapter.number,
                        ayahNumber = v.verseNumber,
                        indexLabel = v.verseKey,
                        arabicText = v.textUthmani ?: "",
                        translationText = v.translations?.firstOrNull()?.text ?: ""
                    )
                }

                ayahsMap[chapter.number] = mapped
                ayahAdapter.submitList(mapped)
            } catch (e: Exception) {
                Toast.makeText(
                    this@QuranReaderActivity,
                    "Failed to load verses",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // ---------------- Bookmarks (Firebase) ----------------

    private fun bookmarkAyah(ayah: AyahUiModel) {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // Firestore path: users/{uid}/bookmarks/{verseKey}
        val bookmarkData = hashMapOf(
            "surahNumber" to ayah.surahNumber,
            "ayahNumber" to ayah.ayahNumber,
            "indexLabel" to ayah.indexLabel,
            "arabicText" to ayah.arabicText,
            "translationText" to ayah.translationText,
            "createdAt" to Timestamp.now()
        )

        firestore.collection("users")
            .document(user.uid)
            .collection("bookmarks")
            .document(ayah.indexLabel)   // verseKey as document id
            .set(bookmarkData)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Bookmarked ${ayah.indexLabel}",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Failed to bookmark: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    // ---------------- Audio playback per verse ----------------

    private fun playAyahAudio(ayah: AyahUiModel) {
        val verseKey = ayah.indexLabel // e.g. "1:1"

        lifecycleScope.launch {
            try {
                val token = accessToken ?: return@launch

                val resp = api.getVerseByKeyWithAudio(
                    bearer = "Bearer $token",
                    verseKey = verseKey,
                    reciterId = 6
                )

                val rawUrl = resp.verse.audio?.url
                if (rawUrl.isNullOrBlank()) {
                    Toast.makeText(
                        this@QuranReaderActivity,
                        "Audio not available for ${ayah.indexLabel}",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@launch
                }

                val finalUrl = when {
                    rawUrl.startsWith("//") -> "https:$rawUrl"
                    rawUrl.startsWith("http") -> rawUrl
                    else -> "https://mirrors.quranicaudio.com/everyayah/$rawUrl"
                }

                withContext(Dispatchers.Main) {
                    mediaPlayer?.release()
                    mediaPlayer = MediaPlayer().apply {
                        setDataSource(finalUrl)
                        setOnPreparedListener { it.start() }
                        setOnCompletionListener {
                            it.release()
                            mediaPlayer = null
                        }
                        setOnErrorListener { mp, _, _ ->
                            mp.release()
                            mediaPlayer = null
                            true
                        }
                        prepareAsync()
                    }

                    Toast.makeText(
                        this@QuranReaderActivity,
                        "Playing ${ayah.indexLabel}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@QuranReaderActivity,
                        "Failed to play audio",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    @SuppressLint("GestureBackNavigation")
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }
}

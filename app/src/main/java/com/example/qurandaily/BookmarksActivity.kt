package com.example.qurandaily

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class BookmarksActivity : AppCompatActivity() {

    private lateinit var rvBookmarkedAyahs: RecyclerView
    private lateinit var btnBack: ImageView
    private lateinit var ayahAdapter: AyahAdapter

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmarks)

        initViews()
        setupRecycler()
        loadBookmarks()
    }

    private fun initViews() {
        rvBookmarkedAyahs = findViewById(R.id.rv_bookmarked_ayahs)
        btnBack = findViewById(R.id.btn_back)

        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecycler() {
        ayahAdapter = AyahAdapter(
            onBookmarkClick = { ayah ->
                // later we can implement "remove bookmark" etc.
                Toast.makeText(
                    this,
                    "Bookmark already saved: ${ayah.indexLabel}",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onTafseerClick = { /* not used here yet */ },
            onPlayClick = { ayah ->
                // for now just info; you can reuse playAyahAudio if you want audio here too
                Toast.makeText(
                    this,
                    "Play from main Quran screen for now (${ayah.indexLabel})",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        rvBookmarkedAyahs.layoutManager = LinearLayoutManager(this)
        rvBookmarkedAyahs.adapter = ayahAdapter
    }

    private fun loadBookmarks() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        firestore
            .collection("users")
            .document(user.uid)
            .collection("bookmarks")
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    Toast.makeText(this, "No bookmarks yet", Toast.LENGTH_SHORT).show()
                    ayahAdapter.submitList(emptyList())
                    return@addOnSuccessListener
                }

                val list = snapshot.documents.map { doc ->
                    // doc.id is like "1:2"
                    val docId = doc.id
                    val parts = docId.split(":")
                    val surahFromId = parts.getOrNull(0)?.toIntOrNull() ?: 0
                    val ayahFromId = parts.getOrNull(1)?.toIntOrNull() ?: 0

                    // try fields; fall back to ID if fields are missing
                    val surahNumber = doc.getLong("surahNumber")?.toInt() ?: surahFromId
                    val ayahNumber = doc.getLong("ayahNumber")?.toInt() ?: ayahFromId
                    val indexLabel = doc.getString("indexLabel") ?: docId
                    val arabicText = doc.getString("arabicText") ?: ""
                    val translationText = doc.getString("translationText") ?: ""

                    AyahUiModel(
                        surahNumber = surahNumber,
                        ayahNumber = ayahNumber,
                        indexLabel = indexLabel,
                        arabicText = arabicText,
                        translationText = translationText
                    )
                }

                ayahAdapter.submitList(list)
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this,
                    "Failed to load bookmarks: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}

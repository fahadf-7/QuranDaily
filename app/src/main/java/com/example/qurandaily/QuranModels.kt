package com.example.qurandaily

import com.google.gson.annotations.SerializedName

// ---------------- UI MODELS ----------------

data class AyahUiModel(
    val surahNumber: Int,
    val ayahNumber: Int,
    val indexLabel: String,      // e.g. "1:1"
    val arabicText: String,
    val translationText: String,
    val isBookmarked: Boolean = false
)

data class ChapterUiModel(
    val number: Int,
    val englishName: String,
    val meta: String,            // e.g. "7 verses • makkah"
    val arabicName: String
)

// ---------------- API DTOs ----------------

data class ChaptersResponse(
    val chapters: List<ApiChapter>
)

data class ApiChapter(
    val id: Int,
    @SerializedName("name_simple") val nameSimple: String,
    @SerializedName("name_arabic") val nameArabic: String,
    @SerializedName("verses_count") val versesCount: Int,
    @SerializedName("revelation_place") val revelationPlace: String
)

data class VersesByChapterResponse(
    val verses: List<ApiVerse>
)

data class ApiVerse(
    @SerializedName("verse_number") val verseNumber: Int,
    @SerializedName("verse_key") val verseKey: String,
    @SerializedName("text_uthmani") val textUthmani: String?,
    val translations: List<ApiTranslation>?
)

data class ApiTranslation(
    val text: String
)

// ---- Audio DTOs for verses/by_key ----

data class VerseByKeyResponse(
    val verse: VerseWithAudio
)

data class VerseWithAudio(
    @SerializedName("verse_key") val verseKey: String,
    val audio: ApiAudio?
)

data class ApiAudio(
    val url: String?,
    val duration: Float? = null
)

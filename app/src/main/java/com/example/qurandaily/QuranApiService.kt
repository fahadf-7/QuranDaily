package com.example.qurandaily

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit interface for Quran.com API
 * (token is always passed as Authorization header)
 */
interface QuranApiService {

    @GET("chapters")
    suspend fun getChapters(
        @Header("Authorization") bearer: String
    ): ChaptersResponse

    // Per-ayah audio (similar to your Python /verses/by_key route)
    @GET("verses/by_key/{verseKey}")
    suspend fun getVerseByKeyWithAudio(
        @Header("Authorization") bearer: String,
        @Path("verseKey") verseKey: String,
        // use literal defaults to avoid needing constants across files
        @Query("audio") reciterId: Int = 6, // Mishary Alafasy
        @Query("fields") fields: String = "verse_key,verse_number"
    ): VerseByKeyResponse

    // Ayahs + translations by chapter
    @GET("verses/by_chapter/{chapterId}")
    suspend fun getVersesByChapter(
        @Header("Authorization") bearer: String,
        @Path("chapterId") chapterId: Int,
        @Query("language") language: String = "en",
        // Saheeh International ID
        @Query("translations") translations: String = "20",
        @Query("fields") fields: String = "text_uthmani",
        @Query("per_page") perPage: Int = 300,
        @Query("page") page: Int = 1,
        @Query("words") words: Boolean = false,
        @Query("translation_fields") translationFields: String = "text"
    ): VersesByChapterResponse
}

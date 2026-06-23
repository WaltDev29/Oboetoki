package kr.ac.waltdev29.oboetoki.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: Int,
    val username: String,
    val name: String,
    val phone: String,
    val email: String,
    @SerializedName("consecutive_attendance")
    val consecutiveAttendance: Int,
    @SerializedName("last_login_date")
    val lastLoginDate: String?
)

data class TokenResponse(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("token_type")
    val tokenType: String
)

data class MainData(
    @SerializedName("consecutive_attendance")
    val consecutiveAttendance: Int,
    @SerializedName("total_words")
    val totalWords: Int,
    @SerializedName("memorized_words")
    val memorizedWords: Int,
    @SerializedName("quote_of_the_day")
    val quoteOfTheDay: String
)

data class Word(
    val id: Int = 0,
    @SerializedName("user_id")
    val userId: Int = 0,
    @SerializedName("original_word")
    val originalWord: String,
    @SerializedName("translated_word")
    val translatedWord: String,
    @SerializedName("source_language")
    val sourceLanguage: String? = null,
    @SerializedName("is_memorized")
    val isMemorized: Boolean = false,
    @SerializedName("memorization_level")
    val memorizationLevel: Int = 0,
    @SerializedName("created_at")
    val createdAt: String? = null
)

data class OcrResult(
    @SerializedName("parsed_words")
    val parsedWords: List<Word>
)

data class DuplicateCheckResponse(
    @SerializedName("is_available")
    val isAvailable: Boolean
)

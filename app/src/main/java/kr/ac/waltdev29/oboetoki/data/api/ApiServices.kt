package kr.ac.waltdev29.oboetoki.data.api

import kr.ac.waltdev29.oboetoki.data.model.DuplicateCheckResponse
import kr.ac.waltdev29.oboetoki.data.model.MainData
import kr.ac.waltdev29.oboetoki.data.model.OcrResult
import kr.ac.waltdev29.oboetoki.data.model.TokenResponse
import kr.ac.waltdev29.oboetoki.data.model.User
import kr.ac.waltdev29.oboetoki.data.model.Word
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface AuthService {
    @POST("/auth/signup")
    suspend fun signup(@Body request: HashMap<String, String>): User

    @GET("/auth/check-email")
    suspend fun checkEmail(@Query("email") email: String): DuplicateCheckResponse

    @FormUrlEncoded
    @POST("/auth/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): TokenResponse
}

interface MainService {
    @GET("/main/")
    suspend fun getMainData(): MainData
}

interface WordService {
    @GET("/words/")
    suspend fun getWords(
        @Query("is_memorized") isMemorized: Boolean? = null
    ): List<Word>

    @POST("/words/")
    suspend fun addWord(@Body request: Word): Word

    @POST("/words/batch")
    suspend fun addWordsBatch(@Body request: List<Word>): List<Word>

    @GET("/words/{word_id}")
    suspend fun getWordDetail(@Path("word_id") wordId: Int): Word

    @PUT("/words/{word_id}")
    suspend fun updateWord(
        @Path("word_id") wordId: Int,
        @Body request: HashMap<String, Any>
    ): Word

    @Multipart
    @POST("/words/ocr")
    suspend fun parseOcrWords(@Part file: MultipartBody.Part): OcrResult
}

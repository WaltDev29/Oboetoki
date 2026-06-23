package kr.ac.waltdev29.oboetoki.data.api

import kr.ac.waltdev29.oboetoki.util.PreferenceManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // Emulator to localhost
    private const val BASE_URL = "http://10.0.2.2:8000"

    private var retrofit: Retrofit? = null

    fun getClient(preferenceManager: PreferenceManager): Retrofit {
        if (retrofit == null) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val authInterceptor = AuthInterceptor(preferenceManager)

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(authInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }

    fun getAuthService(preferenceManager: PreferenceManager): AuthService =
        getClient(preferenceManager).create(AuthService::class.java)

    fun getMainService(preferenceManager: PreferenceManager): MainService =
        getClient(preferenceManager).create(MainService::class.java)

    fun getWordService(preferenceManager: PreferenceManager): WordService =
        getClient(preferenceManager).create(WordService::class.java)
}

package kr.ac.waltdev29.oboetoki.data.api

import kr.ac.waltdev29.oboetoki.util.PreferenceManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val preferenceManager: PreferenceManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        
        val token = preferenceManager.getToken()
        if (!token.isNullOrEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        
        return chain.proceed(requestBuilder.build())
    }
}

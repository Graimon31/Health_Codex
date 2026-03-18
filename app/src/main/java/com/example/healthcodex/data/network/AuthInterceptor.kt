package com.example.healthcodex.data.network

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp interceptor that attaches the Bearer token to every request
 * (except login and refresh endpoints).
 */
class AuthInterceptor(private val tokenProvider: TokenProvider) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val path = original.url.encodedPath

        // Skip auth header for login/refresh/health endpoints
        if (path.endsWith("/login") || path.endsWith("/refresh") || path.endsWith("/health")) {
            return chain.proceed(original)
        }

        val token = runBlocking { tokenProvider.accessToken() }
        if (token.isNullOrBlank()) {
            return chain.proceed(original)
        }

        val request = original.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        return chain.proceed(request)
    }
}

/**
 * Abstraction so the interceptor doesn't depend on DataStore directly.
 */
fun interface TokenProvider {
    suspend fun accessToken(): String?
}

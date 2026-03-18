package com.example.healthcodex.data.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton-like factory that builds Retrofit instances pointing at the
 * Health_Backend server.  The base URL can be changed at runtime (e.g. from
 * the settings screen) — calling [rebuild] swaps the underlying Retrofit.
 */
class ApiClient(private val tokenProvider: TokenProvider) {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val authInterceptor = AuthInterceptor(tokenProvider)

    private fun buildOkHttp(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    @Volatile
    private var retrofit: Retrofit? = null

    @Volatile
    private var currentBaseUrl: String? = null

    fun rebuild(baseUrl: String): Retrofit {
        val url = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        if (url == currentBaseUrl && retrofit != null) return retrofit!!
        val r = Retrofit.Builder()
            .baseUrl(url)
            .client(buildOkHttp())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        retrofit = r
        currentBaseUrl = url
        return r
    }

    fun authApi(baseUrl: String): AuthApi = rebuild(baseUrl).create(AuthApi::class.java)
}

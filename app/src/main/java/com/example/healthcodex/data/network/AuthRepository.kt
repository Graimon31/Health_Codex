package com.example.healthcodex.data.network

import com.example.healthcodex.data.prefs.PrefsRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Manages authentication state against the Health_Backend API.
 */
class AuthRepository(
    private val prefs: PrefsRepository,
    private val apiClient: ApiClient
) {
    val loggedInUserName: Flow<String?> = prefs.loggedInUserName
    val authToken: Flow<String?> = prefs.authToken
    val baseUrl: Flow<String?> = prefs.baseUrl

    private val errorAdapter by lazy {
        Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
            .adapter(ApiError::class.java)
    }

    sealed class AuthResult {
        data class Success(val user: UserOut) : AuthResult()
        data class Error(val message: String) : AuthResult()
    }

    suspend fun register(email: String, password: String, fullName: String): AuthResult {
        val url = prefs.baseUrl.first()
        if (url.isNullOrBlank()) {
            return AuthResult.Error("Адрес сервера не задан. Укажите его в настройках безопасности.")
        }
        return try {
            val api = apiClient.authApi(url)
            val response = api.register(RegisterRequest(email = email, password = password, fullName = fullName))
            if (response.isSuccessful) {
                val body = response.body()!!
                prefs.setAuthToken(body.accessToken)
                prefs.setRefreshToken(body.refreshToken)
                prefs.setLoggedInUserName(body.user.fullName)
                AuthResult.Success(body.user)
            } else {
                val detail = response.errorBody()?.string()?.let { raw ->
                    try { errorAdapter.fromJson(raw)?.detail } catch (_: Exception) { null }
                }
                AuthResult.Error(detail ?: "Ошибка регистрации (${response.code()})")
            }
        } catch (e: Exception) {
            AuthResult.Error("Нет связи с сервером: ${e.localizedMessage}")
        }
    }

    suspend fun login(email: String, password: String): AuthResult {
        val url = prefs.baseUrl.first()
        if (url.isNullOrBlank()) {
            return AuthResult.Error("Адрес сервера не задан. Укажите его в настройках безопасности.")
        }
        return try {
            val api = apiClient.authApi(url)
            val response = api.login(LoginRequest(email = email, password = password))
            if (response.isSuccessful) {
                val body = response.body()!!
                prefs.setAuthToken(body.accessToken)
                prefs.setRefreshToken(body.refreshToken)
                prefs.setLoggedInUserName(body.user.fullName)
                AuthResult.Success(body.user)
            } else {
                val detail = response.errorBody()?.string()?.let { raw ->
                    try { errorAdapter.fromJson(raw)?.detail } catch (_: Exception) { null }
                }
                AuthResult.Error(detail ?: "Ошибка входа (${response.code()})")
            }
        } catch (e: Exception) {
            AuthResult.Error("Нет связи с сервером: ${e.localizedMessage}")
        }
    }

    suspend fun refreshToken(): Boolean {
        val url = prefs.baseUrl.first() ?: return false
        val refresh = prefs.refreshToken.first() ?: return false
        return try {
            val api = apiClient.authApi(url)
            val response = api.refresh(RefreshRequest(refreshToken = refresh))
            if (response.isSuccessful) {
                prefs.setAuthToken(response.body()!!.accessToken)
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    suspend fun logout() {
        prefs.clearSession()
    }

    suspend fun checkServer(): AuthResult {
        val url = prefs.baseUrl.first()
        if (url.isNullOrBlank()) {
            return AuthResult.Error("Адрес сервера не задан")
        }
        return try {
            val api = apiClient.authApi(url)
            val response = api.healthCheck()
            if (response.isSuccessful) {
                AuthResult.Success(UserOut(id = 0, role = "", fullName = "OK"))
            } else {
                AuthResult.Error("Сервер вернул ${response.code()}")
            }
        } catch (e: Exception) {
            AuthResult.Error("Нет связи: ${e.localizedMessage}")
        }
    }
}

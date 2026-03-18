// app/src/main/java/com/example/healthcodex/data/prefs/PrefsRepository.kt
package com.example.healthcodex.data.prefs

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

/**
 * Repository exposing the preferences to the rest of the app.
 */
class PrefsRepository(context: Context) {
    private val prefs = UserPrefs(context.dataStore)

    val baseUrl: Flow<String?> = prefs.baseUrl
    val authToken: Flow<String?> = prefs.authToken
    val refreshToken: Flow<String?> = prefs.refreshToken
    val loggedInUserName: Flow<String?> = prefs.loggedInUserName
    val allowHttp: Flow<Boolean> = prefs.allowHttp
    val demoMode: Flow<Boolean> = prefs.demoMode
    val biometricLockEnabled: Flow<Boolean> = prefs.biometricLockEnabled

    suspend fun setBaseUrl(url: String) = prefs.updateBaseUrl(url)
    suspend fun setAuthToken(token: String?) = prefs.updateAuthToken(token)
    suspend fun setRefreshToken(token: String?) = prefs.updateRefreshToken(token)
    suspend fun setLoggedInUserName(name: String?) = prefs.updateLoggedInUserName(name)
    suspend fun clearSession() = prefs.clearSession()
    suspend fun setAllowHttp(enabled: Boolean) = prefs.setAllowHttp(enabled)
    suspend fun setDemoMode(enabled: Boolean) = prefs.setDemoMode(enabled)
    suspend fun setBiometricLock(enabled: Boolean) = prefs.setBiometricLock(enabled)
}

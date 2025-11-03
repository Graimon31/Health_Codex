// app/src/main/java/com/example/healthcodex/data/prefs/UserPrefs.kt
package com.example.healthcodex.data.prefs

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * DataStore wrapper for lightweight user preferences.
 */
class UserPrefs(private val dataStore: androidx.datastore.core.DataStore<Preferences>) {
    private object Keys {
        val BASE_URL = stringPreferencesKey("base_url")
        val AUTH_TOKEN = stringPreferencesKey("auth_token")
        val ALLOW_HTTP = booleanPreferencesKey("allow_http")
        val DEMO_MODE = booleanPreferencesKey("demo_mode")
        val BIOMETRIC_LOCK = booleanPreferencesKey("biometric_lock")
    }

    val baseUrl: Flow<String?> = dataStore.data.map { it[Keys.BASE_URL] }
    val authToken: Flow<String?> = dataStore.data.map { it[Keys.AUTH_TOKEN] }
    val allowHttp: Flow<Boolean> = dataStore.data.map { it[Keys.ALLOW_HTTP] ?: false }
    val demoMode: Flow<Boolean> = dataStore.data.map { it[Keys.DEMO_MODE] ?: false }
    val biometricLockEnabled: Flow<Boolean> = dataStore.data.map { it[Keys.BIOMETRIC_LOCK] ?: false }

    suspend fun updateBaseUrl(url: String) {
        dataStore.edit { it[Keys.BASE_URL] = url }
    }

    suspend fun updateAuthToken(token: String?) {
        dataStore.edit {
            if (token == null) it.remove(Keys.AUTH_TOKEN) else it[Keys.AUTH_TOKEN] = token
        }
    }

    suspend fun setAllowHttp(enabled: Boolean) {
        dataStore.edit { it[Keys.ALLOW_HTTP] = enabled }
    }

    suspend fun setDemoMode(enabled: Boolean) {
        dataStore.edit { it[Keys.DEMO_MODE] = enabled }
    }

    suspend fun setBiometricLock(enabled: Boolean) {
        dataStore.edit { it[Keys.BIOMETRIC_LOCK] = enabled }
    }
}

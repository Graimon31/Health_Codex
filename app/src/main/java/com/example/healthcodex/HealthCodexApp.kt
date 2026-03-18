// app/src/main/java/com/example/healthcodex/HealthCodexApp.kt
package com.example.healthcodex

import android.app.Application
import com.example.healthcodex.data.db.AppDatabase
import com.example.healthcodex.data.measurements.MeasurementsRepository
import com.example.healthcodex.data.network.ApiClient
import com.example.healthcodex.data.network.AuthRepository
import com.example.healthcodex.data.network.TokenProvider
import com.example.healthcodex.data.prefs.PrefsRepository
import com.example.healthcodex.data.profile.ProfileRepository
import kotlinx.coroutines.flow.first

/**
 * Application entry point exposing lazy singletons for repositories and
 * database access so UI layers can avoid re-instantiating heavy components.
 */
class HealthCodexApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val prefsRepository: PrefsRepository by lazy { PrefsRepository(this) }
    val profileRepository: ProfileRepository by lazy {
        ProfileRepository(database.userProfileDao(), prefsRepository, apiClient)
    }
    val measurementsRepository: MeasurementsRepository by lazy {
        MeasurementsRepository(
            database.measurementDao(),
            database.connectedDeviceDao(),
            apiClient,
            prefsRepository
        )
    }

    val apiClient: ApiClient by lazy {
        ApiClient(TokenProvider { prefsRepository.authToken.first() })
    }

    val authRepository: AuthRepository by lazy {
        AuthRepository(prefsRepository, apiClient)
    }
}

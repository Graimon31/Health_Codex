// app/src/main/java/com/example/healthcodex/HealthCodexApp.kt
package com.example.healthcodex

import android.app.Application
import com.example.healthcodex.data.db.AppDatabase
import com.example.healthcodex.data.measurements.MeasurementsRepository
import com.example.healthcodex.data.prefs.PrefsRepository
import com.example.healthcodex.data.profile.ProfileRepository

/**
 * Application entry point exposing lazy singletons for repositories and
 * database access so UI layers can avoid re-instantiating heavy components.
 */
class HealthCodexApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val prefsRepository: PrefsRepository by lazy { PrefsRepository(this) }
    val profileRepository: ProfileRepository by lazy {
        ProfileRepository(database.userProfileDao(), prefsRepository)
    }
    val measurementsRepository: MeasurementsRepository by lazy {
        MeasurementsRepository(
            database.measurementDao(),
            database.connectedDeviceDao()
        )
    }
}

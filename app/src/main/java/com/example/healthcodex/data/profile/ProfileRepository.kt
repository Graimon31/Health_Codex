// app/src/main/java/com/example/healthcodex/data/profile/ProfileRepository.kt
package com.example.healthcodex.data.profile

import android.content.Context
import com.example.healthcodex.data.db.AppDatabase
import com.example.healthcodex.data.db.UserProfileEntity
import com.example.healthcodex.data.prefs.PrefsRepository
import com.example.healthcodex.util.Validation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Repository combining Room and DataStore sources.
 */
class ProfileRepository(
    context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    private val database = AppDatabase.getInstance(context)
    private val prefsRepository = PrefsRepository(context)
    private val dao = database.userProfileDao()

    val profileFlow: Flow<UserProfile?> =
        dao.observeProfile().map { it?.toDomain() }.flowOn(ioDispatcher)

    suspend fun getProfile(): UserProfile? = withContext(ioDispatcher) {
        dao.getProfile()?.toDomain()
    }

    suspend fun upsertProfile(profile: UserProfile) = withContext(ioDispatcher) {
        Validation.validateProfile(profile)
        dao.upsertProfile(profile.toEntity())
    }

    suspend fun linkBleDevice(name: String?, address: String?) {
        val current = getProfile() ?: return
        upsertProfile(current.copy(bleDeviceName = name, bleDeviceAddress = address))
    }

    suspend fun setBiometricLock(enabled: Boolean) = prefsRepository.setBiometricLock(enabled)
    suspend fun setAllowHttp(enabled: Boolean) = prefsRepository.setAllowHttp(enabled)
    suspend fun setDemoMode(enabled: Boolean) = prefsRepository.setDemoMode(enabled)

    val allowHttp: Flow<Boolean> = prefsRepository.allowHttp
    val demoMode: Flow<Boolean> = prefsRepository.demoMode
    val biometricLockEnabled: Flow<Boolean> = prefsRepository.biometricLockEnabled

    private fun UserProfileEntity.toDomain(): UserProfile = UserProfile(
        userId = userId,
        fullName = fullName,
        birthDate = birthDate,
        sex = sex,
        heightCm = heightCm,
        weightKg = weightKg,
        units = units,
        conditions = conditions,
        allergies = allergies,
        medications = medications,
        restingHr = restingHr,
        bpBaselineSystolic = bpBaselineSystolic,
        bpBaselineDiastolic = bpBaselineDiastolic,
        hrHigh = hrHigh,
        bpSysHigh = bpSysHigh,
        bpDiaHigh = bpDiaHigh,
        emergencyName = emergencyName,
        emergencyPhone = emergencyPhone,
        doctorName = doctorName,
        doctorPhone = doctorPhone,
        bleDeviceName = bleDeviceName,
        bleDeviceAddress = bleDeviceAddress,
        shareWithDoctor = shareWithDoctor,
        consentAccepted = consentAccepted,
        consentVersion = consentVersion,
        consentTimestamp = consentTimestamp
    )

    private fun UserProfile.toEntity(): UserProfileEntity = UserProfileEntity(
        userId = userId,
        fullName = fullName,
        birthDate = birthDate,
        sex = sex,
        heightCm = heightCm,
        weightKg = weightKg,
        units = units,
        conditions = conditions,
        allergies = allergies,
        medications = medications,
        restingHr = restingHr,
        bpBaselineSystolic = bpBaselineSystolic,
        bpBaselineDiastolic = bpBaselineDiastolic,
        hrHigh = hrHigh,
        bpSysHigh = bpSysHigh,
        bpDiaHigh = bpDiaHigh,
        emergencyName = emergencyName,
        emergencyPhone = emergencyPhone,
        doctorName = doctorName,
        doctorPhone = doctorPhone,
        bleDeviceName = bleDeviceName,
        bleDeviceAddress = bleDeviceAddress,
        shareWithDoctor = shareWithDoctor,
        consentAccepted = consentAccepted,
        consentVersion = consentVersion,
        consentTimestamp = consentTimestamp
    )
}

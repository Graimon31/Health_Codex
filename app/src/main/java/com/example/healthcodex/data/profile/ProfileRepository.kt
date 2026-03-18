// app/src/main/java/com/example/healthcodex/data/profile/ProfileRepository.kt
package com.example.healthcodex.data.profile

import android.content.Context
import android.util.Log
import com.example.healthcodex.data.db.AppDatabase
import com.example.healthcodex.data.db.UserProfileDao
import com.example.healthcodex.data.db.UserProfileEntity
import com.example.healthcodex.data.network.ApiClient
import com.example.healthcodex.data.network.LinkDoctorRequest
import com.example.healthcodex.data.network.PatientProfileUpdateRequest
import com.example.healthcodex.data.prefs.PrefsRepository
import com.example.healthcodex.util.Validation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Repository combining Room and DataStore sources.
 */
class ProfileRepository(
    private val userProfileDao: UserProfileDao,
    private val prefsRepository: PrefsRepository,
    private val apiClient: ApiClient? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    constructor(context: Context, ioDispatcher: CoroutineDispatcher = Dispatchers.IO) : this(
        AppDatabase.getInstance(context).userProfileDao(),
        PrefsRepository(context),
        null,
        ioDispatcher
    )

    val profileFlow: Flow<UserProfile?> =
        userProfileDao.observeProfile().map { it?.toDomain() }.flowOn(ioDispatcher)

    suspend fun getProfile(): UserProfile? = withContext(ioDispatcher) {
        userProfileDao.getProfile()?.toDomain()
    }

    suspend fun upsertProfile(profile: UserProfile) = withContext(ioDispatcher) {
        Validation.validateProfile(profile)
        userProfileDao.upsertProfile(profile.toEntity())
        // Sync to server (fire-and-forget)
        try {
            syncProfileToServer(profile)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync profile to server: ${e.message}")
        }
    }

    /**
     * Fetches the patient profile from the server and merges it into local storage.
     */
    suspend fun fetchProfileFromServer(): Result<Unit> = withContext(ioDispatcher) {
        val url = prefsRepository.baseUrl.first()
        val token = prefsRepository.authToken.first()
        if (url.isNullOrBlank() || token.isNullOrBlank()) {
            return@withContext Result.failure(Exception("Не авторизован"))
        }
        try {
            val api = apiClient?.patientApi(url) ?: return@withContext Result.failure(Exception("API не настроен"))
            val response = api.getProfile()
            if (response.isSuccessful) {
                val body = response.body() ?: return@withContext Result.success(Unit)
                val current = userProfileDao.getProfile()?.toDomain()
                val updated = (current ?: createDefaultProfile()).copy(
                    birthDate = body.birthDate?.let {
                        try { LocalDate.parse(it) } catch (_: Exception) { null }
                    },
                    sex = body.sex?.let {
                        try { Sex.valueOf(it) } catch (_: Exception) { Sex.OTHER }
                    } ?: Sex.OTHER,
                    heightCm = body.heightCm,
                    weightKg = body.weightKg,
                    conditions = body.conditions,
                    allergies = body.allergies,
                    doctorName = body.doctorName
                )
                userProfileDao.upsertProfile(updated.toEntity())
                Result.success(Unit)
            } else {
                Result.failure(Exception("Ошибка сервера: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Links the current patient to a doctor by invite code.
     */
    suspend fun linkDoctor(doctorCode: String): Result<String> = withContext(ioDispatcher) {
        val url = prefsRepository.baseUrl.first()
        val token = prefsRepository.authToken.first()
        if (url.isNullOrBlank() || token.isNullOrBlank()) {
            return@withContext Result.failure(Exception("Не авторизован"))
        }
        try {
            val api = apiClient?.patientApi(url) ?: return@withContext Result.failure(Exception("API не настроен"))
            val response = api.linkDoctor(LinkDoctorRequest(doctorCode = doctorCode))
            if (response.isSuccessful) {
                val body = response.body()!!
                // Update local profile with doctor name
                val current = userProfileDao.getProfile()?.toDomain()
                if (current != null) {
                    userProfileDao.upsertProfile(current.copy(doctorName = body.doctorName).toEntity())
                }
                Result.success(body.doctorName)
            } else if (response.code() == 404) {
                Result.failure(Exception("Код врача не найден"))
            } else {
                Result.failure(Exception("Ошибка: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun syncProfileToServer(profile: UserProfile) {
        val url = prefsRepository.baseUrl.first() ?: return
        val token = prefsRepository.authToken.first() ?: return
        if (url.isBlank() || token.isBlank()) return

        val api = apiClient?.patientApi(url) ?: return
        val request = PatientProfileUpdateRequest(
            birthDate = profile.birthDate?.format(DateTimeFormatter.ISO_LOCAL_DATE),
            sex = profile.sex.name,
            heightCm = profile.heightCm,
            weightKg = profile.weightKg,
            conditions = profile.conditions,
            allergies = profile.allergies
        )
        val response = api.updateProfile(request)
        if (response.isSuccessful) {
            Log.d(TAG, "Profile synced to server")
        } else {
            Log.w(TAG, "Server rejected profile update: ${response.code()}")
        }
    }

    private fun createDefaultProfile(): UserProfile = UserProfile(
        userId = "default",
        fullName = "",
        birthDate = null,
        sex = Sex.OTHER,
        heightCm = null,
        weightKg = null,
        units = Units.METRIC,
        conditions = emptyList(),
        allergies = emptyList(),
        medications = emptyList(),
        restingHr = null,
        bpBaselineSystolic = null,
        bpBaselineDiastolic = null,
        hrHigh = null,
        bpSysHigh = null,
        bpDiaHigh = null,
        emergencyName = null,
        emergencyPhone = null,
        doctorName = null,
        doctorPhone = null,
        bleDeviceName = null,
        bleDeviceAddress = null,
        shareWithDoctor = false,
        consentAccepted = false,
        consentVersion = "1.0",
        consentTimestamp = null
    )

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

    companion object {
        private const val TAG = "ProfileRepository"
    }

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

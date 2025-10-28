// app/src/main/java/com/example/healthcodex/data/profile/UserProfile.kt
package com.example.healthcodex.data.profile

import com.squareup.moshi.JsonClass
import java.time.Instant
import java.time.LocalDate

/**
 * Domain representation of the patient profile.
 */
@JsonClass(generateAdapter = true)
data class UserProfile(
    val userId: String,
    val fullName: String,
    val birthDate: LocalDate?,
    val sex: Sex,
    val heightCm: Int?,
    val weightKg: Float?,
    val units: Units,
    val conditions: List<String>,
    val allergies: List<String>,
    val medications: List<Medication>,
    val restingHr: Int?,
    val bpBaselineSystolic: Int?,
    val bpBaselineDiastolic: Int?,
    val hrHigh: Int?,
    val bpSysHigh: Int?,
    val bpDiaHigh: Int?,
    val emergencyName: String?,
    val emergencyPhone: String?,
    val doctorName: String?,
    val doctorPhone: String?,
    val bleDeviceName: String?,
    val bleDeviceAddress: String?,
    val shareWithDoctor: Boolean,
    val consentAccepted: Boolean,
    val consentVersion: String,
    val consentTimestamp: Instant?
)

/**
 * Biological sex values.
 */
enum class Sex { MALE, FEMALE, OTHER }

/**
 * Measurement units for anthropometry.
 */
enum class Units { METRIC, IMPERIAL }

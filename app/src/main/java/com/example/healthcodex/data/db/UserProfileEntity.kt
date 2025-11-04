// app/src/main/java/com/example/healthcodex/data/db/UserProfileEntity.kt
package com.example.healthcodex.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.healthcodex.data.profile.Medication
import com.example.healthcodex.data.profile.Sex
import com.example.healthcodex.data.profile.Units
import java.time.Instant
import java.time.LocalDate

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "full_name") val fullName: String,
    @ColumnInfo(name = "birth_date") val birthDate: LocalDate?,
    val sex: Sex,
    @ColumnInfo(name = "height_cm") val heightCm: Int?,
    @ColumnInfo(name = "weight_kg") val weightKg: Float?,
    val units: Units,
    val conditions: List<String>,
    val allergies: List<String>,
    val medications: List<Medication>,
    @ColumnInfo(name = "resting_hr") val restingHr: Int?,
    @ColumnInfo(name = "bp_baseline_sys") val bpBaselineSystolic: Int?,
    @ColumnInfo(name = "bp_baseline_dia") val bpBaselineDiastolic: Int?,
    @ColumnInfo(name = "hr_high") val hrHigh: Int?,
    @ColumnInfo(name = "bp_sys_high") val bpSysHigh: Int?,
    @ColumnInfo(name = "bp_dia_high") val bpDiaHigh: Int?,
    @ColumnInfo(name = "emergency_name") val emergencyName: String?,
    @ColumnInfo(name = "emergency_phone") val emergencyPhone: String?,
    @ColumnInfo(name = "doctor_name") val doctorName: String?,
    @ColumnInfo(name = "doctor_phone") val doctorPhone: String?,
    @ColumnInfo(name = "ble_device_name") val bleDeviceName: String?,
    @ColumnInfo(name = "ble_device_address") val bleDeviceAddress: String?,
    @ColumnInfo(name = "share_with_doctor") val shareWithDoctor: Boolean,
    @ColumnInfo(name = "consent_accepted") val consentAccepted: Boolean,
    @ColumnInfo(name = "consent_version") val consentVersion: String,
    @ColumnInfo(name = "consent_timestamp") val consentTimestamp: Instant?
)

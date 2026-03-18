package com.example.healthcodex.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    val email: String,
    val password: String,
    @Json(name = "full_name") val fullName: String
)

@JsonClass(generateAdapter = true)
data class PatientProfileResponse(
    @Json(name = "birth_date") val birthDate: String? = null,
    val sex: String? = null,
    @Json(name = "height_cm") val heightCm: Int? = null,
    @Json(name = "weight_kg") val weightKg: Float? = null,
    val conditions: List<String> = emptyList(),
    val allergies: List<String> = emptyList(),
    @Json(name = "doctor_id") val doctorId: Int? = null,
    @Json(name = "doctor_name") val doctorName: String? = null
)

@JsonClass(generateAdapter = true)
data class PatientProfileUpdateRequest(
    @Json(name = "birth_date") val birthDate: String? = null,
    val sex: String? = null,
    @Json(name = "height_cm") val heightCm: Int? = null,
    @Json(name = "weight_kg") val weightKg: Float? = null,
    val conditions: List<String> = emptyList(),
    val allergies: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class LinkDoctorRequest(
    @Json(name = "doctor_code") val doctorCode: String
)

@JsonClass(generateAdapter = true)
data class LinkDoctorResponse(
    @Json(name = "doctor_name") val doctorName: String,
    val message: String
)

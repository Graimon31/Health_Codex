package com.example.healthcodex.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequest(
    val email: String,
    val password: String
)

@JsonClass(generateAdapter = true)
data class RefreshRequest(
    @Json(name = "refresh_token") val refreshToken: String
)

@JsonClass(generateAdapter = true)
data class AuthResponse(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "refresh_token") val refreshToken: String,
    val user: UserOut
)

@JsonClass(generateAdapter = true)
data class RefreshResponse(
    @Json(name = "access_token") val accessToken: String
)

@JsonClass(generateAdapter = true)
data class UserOut(
    val id: Int,
    val role: String,
    @Json(name = "full_name") val fullName: String
)

@JsonClass(generateAdapter = true)
data class HealthCheckResponse(
    val status: String
)

@JsonClass(generateAdapter = true)
data class ApiError(
    val detail: String? = null
)

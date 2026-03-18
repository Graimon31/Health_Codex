package com.example.healthcodex.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.Instant

@JsonClass(generateAdapter = true)
data class MeasurementRequest(
    val type: String,
    @Json(name = "value_primary") val valuePrimary: Double,
    @Json(name = "value_secondary") val valueSecondary: Double? = null,
    val unit: String,
    @Json(name = "measured_at") val measuredAt: String,
    val source: String,
    @Json(name = "device_name") val deviceName: String? = null,
    val note: String? = null
)

@JsonClass(generateAdapter = true)
data class MeasurementResponse(
    val id: Int,
    @Json(name = "patient_id") val patientId: Int,
    val type: String,
    @Json(name = "value_primary") val valuePrimary: Double,
    @Json(name = "value_secondary") val valueSecondary: Double? = null,
    val unit: String,
    @Json(name = "measured_at") val measuredAt: String,
    val source: String,
    @Json(name = "device_name") val deviceName: String? = null,
    val note: String? = null,
    @Json(name = "created_at") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class MeasurementListResponse(
    val items: List<MeasurementResponse>,
    val total: Int
)

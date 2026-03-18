package com.example.healthcodex.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.POST

/**
 * Retrofit interface for patient-specific endpoints (called from mobile app).
 */
interface PatientApi {

    @GET("api/v1/patient/profile")
    suspend fun getProfile(): Response<PatientProfileResponse>

    @PUT("api/v1/patient/profile")
    suspend fun updateProfile(@Body request: PatientProfileUpdateRequest): Response<PatientProfileResponse>

    @POST("api/v1/patient/link-doctor")
    suspend fun linkDoctor(@Body request: LinkDoctorRequest): Response<LinkDoctorResponse>
}

package com.example.healthcodex.data.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit interface for measurement endpoints (called from mobile app).
 */
interface MeasurementsApi {

    @POST("api/v1/measurements")
    suspend fun createMeasurement(@Body request: MeasurementRequest): Response<MeasurementResponse>

    @GET("api/v1/measurements")
    suspend fun getMeasurements(
        @Query("type") type: String? = null,
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null,
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0
    ): Response<List<MeasurementResponse>>

    @GET("api/v1/measurements/{id}")
    suspend fun getMeasurement(@Path("id") id: Int): Response<MeasurementResponse>
}

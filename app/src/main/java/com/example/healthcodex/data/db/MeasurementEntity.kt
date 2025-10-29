// app/src/main/java/com/example/healthcodex/data/db/MeasurementEntity.kt
package com.example.healthcodex.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.healthcodex.data.measurements.MeasurementConfidence
import com.example.healthcodex.data.measurements.MeasurementDetails
import com.example.healthcodex.data.measurements.MeasurementSource
import com.example.healthcodex.data.measurements.MeasurementType
import java.time.Instant

/**
 * Room entity describing a recorded measurement.
 */
@Entity(tableName = "measurements")
data class MeasurementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: MeasurementType,
    val timestamp: Instant,
    val startTimestamp: Instant? = null,
    val source: MeasurementSource,
    val deviceName: String? = null,
    val deviceAddress: String? = null,
    val note: String? = null,
    val confidence: MeasurementConfidence,
    val details: MeasurementDetails,
    val tags: List<String> = emptyList()
)

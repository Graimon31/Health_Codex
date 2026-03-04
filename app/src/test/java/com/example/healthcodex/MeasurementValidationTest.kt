// app/src/test/java/com/example/healthcodex/MeasurementValidationTest.kt
package com.example.healthcodex

import com.example.healthcodex.data.measurements.MeasurementDetails
import com.example.healthcodex.data.measurements.MeasurementEntry
import com.example.healthcodex.data.measurements.MeasurementType
import com.example.healthcodex.util.Validation
import java.time.Instant
import org.junit.Test
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows

/**
 * Unit tests covering measurement validation scenarios.
 */
class MeasurementValidationTest {

    @Test
    fun `heart rate within range succeeds`() {
        val entry = MeasurementEntry(
            type = MeasurementType.HEART_RATE,
            timestamp = Instant.now(),
            details = MeasurementDetails(primaryValue = 72.0)
        )
        Validation.validateMeasurement(entry)
        assertFalse(Validation.isAnomalous(entry))
    }

    @Test
    fun `heart rate below minimum throws`() {
        val entry = MeasurementEntry(
            type = MeasurementType.HEART_RATE,
            timestamp = Instant.now(),
            details = MeasurementDetails(primaryValue = 20.0)
        )
        assertThrows(IllegalArgumentException::class.java) {
            Validation.validateMeasurement(entry)
        }
    }

    @Test
    fun `blood pressure requires both values`() {
        val entry = MeasurementEntry(
            type = MeasurementType.BLOOD_PRESSURE,
            timestamp = Instant.now(),
            details = MeasurementDetails(primaryValue = 120.0)
        )
        assertThrows(IllegalArgumentException::class.java) {
            Validation.validateMeasurement(entry)
        }
    }

    @Test
    fun `sleep validation rejects negative duration`() {
        val entry = MeasurementEntry(
            type = MeasurementType.SLEEP,
            timestamp = Instant.now(),
            details = MeasurementDetails(durationMinutes = -5, endInstant = Instant.now().plusSeconds(3600)),
            startTimestamp = Instant.now()
        )
        assertThrows(IllegalArgumentException::class.java) {
            Validation.validateMeasurement(entry)
        }
    }

    @Test
    fun `pressure above threshold flagged as anomaly`() {
        val entry = MeasurementEntry(
            type = MeasurementType.BLOOD_PRESSURE,
            timestamp = Instant.now(),
            details = MeasurementDetails(primaryValue = 160.0, secondaryValue = 100.0)
        )
        Validation.validateMeasurement(entry)
        assertTrue(Validation.isAnomalous(entry))
    }
}

// app/src/test/java/com/example/healthcodex/NeuralForecastAnalyzerTest.kt
package com.example.healthcodex

import com.example.healthcodex.data.profile.Sex
import com.example.healthcodex.data.profile.Units
import com.example.healthcodex.data.profile.UserProfile
import com.example.healthcodex.feature.forecast.NeuralForecastAnalyzer
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NeuralForecastAnalyzerTest {

    @Test
    fun `returns null when context is missing`() {
        val analyzer = NeuralForecastAnalyzer(context = null)
        val result = analyzer.analyze(sampleProfile("p-null"))

        assertNull(result)
    }

    @Test
    fun `softmax output uses high risk class`() {
        val analyzer = NeuralForecastAnalyzer(
            context = null,
            inferenceOverride = { floatArrayOf(0.1f, 0.25f, 0.6f) }
        )

        val result = analyzer.analyze(sampleProfile("p-softmax"))

        assertNotNull(result)
        assertEquals(0.6, result, 0.001)
        assertTrue(result in 0.0..1.0)
    }

    @Test
    fun `single logit output is returned as probability`() {
        val analyzer = NeuralForecastAnalyzer(
            context = null,
            inferenceOverride = { floatArrayOf(0.42f) }
        )

        val result = analyzer.analyze(sampleProfile("p-logit"))

        assertNotNull(result)
        assertEquals(0.42, result, 0.001)
        assertTrue(result in 0.0..1.0)
    }

    @Test
    fun `incomplete profile still yields bounded probability when model is available`() {
        val analyzer = NeuralForecastAnalyzer(
            context = null,
            inferenceOverride = { floatArrayOf(0.15f, 0.15f, 0.7f) }
        )

        val emptyProfile = sampleProfile("p-empty").copy(
            birthDate = null,
            heightCm = null,
            weightKg = null,
            bpBaselineSystolic = null,
            bpBaselineDiastolic = null,
            restingHr = null,
            conditions = emptyList(),
            allergies = emptyList()
        )

        val result = analyzer.analyze(emptyProfile)

        assertNotNull(result)
        assertTrue(result in 0.0..1.0)
    }

    private fun sampleProfile(id: String): UserProfile = UserProfile(
        userId = id,
        fullName = "Тестовый Пользователь",
        birthDate = LocalDate.now().minusYears(35),
        sex = Sex.FEMALE,
        heightCm = 168,
        weightKg = 68f,
        units = Units.METRIC,
        conditions = listOf("гипертония"),
        allergies = listOf("пыль"),
        medications = emptyList(),
        restingHr = 72,
        bpBaselineSystolic = 122,
        bpBaselineDiastolic = 78,
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
        consentAccepted = true,
        consentVersion = "1.0",
        consentTimestamp = null
    )
}

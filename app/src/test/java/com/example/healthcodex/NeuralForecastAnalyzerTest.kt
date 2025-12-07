// app/src/test/java/com/example/healthcodex/NeuralForecastAnalyzerTest.kt
package com.example.healthcodex

import com.example.healthcodex.data.profile.Sex
import com.example.healthcodex.data.profile.Units
import com.example.healthcodex.data.profile.UserProfile
import com.example.healthcodex.feature.forecast.NeuralForecastAnalyzer
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertNull

class NeuralForecastAnalyzerTest {

    @Test
    fun `returns null when context is missing`() {
        val analyzer = NeuralForecastAnalyzer(null)
        val profile = UserProfile(
            userId = "p1",
            fullName = "Test User",
            birthDate = LocalDate.now().minusYears(40),
            sex = Sex.FEMALE,
            heightCm = 170,
            weightKg = 70f,
            units = Units.METRIC,
            conditions = listOf("гипертония"),
            allergies = emptyList(),
            medications = emptyList(),
            restingHr = 70,
            bpBaselineSystolic = 125,
            bpBaselineDiastolic = 80,
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

        val result = analyzer.analyze(profile)

        assertNull(result)
    }

    @Test
    fun `top factors list is deterministic`() {
        val analyzer = NeuralForecastAnalyzer(null)
        val profile = UserProfile(
            userId = "p2",
            fullName = "Demo",
            birthDate = LocalDate.now().minusYears(20),
            sex = Sex.OTHER,
            heightCm = 180,
            weightKg = 90f,
            units = Units.METRIC,
            conditions = emptyList(),
            allergies = emptyList(),
            medications = emptyList(),
            restingHr = 60,
            bpBaselineSystolic = 110,
            bpBaselineDiastolic = 70,
            hrHigh = null,
            bpSysHigh = null,
            bpDiaHigh = null,
            emergencyName = null,
            emergencyPhone = null,
            doctorName = null,
            doctorPhone = null,
            bleDeviceName = null,
            bleDeviceAddress = null,
            shareWithDoctor = true,
            consentAccepted = true,
            consentVersion = "1.0",
            consentTimestamp = null
        )

        val result = analyzer.analyze(profile)

        assertNull(result)
    }
}

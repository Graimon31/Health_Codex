// app/src/test/java/com/example/healthcodex/ForecastAnalyzerTest.kt
package com.example.healthcodex

import com.example.healthcodex.data.profile.Medication
import com.example.healthcodex.data.profile.Sex
import com.example.healthcodex.data.profile.Units
import com.example.healthcodex.data.profile.UserProfile
import com.example.healthcodex.feature.forecast.ForecastAnalyzer
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tests for the forecast analytics helper. */
class ForecastAnalyzerTest {

    @Test
    fun forecastRequiresProfileWhenNoData() {
        val forecast = ForecastAnalyzer.analyze(null)

        assertTrue(forecast.profileMissing)
        assertTrue(forecast.riskInsights.isNotEmpty())
        assertTrue(forecast.recommendations.any { it.contains("Профиль", ignoreCase = true) })
    }

    @Test
    fun forecastPositiveForHealthyProfile() {
        val profile = UserProfile(
            userId = "u1",
            fullName = "Иван Тестовый",
            birthDate = LocalDate.now().minusYears(30),
            sex = Sex.MALE,
            heightCm = 180,
            weightKg = 75f,
            units = Units.METRIC,
            conditions = emptyList(),
            allergies = emptyList(),
            medications = emptyList<Medication>(),
            restingHr = 65,
            bpBaselineSystolic = 118,
            bpBaselineDiastolic = 76,
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

        val forecast = ForecastAnalyzer.analyze(profile)

        assertFalse(forecast.profileMissing)
        assertTrue(forecast.riskInsights.isEmpty())
        assertTrue(forecast.positiveInsights.isNotEmpty())
        assertEquals("Прогноз благоприятный", forecast.headline)
    }
}

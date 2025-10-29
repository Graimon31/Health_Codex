// app/src/test/java/com/example/healthcodex/ProfileValidationTest.kt
package com.example.healthcodex

import com.example.healthcodex.data.profile.Medication
import com.example.healthcodex.data.profile.Sex
import com.example.healthcodex.data.profile.Units
import com.example.healthcodex.data.profile.UserProfile
import com.example.healthcodex.util.Formatters
import com.example.healthcodex.util.Validation
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ProfileValidationTest {

    private fun baseProfile(): UserProfile = UserProfile(
        userId = "user",
        fullName = "Тест",
        birthDate = LocalDate.now().minusYears(30),
        sex = Sex.MALE,
        heightCm = 180,
        weightKg = 80f,
        units = Units.METRIC,
        conditions = listOf("гипертония"),
        allergies = listOf("пыльца"),
        medications = listOf(Medication("Препарат", "5мг", "утром")),
        restingHr = 60,
        bpBaselineSystolic = 120,
        bpBaselineDiastolic = 80,
        hrHigh = 150,
        bpSysHigh = 160,
        bpDiaHigh = 100,
        emergencyName = "Родственник",
        emergencyPhone = "+79990000000",
        doctorName = "Доктор",
        doctorPhone = "+79991112233",
        bleDeviceName = "HealthBand",
        bleDeviceAddress = "AA:BB:CC:DD:EE",
        shareWithDoctor = true,
        consentAccepted = true,
        consentVersion = "1.0",
        consentTimestamp = Instant.now()
    )

    @Test
    fun `valid profile passes validation`() {
        Validation.validateProfile(baseProfile())
    }

    @Test
    fun `height outside range fails`() {
        val profile = baseProfile().copy(heightCm = 20)
        assertThrows(IllegalArgumentException::class.java) {
            Validation.validateProfile(profile)
        }
    }

    @Test
    fun `bmi calculation`() {
        val bmi = Formatters.calculateBmi(heightCm = 180, weightKg = 81f)
        assertEquals("25.0", bmi)
    }

    @Test
    fun `target heart zone`() {
        val zone = Formatters.targetHrZone(40)
        assertEquals(90 to 153, zone)
    }
}

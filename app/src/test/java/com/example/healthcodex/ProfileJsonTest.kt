// app/src/test/java/com/example/healthcodex/ProfileJsonTest.kt
package com.example.healthcodex

import com.example.healthcodex.data.profile.Medication
import com.example.healthcodex.data.profile.Sex
import com.example.healthcodex.data.profile.Units
import com.example.healthcodex.data.profile.UserProfile
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileJsonTest {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val adapter = moshi.adapter(UserProfile::class.java)

    private fun sampleProfile(): UserProfile = UserProfile(
        userId = "user",
        fullName = "Test User",
        birthDate = LocalDate.parse("1990-01-01"),
        sex = Sex.FEMALE,
        heightCm = 170,
        weightKg = 65f,
        units = Units.METRIC,
        conditions = listOf("диабет"),
        allergies = listOf("арахис"),
        medications = listOf(Medication("Metformin", "500mg", "дважды в день")),
        restingHr = 70,
        bpBaselineSystolic = 115,
        bpBaselineDiastolic = 75,
        hrHigh = 160,
        bpSysHigh = 150,
        bpDiaHigh = 95,
        emergencyName = "ICE",
        emergencyPhone = "+79991234567",
        doctorName = "Dr. Smith",
        doctorPhone = "1234567",
        bleDeviceName = "Tracker",
        bleDeviceAddress = "00:11:22",
        shareWithDoctor = false,
        consentAccepted = true,
        consentVersion = "1.0",
        consentTimestamp = Instant.parse("2024-01-01T00:00:00Z")
    )

    @Test
    fun `serialize and deserialize profile`() {
        val profile = sampleProfile()
        val json = adapter.toJson(profile)
        val restored = adapter.fromJson(json)
        assertEquals(profile, restored)
    }
}

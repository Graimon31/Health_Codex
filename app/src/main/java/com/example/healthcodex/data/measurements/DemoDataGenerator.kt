// app/src/main/java/com/example/healthcodex/data/measurements/DemoDataGenerator.kt
package com.example.healthcodex.data.measurements

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlin.math.sin
import kotlin.math.PI
import kotlin.random.Random

/**
 * Generates realistic synthetic measurement entries tagged with "demo"
 * for a 30-day window ending today. All entries can be identified and
 * removed by filtering on the "demo" tag.
 */
object DemoDataGenerator {

    private val DEMO_TAG = "demo"

    private val WEARABLE_DEVICE = Triple("HealthBand 7", "AA:BB:01:02:03:04", MeasurementDeviceType.WEARABLE)
    private val CARDIO_DEVICE = Triple("CardioTrack Pro", "AA:BB:05:06:07:08", MeasurementDeviceType.WEARABLE)
    private val SCALE_DEVICE = Triple("Smart Scale X", "C1:D2:E3:F4:55:66", MeasurementDeviceType.NON_WEARABLE)

    fun generate(seed: Long = 42L): List<MeasurementEntry> {
        val rng = Random(seed)
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now()
        val entries = mutableListOf<MeasurementEntry>()

        // Starting weight for weight delta simulation
        var baseWeight = 68.0 + rng.nextDouble() * 4.0

        for (dayOffset in 29 downTo 0) {
            val date = today.minusDays(dayOffset.toLong())
            // Phase for circadian simulation (0..2π over 30 days)
            val phase = (29 - dayOffset) / 29.0 * 2.0 * PI

            // -- Heart Rate: 3-5 readings per day from wearable --
            val hrCount = 3 + rng.nextInt(3)
            val hrTimes = listOf(7, 10, 14, 18, 21).shuffled(rng).take(hrCount)
            for (hour in hrTimes) {
                val bpm = 62.0 + 18.0 * sin(phase) + rng.nextDouble() * 10.0 - 5.0
                entries += entry(
                    type = MeasurementType.HEART_RATE,
                    date = date,
                    time = LocalTime.of(hour, rng.nextInt(60)),
                    primary = bpm.coerceIn(50.0, 110.0),
                    device = WEARABLE_DEVICE,
                    confidence = if (bpm > 100 || bpm < 55) MeasurementConfidence.LOW else MeasurementConfidence.HIGH
                )
            }

            // -- Steps: 1 summary per day from wearable --
            val steps = 5000.0 + 5000.0 * sin(phase + 1.0) + rng.nextDouble() * 2000.0 - 1000.0
            entries += entry(
                type = MeasurementType.STEPS,
                date = date,
                time = LocalTime.of(23, 50),
                primary = steps.coerceIn(1000.0, 15000.0),
                device = WEARABLE_DEVICE
            )

            // -- Calories: 1 summary per day --
            val kcal = 1800.0 + 400.0 * sin(phase) + rng.nextDouble() * 200.0 - 100.0
            entries += entry(
                type = MeasurementType.CALORIES,
                date = date,
                time = LocalTime.of(23, 55),
                primary = kcal.coerceIn(1200.0, 3000.0),
                device = WEARABLE_DEVICE
            )

            // -- Blood Pressure: 1-2 readings per day from cardio device --
            val bpCount = 1 + rng.nextInt(2)
            val bpHours = listOf(8, 12, 20).shuffled(rng).take(bpCount)
            for (hour in bpHours) {
                val sys = 115.0 + 10.0 * sin(phase - 0.5) + rng.nextDouble() * 8.0 - 4.0
                val dia = 72.0 + 6.0 * sin(phase - 0.5) + rng.nextDouble() * 6.0 - 3.0
                entries += entry(
                    type = MeasurementType.BLOOD_PRESSURE,
                    date = date,
                    time = LocalTime.of(hour, rng.nextInt(60)),
                    primary = sys.coerceIn(100.0, 150.0),
                    secondary = dia.coerceIn(60.0, 100.0),
                    device = CARDIO_DEVICE,
                    confidence = if (sys > 140 || dia > 90) MeasurementConfidence.MEDIUM else MeasurementConfidence.HIGH
                )
            }

            // -- Weight: every 2nd day from smart scale --
            if (dayOffset % 2 == 0) {
                baseWeight += rng.nextDouble() * 0.3 - 0.15
                entries += entry(
                    type = MeasurementType.WEIGHT,
                    date = date,
                    time = LocalTime.of(7, rng.nextInt(30)),
                    primary = baseWeight.coerceIn(50.0, 150.0),
                    device = SCALE_DEVICE
                )
            }

            // -- SpO2: 2 readings per day from wearable --
            for (hour in listOf(8, 22)) {
                val spo2 = 96.0 + rng.nextDouble() * 3.0
                entries += entry(
                    type = MeasurementType.OXYGEN,
                    date = date,
                    time = LocalTime.of(hour, rng.nextInt(60)),
                    primary = spo2.coerceIn(90.0, 100.0),
                    device = WEARABLE_DEVICE,
                    confidence = if (spo2 < 93) MeasurementConfidence.LOW else MeasurementConfidence.HIGH
                )
            }

            // -- Sleep: nightly from wearable (stored on morning of wake-up) --
            val sleepHours = 5.5 + 2.5 * sin(phase + 0.8) + rng.nextDouble() * 0.8 - 0.4
            val sleepMinutes = (sleepHours * 60).toInt().coerceIn(180, 540)
            val wakeTime = LocalTime.of(6 + rng.nextInt(3), rng.nextInt(60))
            val sleepStart = date.atTime(wakeTime)
                .minusMinutes(sleepMinutes.toLong())
                .atZone(zone).toInstant()
            val sleepEnd = date.atTime(wakeTime).atZone(zone).toInstant()
            entries += MeasurementEntry(
                type = MeasurementType.SLEEP,
                timestamp = sleepEnd,
                startTimestamp = sleepStart,
                source = MeasurementSource.DEVICE,
                deviceName = WEARABLE_DEVICE.first,
                deviceAddress = WEARABLE_DEVICE.second,
                deviceType = WEARABLE_DEVICE.third,
                confidence = MeasurementConfidence.HIGH,
                details = MeasurementDetails(
                    durationMinutes = sleepMinutes,
                    startInstant = sleepStart,
                    endInstant = sleepEnd
                ),
                tags = listOf(DEMO_TAG)
            )

            // -- Respiratory: 1 reading per day --
            val resp = 15.0 + 2.0 * sin(phase) + rng.nextDouble() * 2.0 - 1.0
            entries += entry(
                type = MeasurementType.RESPIRATORY,
                date = date,
                time = LocalTime.of(9, rng.nextInt(60)),
                primary = resp.coerceIn(10.0, 25.0),
                device = CARDIO_DEVICE
            )
        }

        return entries
    }

    private fun entry(
        type: MeasurementType,
        date: LocalDate,
        time: LocalTime,
        primary: Double? = null,
        secondary: Double? = null,
        tertiary: Double? = null,
        device: Triple<String, String, MeasurementDeviceType>,
        confidence: MeasurementConfidence = MeasurementConfidence.HIGH
    ): MeasurementEntry {
        val zone = ZoneId.systemDefault()
        return MeasurementEntry(
            type = type,
            timestamp = date.atTime(time).atZone(zone).toInstant(),
            source = MeasurementSource.DEVICE,
            deviceName = device.first,
            deviceAddress = device.second,
            deviceType = device.third,
            confidence = confidence,
            details = MeasurementDetails(
                primaryValue = primary,
                secondaryValue = secondary,
                tertiaryValue = tertiary
            ),
            tags = listOf(DEMO_TAG)
        )
    }
}

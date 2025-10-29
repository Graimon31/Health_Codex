// app/src/main/java/com/example/healthcodex/util/Validation.kt
package com.example.healthcodex.util

import com.example.healthcodex.data.measurements.MeasurementConfidence
import com.example.healthcodex.data.measurements.MeasurementEntry
import com.example.healthcodex.data.measurements.MeasurementType
import com.example.healthcodex.data.profile.UserProfile
import java.time.LocalDate
import java.time.Period

/**
 * Domain validation helpers used by the profile feature.
 */
object Validation {
    private const val MIN_AGE = 0
    private const val MAX_AGE = 120
    private const val MIN_HEIGHT_CM = 50
    private const val MAX_HEIGHT_CM = 250
    private const val MIN_WEIGHT_KG = 20f
    private const val MAX_WEIGHT_KG = 400f
    private const val MIN_HR = 30
    private const val MAX_HR = 220
    private const val MIN_BP_SYS = 70
    private const val MAX_BP_SYS = 250
    private const val MIN_BP_DIA = 40
    private const val MAX_BP_DIA = 150
    private const val MIN_SPO2 = 70
    private const val MAX_SPO2 = 100
    private const val MIN_RESPIRATORY = 6
    private const val MAX_RESPIRATORY = 40

    fun validateProfile(profile: UserProfile) {
        profile.birthDate?.let { birthDate ->
            val age = calculateAge(birthDate)
            require(age in MIN_AGE..MAX_AGE) { "Возраст вне допустимого диапазона" }
        }

        profile.heightCm?.let {
            require(it in MIN_HEIGHT_CM..MAX_HEIGHT_CM) { "Рост вне диапазона" }
        }

        profile.weightKg?.let {
            require(it in MIN_WEIGHT_KG..MAX_WEIGHT_KG) { "Вес вне диапазона" }
        }

        listOfNotNull(profile.restingHr, profile.hrHigh).forEach { hr ->
            require(hr in MIN_HR..MAX_HR) { "Пульс вне диапазона" }
        }

        listOfNotNull(profile.bpBaselineSystolic, profile.bpSysHigh).forEach { value ->
            require(value in MIN_BP_SYS..MAX_BP_SYS) { "Систолическое давление вне диапазона" }
        }

        listOfNotNull(profile.bpBaselineDiastolic, profile.bpDiaHigh).forEach { value ->
            require(value in MIN_BP_DIA..MAX_BP_DIA) { "Диастолическое давление вне диапазона" }
        }

        profile.emergencyPhone?.let {
            require(it.length >= 5) { "Некорректный номер ICE" }
        }
        profile.doctorPhone?.let {
            require(it.length >= 5) { "Некорректный номер врача" }
        }
    }

    fun calculateAge(birthDate: LocalDate): Int =
        Period.between(birthDate, LocalDate.now()).years.coerceAtLeast(0)

    fun validateMeasurement(entry: MeasurementEntry) {
        val details = entry.details
        when (entry.type) {
            MeasurementType.HEART_RATE -> {
                val bpm = details.primaryValue?.toInt()
                require(bpm != null) { "Укажите пульс" }
                require(bpm in MIN_HR..MAX_HR) { "Пульс вне диапазона" }
            }
            MeasurementType.STEPS -> {
                val steps = details.primaryValue?.toLong() ?: 0L
                require(steps >= 0) { "Количество шагов не может быть отрицательным" }
            }
            MeasurementType.CALORIES -> {
                val calories = details.primaryValue?.toInt() ?: 0
                require(calories >= 0) { "Калории не могут быть отрицательными" }
            }
            MeasurementType.BLOOD_PRESSURE -> {
                val systolic = details.primaryValue?.toInt()
                val diastolic = details.secondaryValue?.toInt()
                require(systolic != null && diastolic != null) { "Заполните давление" }
                require(systolic in MIN_BP_SYS..MAX_BP_SYS) { "Систолическое вне диапазона" }
                require(diastolic in MIN_BP_DIA..MAX_BP_DIA) { "Диастолическое вне диапазона" }
                details.tertiaryValue?.toInt()?.let { pulse ->
                    require(pulse in MIN_HR..MAX_HR) { "Пульс вне диапазона" }
                }
            }
            MeasurementType.WEIGHT -> {
                val weight = details.primaryValue
                require(weight != null) { "Укажите вес" }
                require(weight in MIN_WEIGHT_KG.toDouble()..MAX_WEIGHT_KG.toDouble()) { "Вес вне диапазона" }
            }
            MeasurementType.OXYGEN -> {
                val spo2 = details.primaryValue?.toInt()
                require(spo2 != null) { "Укажите SpO₂" }
                require(spo2 in MIN_SPO2..MAX_SPO2) { "SpO₂ вне диапазона" }
            }
            MeasurementType.SLEEP -> {
                val start = entry.startTimestamp ?: details.startInstant
                val end = details.endInstant ?: entry.timestamp
                require(start != null && end != null) { "Укажите период сна" }
                require(end.isAfter(start)) { "Окончание сна должно быть позже начала" }
                details.durationMinutes?.let { duration ->
                    require(duration > 0) { "Длительность сна должна быть положительной" }
                }
            }
            MeasurementType.RESPIRATORY -> {
                val rate = details.primaryValue?.toInt()
                if (rate != null) {
                    require(rate in MIN_RESPIRATORY..MAX_RESPIRATORY) { "Дыхание вне диапазона" }
                } else {
                    require(!details.statusText.isNullOrBlank()) { "Укажите значение или статус" }
                }
            }
        }
    }

    fun isAnomalous(entry: MeasurementEntry): Boolean {
        val d = entry.details
        val anomaly = when (entry.type) {
            MeasurementType.HEART_RATE -> d.primaryValue?.let { it < 50 || it > 120 } == true
            MeasurementType.STEPS -> false
            MeasurementType.CALORIES -> false
            MeasurementType.BLOOD_PRESSURE -> {
                val sys = d.primaryValue
                val dia = d.secondaryValue
                sys != null && sys > 140 || dia != null && dia > 90
            }
            MeasurementType.WEIGHT -> false
            MeasurementType.OXYGEN -> d.primaryValue?.let { it < 92 } == true
            MeasurementType.SLEEP -> d.durationMinutes?.let { it < 300 || it > 600 } == true
            MeasurementType.RESPIRATORY -> d.primaryValue?.let { it < 10 || it > 24 } == true
        }
        val noteFlag = entry.tags.any { it.contains("аном", ignoreCase = true) }
        return anomaly || noteFlag || entry.confidence == MeasurementConfidence.LOW
    }
}

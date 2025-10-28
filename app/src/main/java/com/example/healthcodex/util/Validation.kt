// app/src/main/java/com/example/healthcodex/util/Validation.kt
package com.example.healthcodex.util

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
}

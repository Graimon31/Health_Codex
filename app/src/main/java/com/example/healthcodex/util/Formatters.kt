// app/src/main/java/com/example/healthcodex/util/Formatters.kt
package com.example.healthcodex.util

import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import kotlin.math.pow
import kotlin.math.round

/**
 * Formatting helpers for profile UI.
 */
object Formatters {
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    fun formatDate(date: LocalDate?): String = date?.format(dateFormatter) ?: "—"

    fun formatPhone(phone: String?): String = phone?.trim().takeUnless { it.isNullOrEmpty() } ?: "—"

    fun calculateBmi(heightCm: Int?, weightKg: Float?): String {
        if (heightCm == null || weightKg == null || heightCm <= 0) return "—"
        val heightM = heightCm / 100f
        val bmi = weightKg / heightM.pow(2)
        return String.format("%.1f", bmi)
    }

    fun targetHrZone(age: Int): Pair<Int, Int> {
        val maxHr = 220 - age
        val lower = (maxHr * 0.5).toInt()
        val upper = (maxHr * 0.85).toInt()
        return lower to upper
    }
}

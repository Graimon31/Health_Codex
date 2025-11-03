// app/src/main/java/com/example/healthcodex/util/Formatters.kt
package com.example.healthcodex.util

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.Period
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.pow
import kotlin.math.round

/**
 * Formatting helpers for profile UI.
 */
object Formatters {
    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    private val headerFormatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("EEE, d MMMM", Locale("ru")).withLocale(Locale("ru"))
    private val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

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

    fun formatTime(time: LocalTime): String = timeFormatter.format(time)

    fun formatInstant(instant: Instant): String =
        timeFormatter.format(instant.atZone(ZoneId.systemDefault()))

    fun formatHeaderDate(date: LocalDate): String =
        headerFormatter.format(date).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("ru")) else it.toString() }

    fun formatDurationMinutes(minutes: Int?): String {
        if (minutes == null) return "—"
        val duration = Duration.ofMinutes(minutes.toLong())
        val hours = duration.toHours().toInt()
        val mins = (duration.toMinutes() % 60).toInt()
        return buildString {
            if (hours > 0) {
                append(hours)
                append(" ч ")
            }
            append(mins)
            append(" м")
        }
    }

    fun formatDouble(value: Double?, decimals: Int = 1): String =
        value?.let { String.format(Locale("ru"), "%0.${decimals}f", it) } ?: "—"

    fun formatInt(value: Number?): String = value?.toLong()?.let { String.format(Locale("ru"), "%,d", it).replace(',', ' ') } ?: "—"
}

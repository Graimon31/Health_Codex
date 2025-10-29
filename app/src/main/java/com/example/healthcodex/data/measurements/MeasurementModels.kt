// app/src/main/java/com/example/healthcodex/data/measurements/MeasurementModels.kt
package com.example.healthcodex.data.measurements

import androidx.annotation.StringRes
import com.example.healthcodex.R
import com.squareup.moshi.JsonClass
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/**
 * Supported measurement kinds shown on the measurements screen.
 */
enum class MeasurementType(
    @StringRes val titleRes: Int,
    @StringRes val subtitleRes: Int
) {
    HEART_RATE(R.string.measure_type_pulse, R.string.measure_subtitle_realtime),
    STEPS(R.string.measure_type_steps, R.string.measure_subtitle_today),
    CALORIES(R.string.measure_type_calories, R.string.measure_subtitle_today),
    BLOOD_PRESSURE(R.string.measure_type_bp, R.string.measure_subtitle_latest),
    WEIGHT(R.string.measure_type_weight, R.string.measure_subtitle_latest),
    OXYGEN(R.string.measure_type_spo2, R.string.measure_subtitle_latest),
    SLEEP(R.string.measure_type_sleep, R.string.measure_subtitle_night),
    RESPIRATORY(R.string.measure_type_resp, R.string.measure_subtitle_latest)
}

/**
 * Source of the measurement.
 */
enum class MeasurementSource(@StringRes val titleRes: Int) {
    DEVICE(R.string.measure_source_device),
    MANUAL(R.string.measure_source_manual)
}

/**
 * Signal quality used to highlight anomalies.
 */
enum class MeasurementConfidence(@StringRes val titleRes: Int) {
    HIGH(R.string.measure_confidence_high),
    MEDIUM(R.string.measure_confidence_medium),
    LOW(R.string.measure_confidence_low)
}

/**
 * Additional structured data required to present a measurement.
 */
@JsonClass(generateAdapter = true)
data class MeasurementDetails(
    val primaryValue: Double? = null,
    val secondaryValue: Double? = null,
    val tertiaryValue: Double? = null,
    val durationMinutes: Int? = null,
    val statusText: String? = null,
    val startInstant: Instant? = null,
    val endInstant: Instant? = null
)

/**
 * Domain model representing a persisted measurement entry.
 */
@JsonClass(generateAdapter = true)
data class MeasurementEntry(
    val id: Long = 0,
    val type: MeasurementType,
    val timestamp: Instant,
    val startTimestamp: Instant? = null,
    val source: MeasurementSource = MeasurementSource.DEVICE,
    val deviceName: String? = null,
    val deviceAddress: String? = null,
    val note: String? = null,
    val confidence: MeasurementConfidence = MeasurementConfidence.HIGH,
    val details: MeasurementDetails = MeasurementDetails(),
    val tags: List<String> = emptyList()
) {
    /**
     * Helper that determines whether the entry falls inside the provided date range.
     */
    fun inRange(start: Instant, end: Instant): Boolean =
        timestamp >= start && timestamp <= end

    /**
     * Calculates the local date for grouping purposes.
     */
    fun localDate(): LocalDate = timestamp.atZone(java.time.ZoneId.systemDefault()).toLocalDate()

    /**
     * Calculates the local time for list presentation.
     */
    fun localTime(): LocalTime = timestamp.atZone(java.time.ZoneId.systemDefault()).toLocalTime()
}

/**
 * Filter describing the currently selected measurement subset.
 */
data class MeasurementFilter(
    val period: MeasurementPeriod = MeasurementPeriod.Today,
    val selectedTypes: Set<MeasurementType> = MeasurementType.values().toSet(),
    val source: MeasurementSourceFilter = MeasurementSourceFilter.All,
    val deviceName: String? = null,
    val onlyAnomalies: Boolean = false,
    val ranges: Map<MeasurementType, MeasurementValueRange> = emptyMap(),
    val query: String = ""
)

sealed class MeasurementSourceFilter {
    data object All : MeasurementSourceFilter()
    data class Only(val source: MeasurementSource) : MeasurementSourceFilter()
}

/**
 * Period picker backing the UI segmented control.
 */
sealed class MeasurementPeriod(val labelRes: Int) {
    data object Today : MeasurementPeriod(R.string.measure_period_today)
    data object Week : MeasurementPeriod(R.string.measure_period_week)
    data object Month : MeasurementPeriod(R.string.measure_period_month)
    data class Custom(val start: LocalDate, val end: LocalDate) : MeasurementPeriod(R.string.measure_period_custom)

    fun range(): Pair<Instant, Instant> {
        val zone = java.time.ZoneId.systemDefault()
        return when (this) {
            Today -> {
                val now = LocalDate.now()
                now.atStartOfDay(zone).toInstant() to now.plusDays(1).atStartOfDay(zone).minusNanos(1).toInstant()
            }
            Week -> {
                val now = LocalDate.now()
                val start = now.minusDays(6)
                start.atStartOfDay(zone).toInstant() to now.plusDays(1).atStartOfDay(zone).minusNanos(1).toInstant()
            }
            Month -> {
                val now = LocalDate.now()
                val start = now.minusDays(29)
                start.atStartOfDay(zone).toInstant() to now.plusDays(1).atStartOfDay(zone).minusNanos(1).toInstant()
            }
            is Custom -> {
                start.atStartOfDay(zone).toInstant() to end.plusDays(1).atStartOfDay(zone).minusNanos(1).toInstant()
            }
        }
    }
}

/**
 * Range filter applied to the measurement primary value.
 */
data class MeasurementValueRange(
    val min: Double? = null,
    val max: Double? = null
)

/**
 * Aggregated summary metrics shown above the feed.
 */
data class MeasurementSummary(
    val averageHr: Double?,
    val steps: Long,
    val calories: Long,
    val pressure: Pair<Double, Double>?,
    val weightDelta: Double?,
    val averageSpo2: Double?,
    val sleepMinutes: Int?,
    val respiratoryRate: Double?
)

// app/src/main/java/com/example/healthcodex/feature/forecast/HealthForecast.kt
package com.example.healthcodex.feature.forecast

/**
 * Represents the summarized forecast for the current user state.
 */
data class HealthForecast(
    val headline: String = "",
    val detail: String = "",
    val positiveInsights: List<WellnessInsight> = emptyList(),
    val riskInsights: List<WellnessInsight> = emptyList(),
    val recommendations: List<String> = emptyList(),
    val profileMissing: Boolean = false
)

/** Insight for a specific health aspect. */
data class WellnessInsight(
    val title: String,
    val message: String,
    val severity: InsightSeverity
)

/** Severity of an insight that affects the icon tint on the UI. */
enum class InsightSeverity {
    POSITIVE,
    WARNING,
    CRITICAL
}

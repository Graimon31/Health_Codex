// app/src/main/java/com/example/healthcodex/feature/forecast/ForecastAnalyzer.kt
package com.example.healthcodex.feature.forecast

import com.example.healthcodex.data.profile.UserProfile
import com.example.healthcodex.util.Validation
import kotlin.math.pow

/**
 * Produces a lightweight health forecast based on the stored profile metrics.
 */
object ForecastAnalyzer {
    private const val BMI_NORMAL_LOW = 18.5
    private const val BMI_NORMAL_HIGH = 24.9
    private const val BMI_OVERWEIGHT = 30.0

    fun analyze(profile: UserProfile?): HealthForecast {
        if (profile == null) {
            return HealthForecast(
                headline = "Недостаточно данных",
                detail = "Заполните раздел \"Профиль\", чтобы рассчитать персональный прогноз.",
                riskInsights = listOf(
                    WellnessInsight(
                        title = "Нет исходных данных",
                        message = "Без роста, веса и базовых показателей прогноз невозможен.",
                        severity = InsightSeverity.WARNING
                    )
                ),
                recommendations = listOf(
                    "Откройте вкладку \"Профиль\" и заполните основные сведения о себе."
                ),
                profileMissing = true
            )
        }

        val positive = mutableListOf<WellnessInsight>()
        val risks = mutableListOf<WellnessInsight>()
        val recommendations = mutableListOf<String>()

        val age = profile.birthDate?.let { Validation.calculateAge(it) }
        val bmiValue = calculateBmiValue(profile.heightCm, profile.weightKg)

        age?.let {
            when {
                it < 45 -> positive += WellnessInsight(
                    title = "Возраст",
                    message = "Возрастной риск сердечно-сосудистых осложнений умеренный.",
                    severity = InsightSeverity.POSITIVE
                )
                it in 45..64 -> risks += WellnessInsight(
                    title = "Возраст",
                    message = "Повышенный возраст требует регулярных обследований.",
                    severity = InsightSeverity.WARNING
                ).also {
                    recommendations += "Проходите диспансеризацию не реже раза в год."
                }
                else -> risks += WellnessInsight(
                    title = "Возраст",
                    message = "Высокий возраст увеличивает вероятность хронических осложнений.",
                    severity = InsightSeverity.CRITICAL
                ).also {
                    recommendations += "Обсудите со своим врачом план регулярных проверок и контроля терапии."
                }
            }
        }

        bmiValue?.let { value ->
            when {
                value < BMI_NORMAL_LOW -> {
                    risks += WellnessInsight(
                        title = "Индекс массы тела",
                        message = "Наблюдается дефицит массы тела (BMI %.1f).".format(value),
                        severity = InsightSeverity.WARNING
                    )
                    recommendations += "Скорректируйте рацион и обсудите набор веса с врачом или диетологом."
                }
                value <= BMI_NORMAL_HIGH -> {
                    positive += WellnessInsight(
                        title = "Индекс массы тела",
                        message = "Вес в пределах нормы (BMI %.1f).".format(value),
                        severity = InsightSeverity.POSITIVE
                    )
                }
                value < BMI_OVERWEIGHT -> {
                    risks += WellnessInsight(
                        title = "Индекс массы тела",
                        message = "Наблюдается избыток массы тела (BMI %.1f).".format(value),
                        severity = InsightSeverity.WARNING
                    )
                    recommendations += "Увеличьте физическую активность и пересмотрите питание, чтобы снизить вес."
                }
                else -> {
                    risks += WellnessInsight(
                        title = "Индекс массы тела",
                        message = "Ожирение повышает нагрузку на сердце и суставы (BMI %.1f).".format(value),
                        severity = InsightSeverity.CRITICAL
                    )
                    recommendations += "Проконсультируйтесь с врачом по поводу программы снижения веса."
                }
            }
        }

        evaluateBloodPressure(profile)?.let { insight ->
            when (insight.severity) {
                InsightSeverity.POSITIVE -> positive += insight
                InsightSeverity.WARNING, InsightSeverity.CRITICAL -> {
                    risks += insight
                    val message = if (insight.severity == InsightSeverity.CRITICAL) {
                        "Обратитесь к врачу, чтобы скорректировать терапию давления."
                    } else {
                        "Отслеживайте давление и сократите потребление соли и стрессы."
                    }
                    recommendations += message
                }
            }
        }

        evaluateRestingHr(profile)?.let { insight ->
            when (insight.severity) {
                InsightSeverity.POSITIVE -> positive += insight
                InsightSeverity.WARNING, InsightSeverity.CRITICAL -> {
                    risks += insight
                    recommendations += "Отслеживайте пульс в покое и при отклонениях обсудите их с врачом."
                }
            }
        }

        if (profile.conditions.isNotEmpty()) {
            risks += WellnessInsight(
                title = "Хронические состояния",
                message = "Зафиксированы диагнозы: ${profile.conditions.joinToString()}.",
                severity = InsightSeverity.WARNING
            )
            recommendations += "Соблюдайте назначенное лечение и записывайте изменения самочувствия."
        } else {
            positive += WellnessInsight(
                title = "Хронические заболевания",
                message = "Серьёзных диагнозов не указано.",
                severity = InsightSeverity.POSITIVE
            )
        }

        if (profile.allergies.isNotEmpty()) {
            risks += WellnessInsight(
                title = "Аллергии",
                message = "Учитывайте аллергии: ${profile.allergies.joinToString()}.",
                severity = InsightSeverity.WARNING
            )
            recommendations += "Всегда сообщайте об аллергиях медицинскому персоналу."
        }

        if (profile.shareWithDoctor) {
            positive += WellnessInsight(
                title = "Совместное ведение",
                message = "Профиль синхронизируется с лечащим врачом.",
                severity = InsightSeverity.POSITIVE
            )
        }

        val riskScore = risks.sumOf { insight ->
            when (insight.severity) {
                InsightSeverity.POSITIVE -> 0
                InsightSeverity.WARNING -> 1
                InsightSeverity.CRITICAL -> 2
            }
        }

        val headline = when {
            riskScore >= 5 -> "Прогноз требует немедленного внимания"
            riskScore in 3..4 -> "Прогноз настораживает"
            riskScore in 1..2 -> "Умеренный прогноз"
            else -> "Прогноз благоприятный"
        }

        val detail = when {
            riskScore >= 5 -> "Несколько факторов риска усиливают вероятность ухудшения состояния в ближайшие месяцы."
            riskScore in 3..4 -> "Часть показателей выходит за рамки нормы — стоит пересмотреть образ жизни и план лечения."
            riskScore in 1..2 -> "Отдельные показатели требуют контроля, но серьёзных угроз не выявлено."
            else -> "Показатели в норме, сохраняйте текущий образ жизни и наблюдение."
        }

        if (recommendations.isEmpty()) {
            recommendations += "Продолжайте придерживаться текущих привычек и контролируйте показатели раз в несколько недель."
        }

        return HealthForecast(
            headline = headline,
            detail = detail,
            positiveInsights = positive.sortedBy { it.title },
            riskInsights = risks.sortedByDescending { it.severity.ordinal },
            recommendations = recommendations.distinct(),
            profileMissing = false
        )
    }

    private fun calculateBmiValue(heightCm: Int?, weightKg: Float?): Double? {
        if (heightCm == null || weightKg == null || heightCm <= 0) return null
        val heightM = heightCm / 100.0
        return weightKg / heightM.pow(2)
    }

    private fun evaluateBloodPressure(profile: UserProfile): WellnessInsight? {
        val sys = profile.bpBaselineSystolic
        val dia = profile.bpBaselineDiastolic
        if (sys == null || dia == null) return null
        return when {
            sys >= 140 || dia >= 90 -> WellnessInsight(
                title = "Артериальное давление",
                message = "Базовые значения повышены (${sys}/${dia} мм рт. ст.).",
                severity = InsightSeverity.CRITICAL
            )
            sys in 120..139 || dia in 80..89 -> WellnessInsight(
                title = "Артериальное давление",
                message = "Давление на верхней границе нормы (${sys}/${dia}).",
                severity = InsightSeverity.WARNING
            )
            else -> WellnessInsight(
                title = "Артериальное давление",
                message = "Давление в пределах нормы (${sys}/${dia}).",
                severity = InsightSeverity.POSITIVE
            )
        }
    }

    private fun evaluateRestingHr(profile: UserProfile): WellnessInsight? {
        val hr = profile.restingHr ?: return null
        return when {
            hr > 90 -> WellnessInsight(
                title = "Пульс в покое",
                message = "Пульс повышен (${hr} уд/мин).",
                severity = InsightSeverity.WARNING
            )
            hr < 50 -> WellnessInsight(
                title = "Пульс в покое",
                message = "Пульс ниже нормы (${hr} уд/мин) — требуется наблюдение.",
                severity = InsightSeverity.WARNING
            )
            else -> WellnessInsight(
                title = "Пульс в покое",
                message = "Пульс в целевом диапазоне (${hr} уд/мин).",
                severity = InsightSeverity.POSITIVE
            )
        }
    }
}

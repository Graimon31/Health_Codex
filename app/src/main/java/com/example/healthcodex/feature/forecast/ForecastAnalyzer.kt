// app/src/main/java/com/example/healthcodex/feature/forecast/ForecastAnalyzer.kt
package com.example.healthcodex.feature.forecast

import com.example.healthcodex.data.profile.UserProfile
import com.example.healthcodex.util.Validation
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.tanh

/**
 * Produces a lightweight health forecast based on the stored profile metrics.
 */
object ForecastAnalyzer {
    private const val BMI_NORMAL_LOW = 18.5
    private const val BMI_NORMAL_HIGH = 24.9
    private const val BMI_OVERWEIGHT = 30.0
    private val neuralModel = HealthRiskNeuralModel()

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

        val riskProbability = neuralModel.predictRisk(profile, age, bmiValue)
        val modelInsightSeverity = when {
            riskProbability >= 0.75 -> InsightSeverity.CRITICAL
            riskProbability >= 0.55 -> InsightSeverity.WARNING
            else -> InsightSeverity.POSITIVE
        }
        val modelInsight = WellnessInsight(
            title = "Оценка нейросети",
            message = "Вероятность ухудшения состояния: %s%%".format(
                "%.1f".format((riskProbability * 100).coerceIn(0.0, 100.0))
            ),
            severity = modelInsightSeverity
        )

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

        when (modelInsightSeverity) {
            InsightSeverity.POSITIVE -> positive += modelInsight
            InsightSeverity.WARNING, InsightSeverity.CRITICAL -> risks += modelInsight
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

        val headline = when {
            riskProbability >= 0.75 -> "Прогноз требует немедленного внимания"
            riskProbability >= 0.55 -> "Прогноз настораживает"
            riskProbability >= 0.35 -> "Умеренный прогноз"
            else -> "Прогноз благоприятный"
        }

        val detail = when {
            riskProbability >= 0.75 -> "Нейросетевая модель фиксирует высокий комплексный риск, обсудите план действий с врачом."
            riskProbability >= 0.55 -> "Часть показателей выходит за рамки нормы — скорректируйте образ жизни и наблюдайтесь чаще."
            riskProbability >= 0.35 -> "Есть факторы, требующие контроля, следуйте рекомендациям и отслеживайте динамику."
            else -> "Показатели в норме, сохраняйте текущий образ жизни и контроль ключевых метрик."
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

/** Lightweight fully-connected neural network for heuristic health-risk scoring. */
private class HealthRiskNeuralModel {
    private val w1 = arrayOf(
        doubleArrayOf(1.4, 1.0, 1.1, 0.8, 0.9, 0.8, 0.7, 0.4, -0.9),
        doubleArrayOf(0.6, 0.5, 0.4, 0.4, 0.5, 0.3, 0.3, 0.2, -0.4),
        doubleArrayOf(1.1, 0.8, 0.9, 0.7, 0.6, 0.5, 0.6, 0.5, -0.6),
        doubleArrayOf(0.3, 0.2, 0.3, 0.2, 0.3, 0.2, 0.2, 0.2, -0.2),
        doubleArrayOf(1.0, 0.9, 0.4, 0.3, 0.3, 0.7, 0.4, 0.4, -0.7),
        doubleArrayOf(0.7, 0.6, 0.5, 0.4, 0.5, 0.5, 0.6, 0.3, -0.5)
    )
    private val b1 = doubleArrayOf(-1.2, -0.3, -0.8, 0.0, -0.9, -0.7)

    private val w2 = arrayOf(
        doubleArrayOf(1.1, 0.8, 1.0, 0.3, 0.9, 0.8),
        doubleArrayOf(0.5, 0.4, 0.3, 0.2, 0.5, 0.3),
        doubleArrayOf(0.8, 0.6, 0.7, 0.3, 0.6, 0.6),
        doubleArrayOf(0.4, 0.3, 0.4, 0.2, 0.3, 0.4)
    )
    private val b2 = doubleArrayOf(-0.6, -0.2, -0.4, -0.1)

    private val wOut = doubleArrayOf(1.2, 0.6, 1.0, 0.4)
    private const val bOut = -0.8

    fun predictRisk(profile: UserProfile, age: Int?, bmi: Double?): Double {
        val features = buildFeatures(profile, age, bmi)
        return predict(features)
    }

    private fun buildFeatures(profile: UserProfile, age: Int?, bmi: Double?): DoubleArray {
        val ageNorm = (age ?: 0).coerceIn(0, 120) / 120.0
        val bmiNorm = bmi?.coerceIn(15.0, 40.0)?.let { (it - 15.0) / 25.0 } ?: 0.28
        val sysNorm = profile.bpBaselineSystolic?.coerceIn(80, 200)?.let { (it - 80) / 120.0 } ?: 0.25
        val diaNorm = profile.bpBaselineDiastolic?.coerceIn(50, 130)?.let { (it - 50) / 80.0 } ?: 0.25
        val hrNorm = profile.restingHr?.coerceIn(40, 190)?.let { (it - 40) / 150.0 } ?: 0.2
        val conditionsNorm = (profile.conditions.size.coerceAtMost(8)) / 8.0
        val medicationsNorm = (profile.medications.size.coerceAtMost(8)) / 8.0
        val allergiesNorm = (profile.allergies.size.coerceAtMost(6)) / 6.0
        val doctorBoost = if (profile.shareWithDoctor) 1.0 else 0.0
        return doubleArrayOf(
            ageNorm,
            bmiNorm,
            sysNorm,
            diaNorm,
            hrNorm,
            conditionsNorm,
            medicationsNorm,
            allergiesNorm,
            doctorBoost
        )
    }

    private fun predict(input: DoubleArray): Double {
        val h1 = DoubleArray(b1.size)
        for (i in h1.indices) {
            h1[i] = b1[i]
            for (j in input.indices) {
                h1[i] += w1[i][j] * input[j]
            }
            h1[i] = tanh(h1[i])
        }

        val h2 = DoubleArray(b2.size)
        for (i in h2.indices) {
            h2[i] = b2[i]
            for (j in h1.indices) {
                h2[i] += w2[i][j] * h1[j]
            }
            h2[i] = tanh(h2[i])
        }

        var out = bOut
        for (i in wOut.indices) {
            out += wOut[i] * h2[i]
        }
        return sigmoid(out).coerceIn(0.0, 1.0)
    }

    private fun sigmoid(x: Double): Double = 1.0 / (1.0 + exp(-x))
}

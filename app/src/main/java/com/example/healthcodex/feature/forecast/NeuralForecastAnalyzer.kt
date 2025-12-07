// app/src/main/java/com/example/healthcodex/feature/forecast/NeuralForecastAnalyzer.kt
package com.example.healthcodex.feature.forecast

import android.content.Context
import android.util.Log
import com.example.healthcodex.data.profile.UserProfile
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.tanh
import org.tensorflow.lite.Interpreter

/**
 * Wraps on-device TensorFlow Lite inference for the forecast module. Falls back to a
 * lightweight heuristic neural scorer when the model file is missing or invalid.
 */
class NeuralForecastAnalyzer(
    private val fallbackModel: LegacyRiskModel = LegacyRiskModel()
) {

    data class Result(
        val probability: Double,
        val topFactors: List<FeatureContribution>,
        val usedTflite: Boolean
    )

    data class FeatureContribution(
        val label: String,
        val impact: Double
    )

    fun analyze(context: Context?, profile: UserProfile, bmi: Double?, age: Int?): Result {
        val normalized = normalize(profile, bmi, age)
        val tfliteProbability = context?.let { ctx ->
            runCatching { runTflite(ctx, normalized.values.toFloatArray()) }.getOrElse { error ->
                Log.w(TAG, "TFLite forecast fallback", error)
                null
            }
        }

        if (tfliteProbability != null) {
            return Result(
                probability = tfliteProbability,
                topFactors = normalized.toContributions(),
                usedTflite = true
            )
        }

        val fallbackProb = fallbackModel.predictRisk(profile, age, bmi)
        return Result(
            probability = fallbackProb,
            topFactors = normalized.toContributions(),
            usedTflite = false
        )
    }

    private fun Map<String, Double>.toContributions(): List<FeatureContribution> =
        entries
            .sortedByDescending { abs(it.value) }
            .take(5)
            .map { FeatureContribution(it.key, it.value) }

    private fun normalize(
        profile: UserProfile,
        bmi: Double?,
        age: Int?
    ): Map<String, Double> {
        val bmiNorm = bmi?.coerceIn(15.0, 40.0)?.let { (it - 15.0) / 25.0 } ?: 0.28
        return mapOf(
            "Возраст" to (age ?: 0).coerceIn(0, 120) / 120.0,
            "BMI" to bmiNorm,
            "Систолическое давление" to (profile.bpBaselineSystolic?.coerceIn(80, 200)
                ?.let { (it - 80) / 120.0 } ?: 0.25),
            "Диастолическое давление" to (profile.bpBaselineDiastolic?.coerceIn(50, 130)
                ?.let { (it - 50) / 80.0 } ?: 0.25),
            "Пульс в покое" to (profile.restingHr?.coerceIn(40, 190)?.let { (it - 40) / 150.0 }
                ?: 0.2),
            "Хронические состояния" to (profile.conditions.size.coerceAtMost(8)) / 8.0,
            "Медикаменты" to (profile.medications.size.coerceAtMost(8)) / 8.0,
            "Аллергии" to (profile.allergies.size.coerceAtMost(6)) / 6.0,
            "Совместное ведение" to if (profile.shareWithDoctor) 1.0 else 0.0
        )
    }

    private fun runTflite(context: Context, features: FloatArray): Double {
        val buffer = loadModelFile(context, MODEL_FILE)
        val interpreter = Interpreter(buffer, Interpreter.Options().apply { setNumThreads(2) })
        val input = arrayOf(features)
        val output = Array(1) { FloatArray(1) }
        interpreter.use { it.run(input, output) }
        return output[0][0].toDouble().coerceIn(0.0, 1.0)
    }

    private fun loadModelFile(context: Context, assetName: String): ByteBuffer {
        val file = File(context.filesDir, assetName)
        if (!file.exists()) {
            context.assets.open(assetName).use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
        FileInputStream(file).channel.use { channel ->
            val mapped = channel.map(FileChannel.MapMode.READ_ONLY, 0, file.length())
            return mapped.load()
        }
    }

    companion object {
        private const val TAG = "NeuralForecastAnalyzer"
        private const val MODEL_FILE = "forecast_model.tflite"
    }
}

/** Legacy lightweight model kept as a deterministic fallback. */
class LegacyRiskModel {
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

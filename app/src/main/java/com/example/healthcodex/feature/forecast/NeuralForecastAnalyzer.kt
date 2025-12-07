// app/src/main/java/com/example/healthcodex/feature/forecast/NeuralForecastAnalyzer.kt
package com.example.healthcodex.feature.forecast

import android.content.Context
import android.util.Log
import com.example.healthcodex.data.profile.UserProfile
import com.example.healthcodex.data.profile.sex.Sex
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.max
import org.tensorflow.lite.Interpreter

/**
 * Runs on-device TensorFlow Lite inference for the forecast module. The model lives in
 * [app/src/main/assets/ml/health_forecast_model.tflite] and is evaluated fully offline.
 */
class NeuralForecastAnalyzer(
    private val context: Context?,
    private val interpreterFactory: ((ByteBuffer) -> Interpreter)? = null
) {

    /** Returns risk probability in [0, 1], or null if the model cannot be evaluated. */
    fun analyze(profile: UserProfile): Double? {
        val interpreter = interpreter ?: return null
        val features = buildFeatureVector(profile) ?: return null
        val output = Array(1) { FloatArray(OUTPUT_CLASSES) }
        return runCatching {
            interpreter.run(arrayOf(features), output)
            output[0].let { probs ->
                val maxProb = probs.maxOrNull() ?: return@let null
                maxProb.toDouble().coerceIn(0.0, 1.0)
            }
        }.getOrElse { error ->
            Log.w(TAG, "TFLite inference failed", error)
            null
        }
    }

    private fun buildFeatureVector(profile: UserProfile): FloatArray? {
        val age = profile.birthDate?.let { com.example.healthcodex.util.Validation.calculateAge(it) }
        val height = profile.heightCm
        val weight = profile.weightKg
        val bmi = profile.heightCm?.takeIf { it > 0 }?.let { h ->
            weight?.div((h / 100.0).toFloat() * (h / 100.0).toFloat())
        }

        val features = mutableListOf<Float>()
        features += normalize(age?.toDouble(), 0.0, 120.0)
        features += when (profile.sex) {
            Sex.MALE -> floatArrayOf(1f, 0f, 0f)
            Sex.FEMALE -> floatArrayOf(0f, 1f, 0f)
            Sex.OTHER -> floatArrayOf(0f, 0f, 1f)
        }.toList()
        features += normalize(height?.toDouble(), 50.0, 250.0)
        features += normalize(weight?.toDouble(), 20.0, 400.0)
        features += normalize(bmi?.toDouble(), 15.0, 45.0)
        features += normalize(profile.bpBaselineSystolic?.toDouble(), 70.0, 250.0)
        features += normalize(profile.bpBaselineDiastolic?.toDouble(), 40.0, 150.0)
        features += normalize(profile.restingHr?.toDouble(), 30.0, 220.0)

        features += encodeCategories(profile.conditions, CONDITION_BUCKETS)
        features += encodeCategories(profile.allergies, ALLERGY_BUCKETS)

        features += normalize(profile.medications.size.toDouble(), 0.0, 12.0)
        features += if (profile.shareWithDoctor) 1f else 0f

        return features.toFloatArray()
    }

    private fun encodeCategories(values: List<String>, buckets: List<String>): List<Float> {
        val lowered = values.map { it.lowercase() }
        val hits = MutableList(buckets.size) { 0f }
        lowered.forEach { value ->
            val index = buckets.indexOfFirst { bucket -> value.contains(bucket) }
            if (index >= 0) {
                hits[index] = 1f
            } else {
                // Last position acts as "other" to preserve alignment.
                hits[hits.lastIndex] = 1f
            }
        }
        return hits
    }

    private fun normalize(value: Double?, min: Double, max: Double): Float {
        if (value == null) return 0f
        val clamped = value.coerceIn(min, max)
        return ((clamped - min) / max(max - min, 1e-3)).toFloat()
    }

    private val interpreter: Interpreter? by lazy {
        val ctx = context ?: return@lazy null
        runCatching {
            val asset = loadModelFile(ctx, MODEL_ASSET)
            interpreterFactory?.invoke(asset)
                ?: Interpreter(asset, Interpreter.Options().apply { setNumThreads(2) })
        }.getOrElse {
            Log.w(TAG, "Unable to load TFLite model", it)
            null
        }
    }

    private fun loadModelFile(context: Context, assetPath: String): ByteBuffer {
        val file = context.cacheDir.resolve(MODEL_TMP_NAME)
        context.assets.open(assetPath).use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        FileInputStream(file).channel.use { channel ->
            return channel.map(FileChannel.MapMode.READ_ONLY, 0, file.length()).load()
        }
    }

    companion object {
        private const val TAG = "NeuralForecastAnalyzer"
        private const val MODEL_ASSET = "ml/health_forecast_model.tflite"
        private const val MODEL_TMP_NAME = "health_forecast_model.tflite"
        private const val OUTPUT_CLASSES = 3
        private val CONDITION_BUCKETS = listOf(
            "гиперто", "диабет", "астма", "хобл", "cardio", "renal", "метаб",
            "onco", "проч" // last acts as "other"
        )
        private val ALLERGY_BUCKETS = listOf(
            "пыль", "пища", "лекар", "пыльца", "живот", "проч"
        )
    }
}

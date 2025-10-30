// app/src/main/java/com/example/healthcodex/data/measurements/MeasurementConverters.kt
package com.example.healthcodex.data.measurements

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/**
 * Room converters for measurement-specific types.
 */
class MeasurementConverters {
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val detailsAdapter = moshi.adapter(MeasurementDetails::class.java)

    @TypeConverter
    fun typeFromString(value: String?): MeasurementType? = value?.let { MeasurementType.valueOf(it) }

    @TypeConverter
    fun typeToString(type: MeasurementType?): String? = type?.name

    @TypeConverter
    fun sourceFromString(value: String?): MeasurementSource? = value?.let { MeasurementSource.valueOf(it) }

    @TypeConverter
    fun sourceToString(source: MeasurementSource?): String? = source?.name

    @TypeConverter
    fun confidenceFromString(value: String?): MeasurementConfidence? = value?.let { MeasurementConfidence.valueOf(it) }

    @TypeConverter
    fun confidenceToString(confidence: MeasurementConfidence?): String? = confidence?.name

    @TypeConverter
    fun detailsFromJson(json: String?): MeasurementDetails? = json?.let { detailsAdapter.fromJson(it) }

    @TypeConverter
    fun detailsToJson(details: MeasurementDetails?): String? = details?.let { detailsAdapter.toJson(it) }
}

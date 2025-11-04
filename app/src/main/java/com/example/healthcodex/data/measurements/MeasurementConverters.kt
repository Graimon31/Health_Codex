// app/src/main/java/com/example/healthcodex/data/measurements/MeasurementConverters.kt
package com.example.healthcodex.data.measurements

import androidx.room.TypeConverter
import com.example.healthcodex.data.db.InstantJsonAdapter
import com.example.healthcodex.data.measurements.DeviceConnectionStatus
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

/**
 * Room converters for measurement-specific types.
 */
class MeasurementConverters {
    private val moshi: Moshi = Moshi.Builder()
        .add(InstantJsonAdapter())
        .add(KotlinJsonAdapterFactory())
        .build()
    private val detailsAdapter = moshi.adapter(MeasurementDetails::class.java)

    @TypeConverter
    fun typeFromString(value: String?): MeasurementType = value?.let {
        runCatching { MeasurementType.valueOf(it) }.getOrElse { MeasurementType.HEART_RATE }
    } ?: MeasurementType.HEART_RATE

    @TypeConverter
    fun typeToString(type: MeasurementType?): String? = type?.name

    @TypeConverter
    fun sourceFromString(value: String?): MeasurementSource = value?.let {
        runCatching { MeasurementSource.valueOf(it) }.getOrElse { MeasurementSource.DEVICE }
    } ?: MeasurementSource.DEVICE

    @TypeConverter
    fun sourceToString(source: MeasurementSource?): String? = source?.name

    @TypeConverter
    fun confidenceFromString(value: String?): MeasurementConfidence = value?.let {
        runCatching { MeasurementConfidence.valueOf(it) }.getOrElse { MeasurementConfidence.HIGH }
    } ?: MeasurementConfidence.HIGH

    @TypeConverter
    fun confidenceToString(confidence: MeasurementConfidence?): String? = confidence?.name

    @TypeConverter
    fun deviceTypeFromString(value: String?): MeasurementDeviceType? = value?.let {
        runCatching { MeasurementDeviceType.valueOf(it) }.getOrNull()
    }

    @TypeConverter
    fun deviceTypeToString(type: MeasurementDeviceType?): String? = type?.name

    @TypeConverter
    fun deviceStatusFromString(value: String?): DeviceConnectionStatus = value?.let {
        runCatching { DeviceConnectionStatus.valueOf(it) }.getOrElse { DeviceConnectionStatus.INACTIVE }
    } ?: DeviceConnectionStatus.INACTIVE

    @TypeConverter
    fun deviceStatusToString(status: DeviceConnectionStatus?): String? = status?.name

    @TypeConverter
    fun detailsFromJson(json: String?): MeasurementDetails = json?.let {
        runCatching { detailsAdapter.fromJson(it) }.getOrNull()
    } ?: MeasurementDetails()

    @TypeConverter
    fun detailsToJson(details: MeasurementDetails?): String =
        runCatching { detailsAdapter.toJson(details ?: MeasurementDetails()) }.getOrDefault("{}")
}

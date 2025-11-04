// app/src/main/java/com/example/healthcodex/data/profile/Converters.kt
package com.example.healthcodex.data.profile

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Type converters bridging Kotlin types with Room persistence.
 */
class UserProfileConverters {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val medicationListAdapter = moshi.adapter<List<Medication>>(Types.newParameterizedType(List::class.java, Medication::class.java))
    private val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    @TypeConverter
    fun fromMedicationJson(value: String?): List<Medication> = value?.let {
        runCatching { medicationListAdapter.fromJson(it) }.getOrNull() ?: emptyList()
    } ?: emptyList()

    @TypeConverter
    fun medicationListToJson(list: List<Medication>?): String = runCatching {
        medicationListAdapter.toJson(list ?: emptyList())
    }.getOrDefault("[]")
    @TypeConverter
    fun fromLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)

    @TypeConverter
    fun localDateToString(date: LocalDate?): String? = date?.format(isoFormatter)
}

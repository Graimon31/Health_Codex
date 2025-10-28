// app/src/main/java/com/example/healthcodex/data/profile/Converters.kt
package com.example.healthcodex.data.profile

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Type converters bridging Kotlin types with Room persistence.
 */
class UserProfileConverters {
    private val moshi = Moshi.Builder().build()
    private val stringListAdapter = moshi.adapter<List<String>>(Types.newParameterizedType(List::class.java, String::class.java))
    private val medicationListAdapter = moshi.adapter<List<Medication>>(Types.newParameterizedType(List::class.java, Medication::class.java))
    private val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    @TypeConverter
    fun fromStringList(value: String?): List<String> =
        value?.let { stringListAdapter.fromJson(it) } ?: emptyList()

    @TypeConverter
    fun stringListToString(list: List<String>?): String =
        stringListAdapter.toJson(list ?: emptyList())

    @TypeConverter
    fun fromMedicationJson(value: String?): List<Medication> =
        value?.let { medicationListAdapter.fromJson(it) } ?: emptyList()

    @TypeConverter
    fun medicationListToJson(list: List<Medication>?): String =
        medicationListAdapter.toJson(list ?: emptyList())

    @TypeConverter
    fun fromInstant(value: String?): Instant? = value?.let(Instant::parse)

    @TypeConverter
    fun instantToString(instant: Instant?): String? = instant?.toString()

    @TypeConverter
    fun fromLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)

    @TypeConverter
    fun localDateToString(date: LocalDate?): String? = date?.format(isoFormatter)
}

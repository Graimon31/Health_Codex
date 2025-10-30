// app/src/main/java/com/example/healthcodex/data/db/CommonConverters.kt
package com.example.healthcodex.data.db

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.time.Instant

/**
 * Shared Room converters reused by multiple features to avoid duplicate definitions.
 */
class CommonConverters {
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val stringListAdapter = moshi.adapter<List<String>>(Types.newParameterizedType(List::class.java, String::class.java))

    @TypeConverter
    fun stringListFromJson(value: String?): List<String> = value?.let { stringListAdapter.fromJson(it) } ?: emptyList()

    @TypeConverter
    fun stringListToJson(list: List<String>?): String = stringListAdapter.toJson(list ?: emptyList())

    @TypeConverter
    fun instantFromString(value: String?): Instant? = value?.let(Instant::parse)

    @TypeConverter
    fun instantToString(instant: Instant?): String? = instant?.toString()
}

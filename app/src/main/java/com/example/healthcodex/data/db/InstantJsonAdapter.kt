// app/src/main/java/com/example/healthcodex/data/db/InstantJsonAdapter.kt
package com.example.healthcodex.data.db

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.Instant

/**
 * Moshi adapter that encodes [Instant] values as ISO-8601 strings for persistence.
 */
class InstantJsonAdapter {
    @ToJson
    fun toJson(value: Instant?): String? = value?.toString()

    @FromJson
    fun fromJson(value: String?): Instant? = value?.let { runCatching { Instant.parse(it) }.getOrNull() }
}

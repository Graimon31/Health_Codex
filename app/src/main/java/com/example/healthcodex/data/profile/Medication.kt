// app/src/main/java/com/example/healthcodex/data/profile/Medication.kt
package com.example.healthcodex.data.profile

import com.squareup.moshi.JsonClass

/**
 * Describes a medication entry stored in the user profile.
 */
@JsonClass(generateAdapter = true)
data class Medication(
    val name: String,
    val dose: String,
    val scheduleNote: String
)

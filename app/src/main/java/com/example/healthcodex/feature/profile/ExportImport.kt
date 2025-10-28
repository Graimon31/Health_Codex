// app/src/main/java/com/example/healthcodex/feature/profile/ExportImport.kt
package com.example.healthcodex.feature.profile

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.example.healthcodex.data.profile.UserProfile
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.time.LocalDate

/**
 * Helper object for JSON export/import and ICE sharing.
 */
object ProfileExportImport {
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val adapter = moshi.adapter(UserProfile::class.java)

    fun exportProfile(context: Context, profile: UserProfile) {
        val resolver = context.contentResolver
        val fileName = "HealthProfile_${LocalDate.now()}.json"
        val json = adapter.toJson(profile)
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
        }
        val uri = resolver.insert(MediaStore.Files.getContentUri("external"), values)
        if (uri != null) {
            resolver.openOutputStream(uri)?.use { stream ->
                stream.write(json.toByteArray())
            }
            Toast.makeText(context, "Файл сохранён: $fileName", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Не удалось сохранить файл", Toast.LENGTH_SHORT).show()
        }
    }

    fun importProfile(context: Context, onResult: (UserProfile?) -> Unit) {
        val resolver = context.contentResolver
        val collection = MediaStore.Files.getContentUri("external")
        val projection = arrayOf(MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DISPLAY_NAME)
        val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("HealthProfile_%")
        val sortOrder = "${MediaStore.MediaColumns.DATE_ADDED} DESC"
        var profile: UserProfile? = null
        resolver.query(collection, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(0)
                val uri = ContentUris.withAppendedId(collection, id)
                resolver.openInputStream(uri)?.use { stream ->
                    val json = stream.bufferedReader().readText()
                    profile = adapter.fromJson(json)
                }
            }
        }
        if (profile == null) {
            Toast.makeText(context, "Файл профиля не найден", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Профиль импортирован", Toast.LENGTH_SHORT).show()
        }
        onResult(profile)
    }

    fun shareIce(context: Context, profile: UserProfile) {
        val age = profile.birthDate?.let { java.time.Period.between(it, java.time.LocalDate.now()).years }
        val text = buildString {
            appendLine("ICE карта")
            appendLine("ФИО: ${profile.fullName}")
            appendLine("Возраст: ${age ?: "—"}")
            appendLine("Диагнозы: ${profile.conditions.joinToString().ifEmpty { "нет" }}")
            appendLine("Аллергии: ${profile.allergies.joinToString().ifEmpty { "нет" }}")
            appendLine("ICE телефон: ${profile.emergencyPhone.orEmpty()}")
            append("Врач: ${profile.doctorName.orEmpty()} ${profile.doctorPhone.orEmpty()}")
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "Поделиться ICE"))
    }
}

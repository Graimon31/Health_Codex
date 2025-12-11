// app/src/main/java/com/example/healthcodex/feature/profile/ExportImport.kt
package com.example.healthcodex.feature.profile

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.example.healthcodex.data.profile.UserProfile
import com.example.healthcodex.data.db.InstantJsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.time.LocalDate
import android.net.Uri
import java.io.InputStream
import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson

/**
 * Helper object for JSON export/import and ICE sharing.
 */
object ProfileExportImport {
    private val moshi: Moshi = Moshi.Builder()
        .add(LocalDateJsonAdapter)
        .add(InstantJsonAdapter())
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

    fun importProfile(context: Context, uri: Uri, onResult: (UserProfile?) -> Unit) {
        val resolver = context.contentResolver
        var profile: UserProfile? = null
        try {
            resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (_: SecurityException) {
            // If not persisted, we will still try reading once.
        }
        runCatching {
            resolver.openInputStream(uri)?.use { stream ->
                profile = parseProfile(stream)
            }
        }.onFailure {
            Toast.makeText(context, "Ошибка чтения файла профиля", Toast.LENGTH_SHORT).show()
        }
        if (profile == null) {
            Toast.makeText(context, "Файл профиля не найден", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Профиль импортирован", Toast.LENGTH_SHORT).show()
        }
        onResult(profile)
    }

    fun parseProfile(stream: InputStream): UserProfile? {
        return runCatching {
            val json = stream.bufferedReader().readText()
            adapter.fromJson(json)
        }.getOrNull()
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

/** Moshi adapter for java.time.LocalDate to ensure JSON import/export succeeds. */
private object LocalDateJsonAdapter {
    @ToJson
    fun toJson(value: LocalDate?): String? = value?.toString()

    @FromJson
    fun fromJson(value: String?): LocalDate? = value?.let(LocalDate::parse)
}

// app/src/main/java/com/example/healthcodex/feature/measurements/MeasurementsExport.kt
package com.example.healthcodex.feature.measurements

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.example.healthcodex.data.measurements.MeasurementEntry
import com.example.healthcodex.util.Formatters
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Helper for exporting measurement history into JSON or CSV formats.
 */
object MeasurementsExport {
    enum class Format(val mime: String, val extension: String) {
        JSON("application/json", "json"),
        CSV("text/csv", "csv")
    }

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val listAdapter = moshi.adapter<List<MeasurementEntry>>(Types.newParameterizedType(List::class.java, MeasurementEntry::class.java))
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)

    fun export(context: Context, entries: List<MeasurementEntry>, format: Format) {
        if (entries.isEmpty()) {
            Toast.makeText(context, "Нет данных для экспорта", Toast.LENGTH_SHORT).show()
            return
        }
        val resolver = context.contentResolver
        val fileName = "Measurements_${LocalDate.now().format(dateFormatter)}.${format.extension}"
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, format.mime)
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS)
        }
        val uri = resolver.insert(MediaStore.Files.getContentUri("external"), values)
        if (uri == null) {
            Toast.makeText(context, "Не удалось создать файл", Toast.LENGTH_SHORT).show()
            return
        }
        val payload = when (format) {
            Format.JSON -> listAdapter.toJson(entries)
            Format.CSV -> buildCsv(entries)
        }
        resolver.openOutputStream(uri)?.use { stream ->
            stream.write(payload.toByteArray())
        }
        Toast.makeText(context, "Экспортировано: $fileName", Toast.LENGTH_SHORT).show()
    }

    private fun buildCsv(entries: List<MeasurementEntry>): String {
        val header = "type,timestamp,value1,value2,value3,source,device,note"
        val rows = entries.joinToString(separator = "\n") { entry ->
            val zone = ZoneId.systemDefault()
            val instant = entry.timestamp.atZone(zone)
            val primary = entry.details.primaryValue?.let { Formatters.formatDouble(it, 2) } ?: ""
            val secondary = entry.details.secondaryValue?.let { Formatters.formatDouble(it, 2) } ?: ""
            val tertiary = entry.details.tertiaryValue?.let { Formatters.formatDouble(it, 2) } ?: ""
            listOf(
                entry.type.name,
                instant.toString(),
                primary,
                secondary,
                tertiary,
                entry.source.name,
                entry.deviceName.orEmpty(),
                entry.note?.replace(',', ';').orEmpty()
            ).joinToString(separator = ",")
        }
        return buildString {
            appendLine(header)
            append(rows)
        }
    }

    fun buildShareText(entry: MeasurementEntry): String = buildString {
        appendLine("${entry.type.name}: ${entry.details.primaryValue ?: entry.details.statusText ?: "—"}")
        entry.details.secondaryValue?.let { appendLine("Дополнительно: $it") }
        entry.note?.takeIf { it.isNotBlank() }?.let { appendLine("Заметка: $it") }
        append("Источник: ${entry.source.name}")
    }
}

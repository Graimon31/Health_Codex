// app/src/main/java/com/example/healthcodex/ui/measurements/MeasurementDetailScreen.kt
package com.example.healthcodex.ui.measurements

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthcodex.R
import com.example.healthcodex.data.measurements.MeasurementEntry
import com.example.healthcodex.data.measurements.MeasurementType
import com.example.healthcodex.util.Formatters
import java.time.ZoneId
import kotlin.math.max
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeasurementDetailRoute(
    measurementId: Long,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as android.app.Application
    val viewModel: MeasurementDetailViewModel = viewModel(
        factory = MeasurementDetailViewModel.factory(application, measurementId)
    )
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(),
                title = { Text(text = stringResource(id = R.string.measure_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.measure_action_back))
                    }
                },
                actions = {
                    state.entry?.let { entry ->
                        IconButton(onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, com.example.healthcodex.feature.measurements.MeasurementsExport.buildShareText(entry))
                            }
                            context.startActivity(Intent.createChooser(intent, context.getString(R.string.measure_share_title)))
                        }) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = stringResource(id = R.string.measure_action_share))
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        MeasurementDetailContent(state = state, paddingValues = innerPadding)
    }
}

@Composable
private fun MeasurementDetailContent(
    state: MeasurementDetailState,
    paddingValues: PaddingValues
) {
    when {
        state.isLoading -> {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        state.entry == null -> {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text(text = stringResource(id = R.string.measure_detail_empty), style = MaterialTheme.typography.bodyLarge)
            }
        }
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { DetailHeroCard(entry = state.entry) }
                item { MeasurementChartSection(entry = state.entry, history = state.typeHistory) }
                item {
                    Text(
                        text = stringResource(id = R.string.measure_detail_history),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                items(state.dayEntries, key = { it.id }) { entry ->
                    HistoryRow(entry = entry)
                }
            }
        }
    }
}

@Composable
private fun DetailHeroCard(entry: MeasurementEntry) {
    Card(
        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = stringResource(id = entry.type.titleRes), style = MaterialTheme.typography.titleLarge)
            Text(text = formatDetailValue(entry), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(
                text = Formatters.formatInstant(entry.timestamp),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MeasurementChartSection(entry: MeasurementEntry, history: List<MeasurementEntry>) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        tonalElevation = 2.dp,
        shape = CardDefaults.shape
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = stringResource(id = R.string.measure_detail_chart_title), style = MaterialTheme.typography.titleMedium)
            when (entry.type) {
                MeasurementType.STEPS, MeasurementType.CALORIES ->
                    BarChart(entries = history, type = entry.type)
                MeasurementType.BLOOD_PRESSURE ->
                    PressureChart(entries = history)
                MeasurementType.SLEEP ->
                    SleepChart(entry = entry, history = history)
                else ->
                    LineChart(entries = history, type = entry.type)
            }
        }
    }
}

// ─── Line chart (HR, Weight, SpO2, Respiratory) ──────────────────────────────

@Composable
private fun LineChart(entries: List<MeasurementEntry>, type: MeasurementType) {
    val values = entries.mapNotNull { it.details.primaryValue?.toFloat() }
    val placeholder = stringResource(id = R.string.measure_detail_chart_placeholder)
    val color = chartColor(type)
    val gridColor = Color.Gray.copy(alpha = 0.25f)
    val labelPaint = android.graphics.Paint().apply {
        this.color = android.graphics.Color.GRAY
        textSize = 28f
        isAntiAlias = true
    }

    Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
        val leftPad = 52f
        val bottomPad = 28f
        val chartW = size.width - leftPad
        val chartH = size.height - bottomPad

        if (values.isEmpty()) {
            drawContext.canvas.nativeCanvas.drawText(placeholder, leftPad + chartW / 4, size.height / 2, labelPaint)
            return@Canvas
        }

        val minV = values.minOrNull()!!
        val maxV = values.maxOrNull()!!
        val range = max(1f, maxV - minV)
        val paddedMin = minV - range * 0.05f
        val paddedMax = maxV + range * 0.05f
        val paddedRange = paddedMax - paddedMin

        // Grid lines (4 horizontal)
        val steps = 4
        for (i in 0..steps) {
            val gy = chartH - (i.toFloat() / steps) * chartH
            drawLine(gridColor, Offset(leftPad, gy), Offset(size.width, gy), strokeWidth = 1f)
            val labelVal = paddedMin + (i.toFloat() / steps) * paddedRange
            drawContext.canvas.nativeCanvas.drawText(
                "%.0f".format(labelVal), 0f, gy + 9f, labelPaint
            )
        }

        if (values.size == 1) {
            val cy = chartH - ((values[0] - paddedMin) / paddedRange) * chartH
            drawCircle(color, 14f, Offset(leftPad + chartW / 2, cy))
            return@Canvas
        }

        // Fill area under curve
        val fillPath = Path()
        values.forEachIndexed { i, v ->
            val x = leftPad + chartW * (i / (values.size - 1f))
            val y = chartH - ((v - paddedMin) / paddedRange) * chartH
            if (i == 0) fillPath.moveTo(x, y) else fillPath.lineTo(x, y)
        }
        fillPath.lineTo(leftPad + chartW, chartH)
        fillPath.lineTo(leftPad, chartH)
        fillPath.close()
        drawPath(fillPath, color.copy(alpha = 0.15f))

        // Line
        val linePath = Path()
        values.forEachIndexed { i, v ->
            val x = leftPad + chartW * (i / (values.size - 1f))
            val y = chartH - ((v - paddedMin) / paddedRange) * chartH
            if (i == 0) linePath.moveTo(x, y) else linePath.lineTo(x, y)
        }
        drawPath(linePath, color, style = Stroke(width = 5f))

        // Dots at each data point
        values.forEachIndexed { i, v ->
            val x = leftPad + chartW * (i / (values.size - 1f))
            val y = chartH - ((v - paddedMin) / paddedRange) * chartH
            drawCircle(color, 7f, Offset(x, y))
        }
    }
}

// ─── Bar chart (Steps, Calories) ─────────────────────────────────────────────

@Composable
private fun BarChart(entries: List<MeasurementEntry>, type: MeasurementType) {
    val zone = ZoneId.systemDefault()
    val values = entries.mapNotNull { e ->
        e.details.primaryValue?.toFloat()?.let { v ->
            val day = e.timestamp.atZone(zone).dayOfWeek.value  // 1=Mon..7=Sun
            Pair(day, v)
        }
    }
    val placeholder = stringResource(id = R.string.measure_detail_chart_placeholder)
    val color = chartColor(type)
    val gridColor = Color.Gray.copy(alpha = 0.25f)
    val labelPaint = android.graphics.Paint().apply {
        this.color = android.graphics.Color.GRAY
        textSize = 28f
        isAntiAlias = true
    }
    val dayLabels = arrayOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")

    Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
        val leftPad = 52f
        val bottomPad = 32f
        val chartW = size.width - leftPad
        val chartH = size.height - bottomPad

        if (values.isEmpty()) {
            drawContext.canvas.nativeCanvas.drawText(placeholder, leftPad + chartW / 4, size.height / 2, labelPaint)
            return@Canvas
        }

        val maxV = max(1f, values.maxOf { it.second })
        val barCount = values.size
        val barW = (chartW / (barCount * 1.5f)).coerceAtMost(chartW / 7f * 0.8f)
        val gap = if (barCount > 1) (chartW - barW * barCount) / (barCount - 1) else 0f

        // Horizontal grid
        for (i in 0..3) {
            val gy = chartH - (i.toFloat() / 3) * chartH
            drawLine(gridColor, Offset(leftPad, gy), Offset(size.width, gy), strokeWidth = 1f)
            val lv = (i.toFloat() / 3) * maxV
            drawContext.canvas.nativeCanvas.drawText("%.0f".format(lv), 0f, gy + 9f, labelPaint)
        }

        values.forEachIndexed { i, (dayOfWeek, v) ->
            val x = leftPad + i * (barW + gap)
            val barH = (v / maxV) * chartH
            drawRoundRect(
                color = color,
                topLeft = Offset(x, chartH - barH),
                size = Size(barW, barH),
                cornerRadius = CornerRadius(6f, 6f)
            )
            // Day label below bar
            val label = dayLabels.getOrNull(dayOfWeek - 1) ?: ""
            drawContext.canvas.nativeCanvas.drawText(
                label, x + barW / 2 - 18f, chartH + bottomPad - 4f, labelPaint
            )
        }
    }
}

// ─── Pressure chart (two lines: systolic / diastolic) ────────────────────────

@Composable
private fun PressureChart(entries: List<MeasurementEntry>) {
    val systolic = entries.mapNotNull { it.details.primaryValue?.toFloat() }
    val diastolic = entries.mapNotNull { it.details.secondaryValue?.toFloat() }
    val placeholder = stringResource(id = R.string.measure_detail_chart_placeholder)
    val sysColor = Color(0xFFEF4444)
    val diaColor = Color(0xFF3B82F6)
    val gridColor = Color.Gray.copy(alpha = 0.25f)
    val labelPaint = android.graphics.Paint().apply {
        this.color = android.graphics.Color.GRAY
        textSize = 28f
        isAntiAlias = true
    }

    Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
        val leftPad = 52f
        val bottomPad = 28f
        val chartW = size.width - leftPad
        val chartH = size.height - bottomPad

        if (systolic.isEmpty() || diastolic.isEmpty()) {
            drawContext.canvas.nativeCanvas.drawText(placeholder, leftPad + chartW / 4, size.height / 2, labelPaint)
            return@Canvas
        }

        val allValues = systolic + diastolic
        val minV = (allValues.minOrNull() ?: 0f) - 5f
        val maxV = (allValues.maxOrNull() ?: 1f) + 5f
        val range = max(1f, maxV - minV)

        // Grid
        for (i in 0..4) {
            val gy = chartH - (i.toFloat() / 4) * chartH
            drawLine(gridColor, Offset(leftPad, gy), Offset(size.width, gy), strokeWidth = 1f)
            val lv = minV + (i.toFloat() / 4) * range
            drawContext.canvas.nativeCanvas.drawText("%.0f".format(lv), 0f, gy + 9f, labelPaint)
        }

        fun drawLine2(values: List<Float>, color: Color) {
            if (values.size == 1) {
                val y = chartH - ((values[0] - minV) / range) * chartH
                drawCircle(color, 12f, Offset(leftPad + chartW / 2, y))
                return
            }
            val path = Path()
            values.forEachIndexed { i, v ->
                val x = leftPad + chartW * (i / (values.size - 1f))
                val y = chartH - ((v - minV) / range) * chartH
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, color, style = Stroke(width = 5f))
            values.forEachIndexed { i, v ->
                val x = leftPad + chartW * (i / (values.size - 1f))
                val y = chartH - ((v - minV) / range) * chartH
                drawCircle(color, 7f, Offset(x, y))
            }
        }

        drawLine2(systolic, sysColor)
        drawLine2(diastolic, diaColor)

        // Legend
        val legPaint = android.graphics.Paint().apply {
            textSize = 28f; isAntiAlias = true
        }
        legPaint.color = android.graphics.Color.rgb(239, 68, 68)
        drawContext.canvas.nativeCanvas.drawText("Сис", leftPad + 4f, 24f, legPaint)
        legPaint.color = android.graphics.Color.rgb(59, 130, 246)
        drawContext.canvas.nativeCanvas.drawText("Диа", leftPad + 80f, 24f, legPaint)
    }
}

// ─── Sleep chart (timeline bar + 7-day duration bars) ────────────────────────

@Composable
private fun SleepChart(entry: MeasurementEntry, history: List<MeasurementEntry>) {
    val zone = ZoneId.systemDefault()
    val sleepColor = Color(0xFF6366F1)
    val gridColor = Color.Gray.copy(alpha = 0.25f)
    val labelPaint = android.graphics.Paint().apply {
        this.color = android.graphics.Color.GRAY
        textSize = 28f; isAntiAlias = true
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // 1. Sleep window timeline bar for the current entry
        val sleepStart = entry.startTimestamp ?: entry.details.startInstant
        val sleepEnd = entry.details.endInstant ?: entry.timestamp
        if (sleepStart != null) {
            val startZdt = sleepStart.atZone(zone)
            val endZdt = sleepEnd.atZone(zone)
            // Normalize to minutes from 8 PM of previous day (20:00) up to 12:00 noon = 16h window
            val windowStartHour = 20 // 8 PM
            val windowMinutes = 16 * 60f // 20:00 → 12:00 = 16 hours
            fun toWindowMinutes(zdt: java.time.ZonedDateTime): Float {
                val h = zdt.hour
                val m = zdt.minute
                val totalFromMidnight = h * 60 + m
                // if hour < 12 we're still in the night, map to minutes after 20:00 (previous day)
                val adjusted = if (h < 12) totalFromMidnight + 24 * 60 - windowStartHour * 60
                else totalFromMidnight - windowStartHour * 60
                return adjusted.toFloat().coerceIn(0f, windowMinutes)
            }
            val startMin = toWindowMinutes(startZdt)
            val endMin = toWindowMinutes(endZdt)

            Canvas(modifier = Modifier.fillMaxWidth().height(60.dp)) {
                val leftPad = 52f
                val chartW = size.width - leftPad
                val trackH = 28f
                val trackY = (size.height - trackH) / 2

                // Track background
                drawRoundRect(gridColor, Offset(leftPad, trackY), Size(chartW, trackH), CornerRadius(6f))
                // Sleep bar
                val barStart = leftPad + (startMin / windowMinutes) * chartW
                val barEnd = leftPad + (endMin / windowMinutes) * chartW
                drawRoundRect(
                    sleepColor,
                    Offset(barStart, trackY),
                    Size((barEnd - barStart).coerceAtLeast(4f), trackH),
                    CornerRadius(6f)
                )
                // X-axis hour labels: 20, 22, 00, 02, 04, 06, 08, 10, 12
                val hourLabels = listOf("20", "22", "00", "02", "04", "06", "08", "10", "12")
                hourLabels.forEachIndexed { i, lbl ->
                    val lx = leftPad + (i.toFloat() / (hourLabels.size - 1)) * chartW
                    drawContext.canvas.nativeCanvas.drawText(lbl, lx - 14f, size.height, labelPaint)
                }
                // Start/end time labels above bar
                val timePaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE; textSize = 26f; isAntiAlias = true
                }
                val fmt = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
                drawContext.canvas.nativeCanvas.drawText(
                    fmt.format(startZdt), barStart + 4f, trackY + trackH * 0.7f, timePaint
                )
                val endLabel = fmt.format(endZdt)
                drawContext.canvas.nativeCanvas.drawText(
                    endLabel, (barEnd - 80f).coerceAtLeast(barStart + 60f), trackY + trackH * 0.7f, timePaint
                )
            }
        }

        // 2. 7-day sleep duration bar chart
        val durations = history.map { e ->
            val day = e.timestamp.atZone(zone).dayOfWeek.value
            val dur = e.details.durationMinutes?.toFloat()
                ?: e.startTimestamp?.let { s ->
                    java.time.Duration.between(s, e.details.endInstant ?: e.timestamp).toMinutes().toFloat()
                } ?: 0f
            Pair(day, dur)
        }

        if (durations.isNotEmpty()) {
            val maxDur = max(1f, durations.maxOf { it.second })
            val dayLabels = arrayOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
            val idealHours = 8 * 60f // 8 hours reference line

            Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                val leftPad = 52f
                val bottomPad = 32f
                val chartW = size.width - leftPad
                val chartH = size.height - bottomPad

                val barCount = durations.size
                val barW = (chartW / (barCount * 1.6f)).coerceAtMost(chartW / 7f * 0.8f)
                val gap = if (barCount > 1) (chartW - barW * barCount) / (barCount - 1) else 0f

                // Horizontal grid (6 h, 7 h, 8 h, 9 h labels)
                listOf(6f, 7f, 8f, 9f).forEach { h ->
                    val gy = chartH - (h * 60 / maxDur) * chartH
                    if (gy in 0f..chartH) {
                        drawLine(gridColor, Offset(leftPad, gy), Offset(size.width, gy), strokeWidth = 1f)
                        drawContext.canvas.nativeCanvas.drawText("${h.toInt()}ч", 0f, gy + 9f, labelPaint)
                    }
                }

                // Ideal 8h dashed reference line
                if (idealHours <= maxDur) {
                    val iy = chartH - (idealHours / maxDur) * chartH
                    val dashPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.argb(120, 99, 102, 241)
                        strokeWidth = 2f
                        pathEffect = android.graphics.DashPathEffect(floatArrayOf(12f, 8f), 0f)
                        isAntiAlias = true
                    }
                    drawContext.canvas.nativeCanvas.drawLine(leftPad, iy, size.width, iy, dashPaint)
                }

                durations.forEachIndexed { i, (dayOfWeek, dur) ->
                    val x = leftPad + i * (barW + gap)
                    val barH = (dur / maxDur) * chartH
                    val isCurrentEntry = i == durations.indexOfFirst {
                        it.first == entry.timestamp.atZone(zone).dayOfWeek.value
                    }
                    drawRoundRect(
                        color = if (isCurrentEntry) sleepColor else sleepColor.copy(alpha = 0.5f),
                        topLeft = Offset(x, chartH - barH),
                        size = Size(barW, barH),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                    val label = dayLabels.getOrNull(dayOfWeek - 1) ?: ""
                    drawContext.canvas.nativeCanvas.drawText(label, x + barW / 2 - 18f, chartH + bottomPad - 4f, labelPaint)
                }
            }
        }
    }
}

// ─── History row ──────────────────────────────────────────────────────────────

@Composable
private fun HistoryRow(entry: MeasurementEntry) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = Formatters.formatInstant(entry.timestamp), style = MaterialTheme.typography.labelLarge)
            Text(text = stringResource(id = entry.type.titleRes), style = MaterialTheme.typography.bodyMedium)
        }
        Text(text = formatDetailValue(entry), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun formatDetailValue(entry: MeasurementEntry): String = when (entry.type) {
    MeasurementType.HEART_RATE -> entry.details.primaryValue?.let { "%.0f уд/мин".format(it) } ?: "—"
    MeasurementType.STEPS -> Formatters.formatInt(entry.details.primaryValue?.toLong() ?: 0L)
    MeasurementType.CALORIES -> Formatters.formatInt(entry.details.primaryValue?.toLong() ?: 0L) + " ккал"
    MeasurementType.BLOOD_PRESSURE -> {
        val sys = entry.details.primaryValue?.toInt()
        val dia = entry.details.secondaryValue?.toInt()
        if (sys != null && dia != null) "$sys/$dia мм рт. ст." else "—"
    }
    MeasurementType.WEIGHT -> entry.details.primaryValue?.let { "%.1f кг".format(it) } ?: "—"
    MeasurementType.OXYGEN -> entry.details.primaryValue?.let { "%.0f %%".format(it) } ?: "—"
    MeasurementType.SLEEP -> entry.details.durationMinutes?.let { Formatters.formatDurationMinutes(it) } ?: "—"
    MeasurementType.RESPIRATORY -> entry.details.primaryValue?.let { "%.0f вдох/мин".format(it) } ?: (entry.details.statusText ?: "—")
}

private fun chartColor(type: MeasurementType): Color = when (type) {
    MeasurementType.HEART_RATE -> Color(0xFFEF4444)
    MeasurementType.STEPS -> Color(0xFF10B981)
    MeasurementType.CALORIES -> Color(0xFFFB923C)
    MeasurementType.BLOOD_PRESSURE -> Color(0xFF6366F1)
    MeasurementType.WEIGHT -> Color(0xFF3B82F6)
    MeasurementType.OXYGEN -> Color(0xFF14B8A6)
    MeasurementType.SLEEP -> Color(0xFF6366F1)
    MeasurementType.RESPIRATORY -> Color(0xFF22C55E)
}

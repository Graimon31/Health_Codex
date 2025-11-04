// app/src/main/java/com/example/healthcodex/ui/measurements/MeasurementDetailScreen.kt
package com.example.healthcodex.ui.measurements

import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
import kotlin.math.max
import kotlin.math.min

/**
 * Navigation entry for the measurement detail screen.
 */
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
                title = {
                    Text(text = stringResource(id = R.string.measure_detail_title))
                },
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
        MeasurementDetailContent(
            state = state,
            paddingValues = innerPadding
        )
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
                item {
                    DetailHeroCard(entry = state.entry)
                }
                item {
                    MeasurementChartSection(entry = state.entry, history = state.dayEntries)
                }
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
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = stringResource(id = entry.type.titleRes), style = MaterialTheme.typography.titleLarge)
            Text(
                text = formatDetailValue(entry),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        tonalElevation = 2.dp,
        shape = CardDefaults.shape
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = stringResource(id = R.string.measure_detail_chart_title), style = MaterialTheme.typography.titleMedium)
            when (entry.type) {
                MeasurementType.STEPS, MeasurementType.CALORIES -> BarChart(entries = history, type = entry.type)
                MeasurementType.BLOOD_PRESSURE -> PressureChart(entries = history)
                MeasurementType.SLEEP -> SleepSummary(entry = entry)
                else -> LineChart(entries = history, type = entry.type)
            }
        }
    }
}

@Composable
private fun LineChart(entries: List<MeasurementEntry>, type: MeasurementType) {
    val values = entries.mapNotNull { it.details.primaryValue?.toFloat() }
    val placeholder = stringResource(id = R.string.measure_detail_chart_placeholder)
    Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
        if (values.isEmpty()) {
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    placeholder,
                    size.width / 4,
                    size.height / 2,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 36f
                    }
                )
            }
            return@Canvas
        }
        val minValue = values.minOrNull() ?: 0f
        val maxValue = values.maxOrNull() ?: 0f
        val range = max(1f, maxValue - minValue)
        val color = chartColor(type)
        if (values.size == 1) {
            val value = values.first()
            val y = size.height - ((value - minValue) / range) * size.height
            drawCircle(color = color, radius = 10f, center = Offset(size.width / 2f, y))
            return@Canvas
        }
        val path = Path()
        values.forEachIndexed { index, value ->
            val x = size.width * (index / (values.size - 1f))
            val y = size.height - ((value - minValue) / range) * size.height
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        drawPath(path = path, color = color, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f))
    }
}

@Composable
private fun BarChart(entries: List<MeasurementEntry>, type: MeasurementType) {
    val values = entries.mapNotNull { it.details.primaryValue?.toFloat() }
    val placeholder = stringResource(id = R.string.measure_detail_chart_placeholder)
    Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
        if (values.isEmpty()) {
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    placeholder,
                    size.width / 4,
                    size.height / 2,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 36f
                    }
                )
            }
            return@Canvas
        }
        val barWidth = size.width / (values.size * 1.5f)
        val maxValue = max(1f, values.maxOrNull() ?: 1f)
        val color = chartColor(type)
        values.forEachIndexed { index, value ->
            val x = index * (barWidth * 1.5f)
            val barHeight = (value / maxValue) * size.height
            drawRect(
                color = color,
                topLeft = androidx.compose.ui.geometry.Offset(x, size.height - barHeight),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
            )
        }
    }
}

@Composable
private fun PressureChart(entries: List<MeasurementEntry>) {
    val systolic = entries.mapNotNull { it.details.primaryValue?.toFloat() }
    val diastolic = entries.mapNotNull { it.details.secondaryValue?.toFloat() }
    val placeholder = stringResource(id = R.string.measure_detail_chart_placeholder)
    Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
        if (systolic.isEmpty() || diastolic.isEmpty()) {
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    placeholder,
                    size.width / 4,
                    size.height / 2,
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 36f
                    }
                )
            }
            return@Canvas
        }
        val minValue = min(systolic.minOrNull() ?: 0f, diastolic.minOrNull() ?: 0f)
        val maxValue = max(systolic.maxOrNull() ?: 0f, diastolic.maxOrNull() ?: 0f)
        val range = max(1f, maxValue - minValue)
        val sysPath = Path()
        val diaPath = Path()
        if (systolic.size == 1 && diastolic.size == 1) {
            val sysY = size.height - ((systolic.first() - minValue) / range) * size.height
            val diaY = size.height - ((diastolic.first() - minValue) / range) * size.height
            val centerX = size.width / 2f
            drawCircle(color = Color(0xFFEF4444), radius = 10f, center = Offset(centerX, sysY))
            drawCircle(color = Color(0xFF3B82F6), radius = 10f, center = Offset(centerX, diaY))
            return@Canvas
        }
        systolic.forEachIndexed { index, value ->
            val x = if (systolic.size == 1) 0f else size.width * (index / (systolic.size - 1f))
            val y = size.height - ((value - minValue) / range) * size.height
            if (index == 0) sysPath.moveTo(x, y) else sysPath.lineTo(x, y)
        }
        diastolic.forEachIndexed { index, value ->
            val x = if (diastolic.size == 1) 0f else size.width * (index / (diastolic.size - 1f))
            val y = size.height - ((value - minValue) / range) * size.height
            if (index == 0) diaPath.moveTo(x, y) else diaPath.lineTo(x, y)
        }
        drawPath(sysPath, color = Color(0xFFEF4444), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5f))
        drawPath(diaPath, color = Color(0xFF3B82F6), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5f))
    }
}

@Composable
private fun SleepSummary(entry: MeasurementEntry) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(
                id = R.string.measure_detail_sleep_summary,
                entry.startTimestamp?.let { Formatters.formatInstant(it) } ?: "—",
                entry.details.endInstant?.let { Formatters.formatInstant(it) } ?: Formatters.formatInstant(entry.timestamp),
                entry.details.durationMinutes?.let { Formatters.formatDurationMinutes(it) } ?: "—"
            ),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun HistoryRow(entry: MeasurementEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
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

private fun formatDetailValue(entry: MeasurementEntry): String = when (entry.type) {
    MeasurementType.HEART_RATE -> entry.details.primaryValue?.let { String.format("%.0f уд/мин", it) } ?: "—"
    MeasurementType.STEPS -> Formatters.formatInt(entry.details.primaryValue?.toLong() ?: 0L)
    MeasurementType.CALORIES -> Formatters.formatInt(entry.details.primaryValue?.toLong() ?: 0L) + " ккал"
    MeasurementType.BLOOD_PRESSURE -> {
        val sys = entry.details.primaryValue?.toInt()
        val dia = entry.details.secondaryValue?.toInt()
        if (sys != null && dia != null) "$sys/$dia мм рт. ст." else "—"
    }
    MeasurementType.WEIGHT -> entry.details.primaryValue?.let { String.format("%.1f кг", it) } ?: "—"
    MeasurementType.OXYGEN -> entry.details.primaryValue?.let { String.format("%.0f %%", it) } ?: "—"
    MeasurementType.SLEEP -> entry.details.durationMinutes?.let { Formatters.formatDurationMinutes(it) } ?: "—"
    MeasurementType.RESPIRATORY -> entry.details.primaryValue?.let { String.format("%.0f вдох/мин", it) } ?: (entry.details.statusText ?: "—")
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

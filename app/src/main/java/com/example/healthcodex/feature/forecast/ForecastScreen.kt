// app/src/main/java/com/example/healthcodex/feature/forecast/ForecastScreen.kt
package com.example.healthcodex.feature.forecast

import android.app.Application
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.roundToInt

/**
 * Entry point for the forecast screen to be used inside navigation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForecastRoute(
    paddingValues: PaddingValues = PaddingValues(),
    onFillProfile: () -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val viewModel: ForecastViewModel = viewModel(factory = ForecastViewModel.factory(application))
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Прогноз") }) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        if (state.isLoading) {
            LoadingState(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(paddingValues)
            )
        } else {
            ForecastContent(
                forecast = state.forecast,
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(paddingValues),
                onFillProfile = onFillProfile
            )
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ForecastContent(
    forecast: HealthForecast,
    modifier: Modifier = Modifier,
    onFillProfile: () -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { ForecastSummaryCard(forecast) }
        if (forecast.profileMissing) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Нет данных для прогноза",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Добавьте рост, вес и диагнозы, чтобы система могла рассчитать прогноз.")
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(onClick = onFillProfile) {
                            Text("Перейти к заполнению профиля")
                        }
                    }
                }
            }
        } else {
            if (forecast.positiveInsights.isNotEmpty()) {
                item { SectionHeader("Что сейчас хорошо") }
                items(forecast.positiveInsights) { insight ->
                    InsightCard(insight = insight)
                }
            }
            if (forecast.riskInsights.isNotEmpty()) {
                item { SectionHeader("Что требует внимания") }
                items(forecast.riskInsights) { insight ->
                    InsightCard(insight = insight)
                }
            }
            if (forecast.recommendations.isNotEmpty()) {
                item { SectionHeader("Рекомендации на ближайшее время") }
                item {
                    Card {
                        Column(modifier = Modifier.padding(16.dp)) {
                            forecast.recommendations.forEachIndexed { index, recommendation ->
                                Text(text = "• $recommendation", style = MaterialTheme.typography.bodyMedium)
                                if (index != forecast.recommendations.lastIndex) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ForecastSummaryCard(forecast: HealthForecast) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = forecast.headline,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = forecast.detail, style = MaterialTheme.typography.bodyMedium)
            if (!forecast.profileMissing && forecast.riskProbability != null) {
                val percent = (forecast.riskProbability * 100).roundToInt().coerceIn(0, 100)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Вероятность риска: $percent%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = forecast.riskProbability.toFloat().coerceIn(0f, 1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = when {
                        percent < 33 -> Color(0xFF2E7D32)
                        percent < 66 -> Color(0xFFF57F17)
                        else -> Color(0xFFC62828)
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (forecast.usedTflite) "Прогноз с использованием нейросети" else "Прогноз по эвристике профиля",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!forecast.profileMissing) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            if (forecast.usedTflite) {
                                "Прогноз с использованием нейросети"
                            } else {
                                "Обновлено на основе профиля"
                            }
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Filled.Info, contentDescription = null)
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun InsightCard(insight: WellnessInsight) {
    val (icon, tint) = when (insight.severity) {
        InsightSeverity.POSITIVE -> Icons.Filled.CheckCircle to Color(0xFF2E7D32)
        InsightSeverity.WARNING -> Icons.Filled.Warning to Color(0xFFF57F17)
        InsightSeverity.CRITICAL -> Icons.Filled.Warning to Color(0xFFC62828)
    }
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = tint)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = insight.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = insight.message, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

package com.example.healthcodex.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun LiquidGlassBackdrop(modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(
                        scheme.background,
                        scheme.primary.copy(alpha = 0.26f),
                        scheme.secondary.copy(alpha = 0.32f)
                    )
                )
            )
    )
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.30f),
                        Color.Transparent
                    )
                )
            )
    )
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        scheme.primary.copy(alpha = 0.22f),
                        Color.Transparent
                    )
                )
            )
    )
}

@Composable
fun LiquidGlassSurface(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(26.dp)
    Surface(
        modifier = modifier
            .shadow(16.dp, shape = shape, ambientColor = Color.White.copy(alpha = 0.20f))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.50f),
                shape = shape
            ),
        shape = shape,
        color = Color.Transparent,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.84f),
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.66f)
                        )
                    )
                )
                .padding(1.dp)
        ) {
            content()
        }
    }
}

val GlassPositive = Color(0xFF6BDAA0)
val GlassWarning = Color(0xFFFFC56C)
val GlassCritical = Color(0xFFFF8888)

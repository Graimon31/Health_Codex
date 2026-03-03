package com.example.healthcodex.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
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
                        scheme.primary.copy(alpha = 0.20f),
                        scheme.secondary.copy(alpha = 0.25f)
                    )
                )
            )
    )
}

@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    color: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
    contentColor: Color = contentColorFor(color),
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
                shape = shape
            ),
        shape = shape,
        color = color,
        contentColor = contentColor,
        tonalElevation = 2.dp,
        shadowElevation = 0.dp,
        content = content
    )
}

val GlassPositive = Color(0xFF6BDAA0)
val GlassWarning = Color(0xFFFFC56C)
val GlassCritical = Color(0xFFFF8888)

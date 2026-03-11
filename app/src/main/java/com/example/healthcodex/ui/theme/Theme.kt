// app/src/main/java/com/example/healthcodex/ui/theme/Theme.kt
package com.example.healthcodex.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    secondary = md_theme_light_secondary,
    background = md_theme_light_background
)

private val DarkColors = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    secondary = md_theme_dark_secondary,
    background = md_theme_dark_background
)

/**
 * Material 3 colour scheme for the Liquid Glass design language.
 *
 * Surfaces and backgrounds are set to fully transparent so the cosmic gradient
 * painted by [LiquidGlassBackdrop] shows through every screen.
 */
private val GlassColorScheme = darkColorScheme(
    primary = GlassButtonStart,
    onPrimary = Color.White,
    primaryContainer = GlassSurface,
    onPrimaryContainer = GlassTextPrimary,
    secondary = GlassButtonEnd,
    onSecondary = Color.White,
    secondaryContainer = GlassSurface,
    onSecondaryContainer = GlassTextPrimary,
    tertiary = GlassChipBlue,
    onTertiary = Color.White,
    background = Color.Transparent,
    onBackground = GlassTextPrimary,
    surface = GlassSurface,
    onSurface = GlassTextPrimary,
    surfaceVariant = GlassSurfaceFallback,
    onSurfaceVariant = GlassTextSecondary,
    outline = GlassBorderLight,
    outlineVariant = GlassBorderDim
)

/** Rounded shapes aligned with the Liquid Glass design spec. */
private val GlassShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(32.dp),
    extraLarge = RoundedCornerShape(50)
)

/**
 * Primary theme for Health Codex.
 *
 * Always applies the Liquid Glass design language regardless of system dark-mode setting,
 * because the cosmic background requires a dark colour scheme. Pass [useGlass] = false
 * to fall back to the standard Material 3 light/dark palette (useful for screenshots or
 * design variants).
 *
 * @param useGlass Whether to use the Liquid Glass design language (default: true)
 * @param darkTheme Only relevant when [useGlass] is false
 * @param content Content slot
 */
@Composable
fun HealthCodexTheme(
    useGlass: Boolean = true,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        useGlass -> GlassColorScheme
        darkTheme -> DarkColors
        else -> LightColors
    }
    val shapes = if (useGlass) GlassShapes else MaterialTheme.shapes

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = shapes,
        typography = MaterialTheme.typography,
        content = content
    )
}

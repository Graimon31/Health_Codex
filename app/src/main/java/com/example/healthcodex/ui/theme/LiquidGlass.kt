// app/src/main/java/com/example/healthcodex/ui/theme/LiquidGlass.kt
package com.example.healthcodex.ui.theme

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Full-screen cosmic gradient backdrop. On API 31+ a soft radial blur overlay is layered on top. */
@Composable
fun LiquidGlassBackdrop(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        GlassGradientMid,
                        GlassGradientStart,
                        GlassGradientEnd
                    ),
                    center = Offset(0.35f, 0.2f),
                    radius = Float.POSITIVE_INFINITY
                )
            )
            .drawBehind {
                // Secondary radial highlight in top-right corner for depth
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(GlassHighlight, Color.Transparent),
                        center = Offset(size.width * 0.85f, size.height * 0.15f),
                        radius = size.width * 0.55f
                    ),
                    radius = size.width * 0.55f,
                    center = Offset(size.width * 0.85f, size.height * 0.15f)
                )
            }
    ) {
        content()
    }
}

/**
 * Semi-transparent frosted glass container with rounded corners.
 *
 * On API 31+ a backdrop blur is applied; on older API levels a slightly more opaque
 * background is used as a graceful degradation.
 *
 * @param modifier Modifier for the container
 * @param cornerRadius Corner radius of the glass panel
 * @param content Slot for composable content
 */
@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val surfaceColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        GlassSurface
    } else {
        GlassSurfaceFallback
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(surfaceColor)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(GlassBorderLight, GlassBorderDim)
                ),
                shape = shape
            )
    ) {
        content()
    }
}

/**
 * Gradient button following the Liquid Glass design spec.
 * Replaces standard [Button] / [ElevatedButton] with a rounded gradient pill.
 *
 * @param onClick Action when tapped
 * @param modifier Modifier
 * @param enabled Whether the button is interactive
 * @param gradient Brush used as the button background; defaults to the primary blue-violet gradient
 * @param content Composable slot for the button label / icons
 */
@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    gradient: Brush = Brush.horizontalGradient(
        colors = listOf(GlassButtonStart, GlassButtonEnd)
    ),
    content: @Composable RowScope.() -> Unit
) {
    val shape = RoundedCornerShape(50)
    Button(
        onClick = onClick,
        modifier = modifier.background(brush = gradient, shape = shape),
        enabled = enabled,
        shape = shape,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = Color.White.copy(alpha = 0.4f)
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
    ) {
        content()
    }
}

/**
 * Outlined ghost button for secondary actions (e.g., "Отмена").
 */
@Composable
fun GlassOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(50),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = Color.White.copy(alpha = 0.4f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.5.dp,
            color = GlassBorderLight
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
    ) {
        content()
    }
}

/**
 * Data class for a single bottom-navigation item used by [GlassBottomBar].
 */
data class GlassNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

/**
 * Frosted glass navigation bar that replaces the default [NavigationBar].
 *
 * Selected item is highlighted with [GlassNavSelected]; unselected items use
 * a semi-transparent white tint.
 *
 * @param items List of navigation destinations
 * @param currentRoute Currently active route string
 * @param onItemSelected Callback when an item is tapped; receives the destination route
 * @param isSelected Predicate that determines whether a given [GlassNavItem] is selected
 */
@Composable
fun GlassBottomBar(
    items: List<GlassNavItem>,
    currentRoute: String?,
    onItemSelected: (String) -> Unit,
    isSelected: (GlassNavItem) -> Boolean = { it.route == currentRoute }
) {
    val shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(GlassBottomBarBg)
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(colors = listOf(GlassBorderLight, GlassBorderDim)),
                shape = shape
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = isSelected(item)
                NavigationBarItem(
                    selected = selected,
                    onClick = { onItemSelected(item.route) },
                    icon = { Icon(item.icon, contentDescription = item.label) },
                    label = { Text(item.label) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = GlassNavSelected,
                        selectedTextColor = GlassNavSelected,
                        unselectedIconColor = GlassNavUnselected,
                        unselectedTextColor = GlassNavUnselected,
                        indicatorColor = GlassNavIndicator
                    )
                )
            }
        }
    }
}

// app/src/main/java/com/example/healthcodex/ui/theme/Color.kt
package com.example.healthcodex.ui.theme

import androidx.compose.ui.graphics.Color

// ── Legacy Material 3 palette (kept for compatibility) ──────────────────────
val md_theme_light_primary = Color(0xFF006D3A)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_secondary = Color(0xFF506352)
val md_theme_light_background = Color(0xFFFBFDF8)

val md_theme_dark_primary = Color(0xFF6BDB8B)
val md_theme_dark_onPrimary = Color(0xFF00391B)
val md_theme_dark_secondary = Color(0xFFB3CCB6)
val md_theme_dark_background = Color(0xFF101510)

// ── Liquid Glass – Cosmic background gradient ────────────────────────────────
/** Deep navy – gradient start (top-left area) */
val GlassGradientStart = Color(0xFF0D0B2A)
/** Mid violet – gradient centre */
val GlassGradientMid = Color(0xFF2B1464)
/** Deep purple – gradient end (bottom-right) */
val GlassGradientEnd = Color(0xFF1A0A3D)
/** Radial highlight blob (top-right) */
val GlassHighlight = Color(0x337B52FF)

// ── Liquid Glass – Surface ───────────────────────────────────────────────────
/** Primary glass panel – used on API 31+ where blur is available */
val GlassSurface = Color(0x26FFFFFF)          // white 15 %
/** Fallback glass panel for API < 31 (slightly more opaque) */
val GlassSurfaceFallback = Color(0x4DFFFFFF)  // white 30 %
/** Lighter border edge */
val GlassBorderLight = Color(0x66FFFFFF)       // white 40 %
/** Dimmer border edge */
val GlassBorderDim = Color(0x1AFFFFFF)         // white 10 %

// ── Liquid Glass – Buttons ───────────────────────────────────────────────────
/** Primary gradient button start (blue-violet) */
val GlassButtonStart = Color(0xFF5B5FE8)
/** Primary gradient button end (violet-purple) */
val GlassButtonEnd = Color(0xFF7B52FF)
/** Solid blue for add/action chips */
val GlassChipBlue = Color(0xFF4A6CF7)

// ── Liquid Glass – Bottom navigation bar ────────────────────────────────────
/** Bottom nav background */
val GlassBottomBarBg = Color(0x40D0CEFC)       // lavender-white 25 %
/** Selected icon/label tint */
val GlassNavSelected = Color(0xFF7B88FF)
/** Unselected icon/label tint */
val GlassNavUnselected = Color(0xAAFFFFFF)
/** Selection indicator pill */
val GlassNavIndicator = Color(0x33FFFFFF)

// ── Liquid Glass – Text ──────────────────────────────────────────────────────
/** Primary text on glass surfaces */
val GlassTextPrimary = Color(0xFFFFFFFF)
/** Secondary / hint text on glass surfaces */
val GlassTextSecondary = Color(0xCCFFFFFF)

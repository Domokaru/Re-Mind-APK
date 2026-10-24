package com.hallauer.remind.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.hallauer.remind.data.local.entity.GroupBucket

// Global Dark Mode Surfaces (Default)
val DarkBackground = Color(0xFF0B0F19)
val DarkSurface = Color(0xFF111827)
val DarkSurfaceVariant = Color(0xFF1E293B)
val DarkPrimary = Color(0xFF08DAC9)
val DarkOnPrimary = Color(0xFF04201D)
val DarkTextPrimary = Color(0xFFF8FAFC)
val DarkTextSecondary = Color(0xFF94A3B8)

// Global Light Mode Surfaces
val LightBackground = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF1F5F9)
val LightPrimary = Color(0xFF069689)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightTextPrimary = Color(0xFF0F172A)
val LightTextSecondary = Color(0xFF64748B)

val DarkColorScheme = darkColorScheme(
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    onSurfaceVariant = DarkTextSecondary
)

val LightColorScheme = lightColorScheme(
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary,
    onSurfaceVariant = LightTextSecondary
)

/**
 * Returns the adaptive high-contrast color for each GroupBucket based on the active theme mode.
 */
@Composable
fun GroupBucket.toColor(isDarkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme()): Color {
    val isDark = isDarkTheme // Or read from SettingsDataStore
    return when (this) {
        GroupBucket.SPOUSE -> if (isDark) Color(0xFFE11D48) else Color(0xFFBE123C)       // Crimson / Warm Rose
        GroupBucket.FAMILY -> if (isDark) Color(0xFFD97706) else Color(0xFFB45309)       // Deep Amber / Gold
        GroupBucket.FRIENDS -> if (isDark) Color(0xFF2563EB) else Color(0xFF1D4ED8)      // Royal Blue
        GroupBucket.ACQUAINTANCES -> if (isDark) Color(0xFF0D9488) else Color(0xFF0F766E)// Emerald / Teal
        GroupBucket.OTHERS -> if (isDark) Color(0xFF7C3AED) else Color(0xFF6D28D9)       // Slate Purple
        GroupBucket.COLLEAGUES -> if (isDark) Color(0xFF06B6D4) else Color(0xFF0891B2)   // Cyan / Sky
        GroupBucket.MENTORS -> if (isDark) Color(0xFF8B5CF6) else Color(0xFF7C3AED)      // Violet
        GroupBucket.CUSTOM -> if (isDark) Color(0xFFF59E0B) else Color(0xFFD97706)       // Amber
    }
}

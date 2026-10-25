package com.hallauer.remind.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Analytics)
    data object Calendar : Screen("calendar", "Calendar", Icons.Default.CalendarMonth)
    data object People : Screen("people", "People", Icons.Default.People)
    data object Phrases : Screen("phrases", "Phrases", Icons.Default.FormatQuote)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

val BottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Calendar,
    Screen.People,
    Screen.Phrases
)

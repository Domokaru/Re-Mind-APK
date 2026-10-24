package com.hallauer.remind.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.People
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val DashboardAnalyticsIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "DashboardAnalytics",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        // Outer Board frame
        path(
            stroke = SolidColor(Color.White),
            strokeLineWidth = 1.6f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(3f, 5f)
            lineTo(21f, 5f)
            lineTo(21f, 19f)
            lineTo(13f, 19f)
            moveTo(3f, 12f)
            lineTo(3f, 5f)
        }

        // Line graph with nodes across the board
        path(
            stroke = SolidColor(Color.White),
            strokeLineWidth = 1.6f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(4.5f, 11f)
            lineTo(7.5f, 7.5f)
            lineTo(10.5f, 9.5f)
            lineTo(14.5f, 7f)
        }

        // Upper-right list dashes
        path(
            stroke = SolidColor(Color.White),
            strokeLineWidth = 1.4f,
            strokeLineCap = StrokeCap.Round
        ) {
            moveTo(17f, 7.5f)
            lineTo(19.5f, 7.5f)
            moveTo(17f, 10f)
            lineTo(19.5f, 10f)
        }

        // Bar chart columns on lower right
        path(
            stroke = SolidColor(Color.White),
            strokeLineWidth = 1.6f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(14.5f, 18f)
            lineTo(14.5f, 12.5f)
            moveTo(17f, 18f)
            lineTo(17f, 14f)
            moveTo(19.5f, 18f)
            lineTo(19.5f, 15.5f)
        }

        // Pie chart on lower left
        path(
            stroke = SolidColor(Color.White),
            strokeLineWidth = 1.6f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        ) {
            moveTo(7f, 12.5f)
            arcTo(4f, 4f, 0f, isMoreThanHalf = true, isPositiveArc = true, 7f, 20.5f)
            arcTo(4f, 4f, 0f, isMoreThanHalf = true, isPositiveArc = true, 7f, 12.5f)
            moveTo(7f, 16.5f)
            lineTo(7f, 12.5f)
            moveTo(7f, 16.5f)
            lineTo(4.2f, 18.5f)
            moveTo(7f, 16.5f)
            lineTo(10.5f, 17.5f)
        }
    }.build()
}

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Dashboard : Screen("dashboard", "Dashboard", DashboardAnalyticsIcon)
    data object Calendar : Screen("calendar", "Calendar", Icons.Default.CalendarMonth)
    data object People : Screen("people", "People", Icons.Default.People)
    data object Phrases : Screen("phrases", "Phrases", Icons.Default.FormatQuote)
    data object Settings : Screen("settings", "Settings", Icons.Default.Dashboard)
}

val BottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Calendar,
    Screen.People,
    Screen.Phrases
)

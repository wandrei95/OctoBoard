package com.test.kmp.octoboard.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF6750A4),
    secondary = Color(0xFF625B71),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    secondary = Color(0xFFCCC2DC),
)

object OctoBoardTheme {
    val dimensions: OctoBoardDimensions
        @Composable get() = LocalOctoBoardDimensions.current
}

@Composable
fun OctoBoardTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalOctoBoardDimensions provides OctoBoardDimensions()) {
        MaterialTheme(
            colorScheme = if (isSystemInDarkTheme()) {
                DarkColors
            } else {
                LightColors
            },
            content = content,
        )
    }
}

package com.test.kmp.octoboard.core.designsystem

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class OctoBoardDimensions(
    val spacingExtraSmall: Dp = 4.dp,
    val spacingSmall: Dp = 8.dp,
    val spacingMedium: Dp = 12.dp,
    val spacingLarge: Dp = 16.dp,
    val spacingExtraLarge: Dp = 24.dp,

    val iconSizeSmall: Dp = 24.dp,
    val avatarSizeMedium: Dp = 40.dp,

    val cornerRadiusSmall: Dp = 4.dp,
    val cornerRadiusMedium: Dp = 8.dp,
)

internal val LocalOctoBoardDimensions = staticCompositionLocalOf { OctoBoardDimensions() }

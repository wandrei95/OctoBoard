package com.test.kmp.octoboard.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import coil3.compose.AsyncImage

private val fallbackColors = listOf(
    Color(0xFF6750A4),
    Color(0xFF386A20),
    Color(0xFF9C4146),
    Color(0xFF006874),
    Color(0xFF8B5000),
    Color(0xFF52633F),
)

private fun fallbackColorFor(seed: String): Color =
    fallbackColors[seed.hashCode().mod(fallbackColors.size)]

@Composable
fun Avatar(
    imageUrl: String?,
    fallbackLabel: String,
    modifier: Modifier = Modifier,
    size: Dp = OctoBoardTheme.dimensions.avatarSizeMedium,
) {
    var loadFailed by remember(imageUrl) { mutableStateOf(false) }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(fallbackColorFor(fallbackLabel)),
        contentAlignment = Alignment.Center,
    ) {
        if (!imageUrl.isNullOrBlank() && !loadFailed) {
            AsyncImage(
                model = imageUrl,
                contentDescription = fallbackLabel,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                onError = { loadFailed = true },
            )
        } else {
            Text(
                text = fallbackLabel.take(1).uppercase(),
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

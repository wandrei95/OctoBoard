package com.test.kmp.octoboard.integration.navigation

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.test.kmp.octoboard.core.designsystem.OctoBoardTheme

@Composable
fun IntegrationRoot() {
    OctoBoardTheme {
        OctoBoardNavHost()
    }
}

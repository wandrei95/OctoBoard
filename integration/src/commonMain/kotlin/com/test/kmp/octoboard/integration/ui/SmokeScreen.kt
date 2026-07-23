package com.test.kmp.octoboard.integration.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.test.kmp.octoboard.integration.presentation.SmokeUiState
import com.test.kmp.octoboard.integration.presentation.SmokeViewModel

@Composable
fun SmokeScreen(viewModel: SmokeViewModel) {
    MaterialTheme {
        val state by viewModel.uiState.collectAsState()

        Column(modifier = Modifier.fillMaxSize().safeContentPadding().padding(16.dp)) {
            when (val current = state) {
                is SmokeUiState.Loading -> Text("Loading JetBrains/kotlin from GitHub...")
                is SmokeUiState.Success -> Text(
                    "${current.repo.fullName}\n${current.repo.description}\n★ ${current.repo.stargazersCount}"
                )
                is SmokeUiState.Error -> Text("Error: ${current.message}")
            }
        }
    }
}

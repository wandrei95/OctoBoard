package com.test.kmp.octoboard.integration.navigation

import androidx.compose.runtime.Composable
import com.test.kmp.octoboard.integration.presentation.SmokeViewModel
import com.test.kmp.octoboard.integration.ui.SmokeScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun IntegrationRoot() {
    val viewModel = koinViewModel<SmokeViewModel>()
    SmokeScreen(viewModel)
}

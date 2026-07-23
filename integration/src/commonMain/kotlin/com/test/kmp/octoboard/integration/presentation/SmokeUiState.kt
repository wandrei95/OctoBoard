package com.test.kmp.octoboard.integration.presentation

import com.test.kmp.octoboard.integration.entities.SmokeRepo

sealed interface SmokeUiState {
    data object Loading : SmokeUiState
    data class Success(val repo: SmokeRepo) : SmokeUiState
    data class Error(val message: String) : SmokeUiState
}

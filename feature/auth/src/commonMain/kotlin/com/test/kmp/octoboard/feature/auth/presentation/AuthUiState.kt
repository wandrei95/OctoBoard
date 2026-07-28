package com.test.kmp.octoboard.feature.auth.presentation

data class AuthUiState(
    val tokenInput: String = "",
    val hasStoredToken: Boolean = false,
    val justSaved: Boolean = false,
)

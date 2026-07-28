package com.test.kmp.octoboard.feature.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.test.kmp.octoboard.feature.auth.data.SecureStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val secureStorage: SecureStorage,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(hasStoredToken = secureStorage.getToken() != null) }
        }
    }

    fun onTokenInputChange(value: String) {
        _uiState.update { it.copy(tokenInput = value, justSaved = false) }
    }

    fun onSave() {
        val token = _uiState.value.tokenInput.trim()
        if (token.isEmpty()) return

        viewModelScope.launch {
            secureStorage.saveToken(token)
            _uiState.update {
                it.copy(
                    tokenInput = "",
                    hasStoredToken = true,
                    justSaved = true,
                )
            }
        }
    }

    fun onClear() {
        viewModelScope.launch {
            secureStorage.clearToken()
            _uiState.update {
                it.copy(
                    hasStoredToken = false,
                    justSaved = false,
                )
            }
        }
    }
}

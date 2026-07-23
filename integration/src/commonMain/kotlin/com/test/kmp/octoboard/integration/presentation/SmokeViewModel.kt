package com.test.kmp.octoboard.integration.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.test.kmp.octoboard.integration.data.SmokeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SmokeViewModel(private val repository: SmokeRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<SmokeUiState>(SmokeUiState.Loading)
    val uiState: StateFlow<SmokeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = try {
                SmokeUiState.Success(repository.fetchKotlinRepo())
            } catch (e: Exception) {
                SmokeUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

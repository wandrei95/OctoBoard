package com.test.kmp.octoboard.feature.favorites.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.test.kmp.octoboard.feature.favorites.data.repositories.FavoritesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class FavoritesViewModel(
    private val repository: FavoritesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    init {
        observe()
    }

    fun onRetry() = observe()

    private fun observe() {
        observeJob?.cancel()
        _uiState.update { it.copy(status = FavoritesStatus.Loading) }
        observeJob = viewModelScope.launch {
            repository.observeFavorites().collect { favorites ->
                val status = when {
                    favorites.repos.isNotEmpty() -> FavoritesStatus.Content(
                        repos = favorites.repos,
                        failedCount = favorites.failedCount,
                    )
                    favorites.failedCount > 0 -> FavoritesStatus.Error
                    else -> FavoritesStatus.Empty
                }
                _uiState.update { it.copy(status = status) }
            }
        }
    }
}

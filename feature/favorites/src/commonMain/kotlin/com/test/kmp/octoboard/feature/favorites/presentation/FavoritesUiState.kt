package com.test.kmp.octoboard.feature.favorites.presentation

import com.test.kmp.octoboard.shared.entities.GithubRepository

internal sealed interface FavoritesStatus {
    data object Loading : FavoritesStatus
    data object Empty : FavoritesStatus
    data class Content(
        val repos: List<GithubRepository>,
        val failedCount: Int,
    ) : FavoritesStatus
    // Every saved favorite failed to load.
    data object Error : FavoritesStatus
}

internal data class FavoritesUiState(
    val status: FavoritesStatus = FavoritesStatus.Loading,
)

package com.test.kmp.octoboard.feature.search.presentation

import com.test.kmp.octoboard.feature.search.entities.RepoSummary
import com.test.kmp.octoboard.feature.search.entities.SearchMode
import com.test.kmp.octoboard.feature.search.entities.SearchSort
import com.test.kmp.octoboard.feature.search.entities.UserSummary

sealed interface SearchStatus {
    data object Idle : SearchStatus
    data object Loading : SearchStatus
    data object Empty : SearchStatus
    data object Content : SearchStatus
    data class Error(val message: String) : SearchStatus
}

sealed interface SearchResults {
    data class RepositoryResults(val items: List<RepoSummary> = emptyList()) : SearchResults
    data class UserResults(val items: List<UserSummary> = emptyList()) : SearchResults
}

data class SearchUiState(
    val query: String = "",
    val mode: SearchMode = SearchMode.Repositories,
    val sort: SearchSort = SearchSort.BestMatch,
    val language: String = "",
    val status: SearchStatus = SearchStatus.Idle,
    val results: SearchResults = SearchResults.RepositoryResults(),
    val isLoadingMore: Boolean = false,
)

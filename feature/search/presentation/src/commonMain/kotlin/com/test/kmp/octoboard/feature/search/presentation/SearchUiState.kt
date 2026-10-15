package com.test.kmp.octoboard.feature.search.presentation

import com.test.kmp.octoboard.feature.search.entities.GithubUserSummary
import com.test.kmp.octoboard.feature.search.entities.SearchMode
import com.test.kmp.octoboard.feature.search.entities.SearchSort
import com.test.kmp.octoboard.shared.entities.GithubRepository

internal sealed interface SearchStatus {
    data object Idle : SearchStatus
    data object Loading : SearchStatus
    data object Empty : SearchStatus
    data object Content : SearchStatus
    data class Error(val message: String) : SearchStatus
}

internal sealed interface SearchResults {
    data class GithubRepositorySearchResults(val items: List<GithubRepository> = emptyList()) : SearchResults
    data class GithubUserSearchResults(val items: List<GithubUserSummary> = emptyList()) : SearchResults
}

internal data class SearchUiState(
    val query: String = "",
    val mode: SearchMode = SearchMode.Repositories,
    val sort: SearchSort = SearchSort.BestMatch,
    val language: String = "",
    val status: SearchStatus = SearchStatus.Idle,
    val results: SearchResults = SearchResults.GithubRepositorySearchResults(),
    val isLoadingMore: Boolean = false,
)

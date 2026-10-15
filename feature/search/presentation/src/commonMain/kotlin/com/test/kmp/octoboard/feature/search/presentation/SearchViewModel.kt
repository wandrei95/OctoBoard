package com.test.kmp.octoboard.feature.search.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.test.kmp.octoboard.feature.search.data.repositories.SearchRepository
import com.test.kmp.octoboard.feature.search.entities.SearchMode
import com.test.kmp.octoboard.feature.search.entities.SearchSort
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val SEARCH_DEBOUNCE_MS = 300L
private const val MIN_QUERY_LENGTH = 2

internal class SearchViewModel(private val repository: SearchRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var currentPage: Int = 1
    private var hasNextPage: Boolean = false

    fun onQueryChange(text: String) {
        _uiState.update { it.copy(query = text) }
        search(
            debounce = true,
            resetPage = true,
        )
    }

    fun onModeChange(newMode: SearchMode) {
        _uiState.update { it.copy(mode = newMode) }
        search(resetPage = true)
    }

    fun onSortChange(newSort: SearchSort) {
        _uiState.update { it.copy(sort = newSort) }
        search(resetPage = true)
    }

    fun onLanguageFilterChange(text: String) {
        _uiState.update { it.copy(language = text) }
        search(resetPage = true)
    }

    fun onLoadNextPage() {
        val state = _uiState.value
        if (state.status != SearchStatus.Content || state.isLoadingMore || !hasNextPage) {
            return
        }
        search(page = currentPage + 1)
    }

    fun onRetry() {
        search(page = currentPage)
    }

    // Only the free-text query is debounced (GitHub's search endpoints are limited to
    // 10 req/min unauthenticated, stricter than the general 60/hr, so this is load-bearing,
    // not just UX polish) — mode/sort/language/pagination changes search immediately.
    // Every call cancels whatever search is in flight, so a fast typist or a rapid filter
    // change can never have an older request land after a newer one.
    private fun search(
        debounce: Boolean = false,
        resetPage: Boolean = false,
        page: Int? = null,
    ) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (debounce) {
                delay(SEARCH_DEBOUNCE_MS)
            }
            val targetPage = page ?: if (resetPage) {
                1
            } else {
                currentPage
            }
            load(targetPage)
        }
    }

    private suspend fun load(pageNumber: Int) {
        val state = _uiState.value

        if (isQueryTooShort(state.query)) {
            resetToIdle()
            return
        }

        currentPage = pageNumber

        _uiState.update {
            it.copy(
                status = if (pageNumber == 1) {
                    SearchStatus.Loading
                } else {
                    it.status
                },
                isLoadingMore = pageNumber > 1,
            )
        }

        when (state.mode) {
            SearchMode.Repositories -> loadRepositories(
                state = state,
                pageNumber = pageNumber,
            )
            SearchMode.Users -> loadUsers(
                state = state,
                pageNumber = pageNumber,
            )
        }
    }

    private fun isQueryTooShort(query: String): Boolean = query.trim().length < MIN_QUERY_LENGTH

    private fun resetToIdle() {
        currentPage = 1
        hasNextPage = false
        _uiState.update {
            it.copy(
                status = SearchStatus.Idle,
                results = when (it.mode) {
                    SearchMode.Repositories -> SearchResults.GithubRepositorySearchResults()
                    SearchMode.Users -> SearchResults.GithubUserSearchResults()
                },
                isLoadingMore = false,
            )
        }
    }

    private suspend fun loadRepositories(
        state: SearchUiState,
        pageNumber: Int,
    ) {
        repository.searchRepositories(
            query = state.query,
            sort = state.sort,
            language = state.language.ifBlank { null },
            page = pageNumber,
        ).onSuccess { result ->
            hasNextPage = result.hasNextPage
            _uiState.update { current ->
                val previousItems = (current.results as? SearchResults.GithubRepositorySearchResults)?.items.orEmpty()
                val items = if (pageNumber == 1) {
                    result.items
                } else {
                    previousItems + result.items
                }
                current.copy(
                    status = if (items.isEmpty()) {
                        SearchStatus.Empty
                    } else {
                        SearchStatus.Content
                    },
                    results = SearchResults.GithubRepositorySearchResults(items),
                    isLoadingMore = false,
                )
            }
        }.onFailure(::handleSearchError)
    }

    private suspend fun loadUsers(
        state: SearchUiState,
        pageNumber: Int,
    ) {
        repository.searchUsers(
            query = state.query,
            page = pageNumber,
        )
            .onSuccess { result ->
                hasNextPage = result.hasNextPage
                _uiState.update { current ->
                    val previousItems = (current.results as? SearchResults.GithubUserSearchResults)?.items.orEmpty()
                    val items = if (pageNumber == 1) {
                        result.items
                    } else {
                        previousItems + result.items
                    }
                    current.copy(
                        status = if (items.isEmpty()) {
                            SearchStatus.Empty
                        } else {
                            SearchStatus.Content
                        },
                        results = SearchResults.GithubUserSearchResults(items),
                        isLoadingMore = false,
                    )
                }
            }.onFailure(::handleSearchError)
    }

    private fun handleSearchError(error: Throwable) {
        _uiState.update {
            it.copy(
                status = SearchStatus.Error(error.message ?: "Something went wrong"),
                isLoadingMore = false,
            )
        }
    }
}

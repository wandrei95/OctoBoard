package com.test.kmp.octoboard.feature.userprofile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.test.kmp.octoboard.core.common.entities.PagedResult
import com.test.kmp.octoboard.feature.userprofile.data.repositories.UserProfileRepository
import com.test.kmp.octoboard.shared.entities.GithubRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class UserProfileViewModel(
    private val repository: UserProfileRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    private var currentUsername: String? = null
    private var currentPage: Int = 1
    private var hasNextPage: Boolean = false
    private var loadJob: Job? = null
    private var loadMoreJob: Job? = null

    fun load(username: String) {
        currentUsername = username
        currentPage = 1
        hasNextPage = false

        loadJob?.cancel()
        loadMoreJob?.cancel()
        _uiState.update {
            it.copy(
                status = UserProfileStatus.Loading,
                isLoadingMore = false,
            )
        }
        loadJob = viewModelScope.launch {
            loadInternal(username)
        }
    }

    private suspend fun loadInternal(username: String) = coroutineScope {
        val detailsDeferred = async { repository.getUserDetails(username) }
        val reposDeferred = async {
            repository.getUserRepositories(
                username = username,
                page = 1,
            )
        }

        val status = detailsDeferred.await().fold(
            onSuccess = { details ->
                val reposResult = reposDeferred.await()
                    .getOrDefault(PagedResult(items = emptyList(), hasNextPage = false))
                hasNextPage = reposResult.hasNextPage
                UserProfileStatus.Content(
                    details = details,
                    repos = reposResult.items,
                )
            },
            onFailure = { error ->
                UserProfileStatus.Error(error.message ?: "Something went wrong")
            },
        )
        _uiState.update { it.copy(status = status) }
    }

    fun loadMoreRepos() {
        val username = currentUsername ?: return
        val state = _uiState.value
        if (state.status !is UserProfileStatus.Content || state.isLoadingMore || !hasNextPage) {
            return
        }

        loadMoreJob?.cancel()
        _uiState.update { it.copy(isLoadingMore = true) }
        loadMoreJob = viewModelScope.launch {
            val nextPage = currentPage + 1
            repository.getUserRepositories(
                username = username,
                page = nextPage,
            ).onSuccess { result ->
                currentPage = nextPage
                hasNextPage = result.hasNextPage
                _uiState.update { current -> current.appendRepos(result.items) }
            }.onFailure {
                _uiState.update { it.copy(isLoadingMore = false) }
            }
        }
    }

    private fun UserProfileUiState.appendRepos(
        newItems: List<GithubRepository>,
    ): UserProfileUiState {
        val content = status as? UserProfileStatus.Content ?: return copy(isLoadingMore = false)
        return copy(
            status = content.copy(repos = content.repos + newItems),
            isLoadingMore = false,
        )
    }

    fun onRetry() {
        val username = currentUsername ?: return
        load(username)
    }
}

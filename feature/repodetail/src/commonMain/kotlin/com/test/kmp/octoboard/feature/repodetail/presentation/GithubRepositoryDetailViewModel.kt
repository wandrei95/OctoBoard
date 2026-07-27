package com.test.kmp.octoboard.feature.repodetail.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.test.kmp.octoboard.feature.repodetail.data.repositories.GithubRepositoryDetailRepository
import com.test.kmp.octoboard.feature.repodetail.entities.GithubRepositoryLanguageBreakdown
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class GithubRepositoryDetailViewModel(
    private val repository: GithubRepositoryDetailRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(GithubRepositoryDetailUiState())
    val uiState: StateFlow<GithubRepositoryDetailUiState> = _uiState.asStateFlow()

    private var currentOwner: String? = null
    private var currentRepo: String? = null
    private var loadJob: Job? = null

    fun load(
        owner: String,
        repo: String,
    ) {
        currentOwner = owner
        currentRepo = repo

        loadJob?.cancel()
        _uiState.update { it.copy(status = GithubRepositoryDetailStatus.Loading) }
        loadJob = viewModelScope.launch {
            loadInternal(
                owner = owner,
                repo = repo,
            )
        }
    }

    private suspend fun loadInternal(
        owner: String,
        repo: String,
    ) = coroutineScope {
        val detailsDeferred = async {
            repository.getRepositoryDetails(
                owner = owner,
                repo = repo,
            )
        }
        val languagesDeferred = async {
            repository.getLanguages(
                owner = owner,
                repo = repo,
            )
        }
        val contributorsDeferred = async {
            repository.getContributors(
                owner = owner,
                repo = repo,
            )
        }

        val status = detailsDeferred.await().fold(
            onSuccess = { details ->
                GithubRepositoryDetailStatus.Content(
                    details = details,
                    languages = languagesDeferred.await()
                        .getOrDefault(
                            GithubRepositoryLanguageBreakdown(emptyMap())
                        ),
                    contributors = contributorsDeferred.await().getOrDefault(emptyList()),
                )
            },
            onFailure = { error ->
                GithubRepositoryDetailStatus.Error(
                    error.message ?: "Something went wrong"
                )
            },
        )
        _uiState.update { it.copy(status = status) }
    }

    fun onRetry() {
        val owner = currentOwner ?: return
        val repo = currentRepo ?: return
        load(
            owner = owner,
            repo = repo,
        )
    }
}

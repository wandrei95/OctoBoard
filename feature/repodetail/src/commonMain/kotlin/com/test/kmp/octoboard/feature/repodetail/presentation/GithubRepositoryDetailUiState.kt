package com.test.kmp.octoboard.feature.repodetail.presentation

import com.test.kmp.octoboard.core.common.entities.GithubRepository
import com.test.kmp.octoboard.feature.repodetail.entities.GithubRepositoryContributor
import com.test.kmp.octoboard.feature.repodetail.entities.GithubRepositoryLanguageBreakdown

sealed interface GithubRepositoryDetailStatus {
    data object Loading : GithubRepositoryDetailStatus
    data class Content(
        val details: GithubRepository,
        val languages: GithubRepositoryLanguageBreakdown,
        val contributors: List<GithubRepositoryContributor>,
    ) : GithubRepositoryDetailStatus
    data class Error(val message: String) : GithubRepositoryDetailStatus
}

data class GithubRepositoryDetailUiState(
    val status: GithubRepositoryDetailStatus = GithubRepositoryDetailStatus.Loading,
)

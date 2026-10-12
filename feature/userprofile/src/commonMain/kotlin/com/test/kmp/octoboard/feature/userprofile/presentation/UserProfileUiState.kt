package com.test.kmp.octoboard.feature.userprofile.presentation

import com.test.kmp.octoboard.feature.userprofile.entities.GithubUserDetails
import com.test.kmp.octoboard.shared.entities.GithubRepository

sealed interface UserProfileStatus {
    data object Loading : UserProfileStatus
    data class Content(
        val details: GithubUserDetails,
        val repos: List<GithubRepository>,
    ) : UserProfileStatus
    data class Error(val message: String) : UserProfileStatus
}

data class UserProfileUiState(
    val status: UserProfileStatus = UserProfileStatus.Loading,
    val isLoadingMore: Boolean = false,
)

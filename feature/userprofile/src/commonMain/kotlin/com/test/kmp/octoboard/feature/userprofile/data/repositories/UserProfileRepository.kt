package com.test.kmp.octoboard.feature.userprofile.data.repositories

import com.test.kmp.octoboard.core.common.entities.GithubRepository
import com.test.kmp.octoboard.core.common.entities.PagedResult
import com.test.kmp.octoboard.feature.userprofile.data.datasources.UserProfileRemoteDataSource
import com.test.kmp.octoboard.feature.userprofile.entities.GithubUserDetails

internal class UserProfileRepository(
    private val userProfileRemoteDataSource: UserProfileRemoteDataSource,
) {
    suspend fun getUserDetails(
        username: String,
    ): Result<GithubUserDetails> {
        return userProfileRemoteDataSource.fetchUserDetails(username = username)
    }

    suspend fun getUserRepositories(
        username: String,
        page: Int,
    ): Result<PagedResult<GithubRepository>> {
        return userProfileRemoteDataSource.fetchUserRepositories(
            username = username,
            page = page,
        )
    }
}

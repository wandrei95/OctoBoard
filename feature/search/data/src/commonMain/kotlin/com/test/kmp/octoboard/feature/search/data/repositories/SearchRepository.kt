package com.test.kmp.octoboard.feature.search.data.repositories

import com.test.kmp.octoboard.core.common.entities.PagedResult
import com.test.kmp.octoboard.feature.search.data.datasources.GithubRepositorySearchRemoteDataSource
import com.test.kmp.octoboard.feature.search.data.datasources.GithubUserSearchRemoteDataSource
import com.test.kmp.octoboard.feature.search.entities.GithubUserSummary
import com.test.kmp.octoboard.feature.search.entities.SearchSort
import com.test.kmp.octoboard.shared.entities.GithubRepository

class SearchRepository internal constructor(
    private val githubRepositorySearchRemoteDataSource: GithubRepositorySearchRemoteDataSource,
    private val githubUserSearchRemoteDataSource: GithubUserSearchRemoteDataSource,
) {
    suspend fun searchRepositories(
        query: String,
        sort: SearchSort,
        language: String?,
        page: Int,
    ): Result<PagedResult<GithubRepository>> {
        return githubRepositorySearchRemoteDataSource.search(
            query = query,
            sort = sort,
            language = language,
            page = page,
        )
    }

    suspend fun searchUsers(
        query: String,
        page: Int,
    ): Result<PagedResult<GithubUserSummary>> {
        return githubUserSearchRemoteDataSource.search(
            query = query,
            page = page,
        )
    }
}

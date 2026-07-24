package com.test.kmp.octoboard.feature.search.data.repositories

import com.test.kmp.octoboard.core.common.entities.PagedResult
import com.test.kmp.octoboard.feature.search.data.datasources.RepositoriesRemoteDataSource
import com.test.kmp.octoboard.feature.search.data.datasources.UsersRemoteDataSource
import com.test.kmp.octoboard.feature.search.entities.RepoSummary
import com.test.kmp.octoboard.feature.search.entities.SearchSort
import com.test.kmp.octoboard.feature.search.entities.UserSummary

internal class SearchRepository(
    private val repositoriesRemoteDataSource: RepositoriesRemoteDataSource,
    private val usersRemoteDataSource: UsersRemoteDataSource,
) {
    suspend fun searchRepositories(
        query: String,
        sort: SearchSort,
        language: String?,
        page: Int,
    ): Result<PagedResult<RepoSummary>> {
        return repositoriesRemoteDataSource.search(
            query = query,
            sort = sort,
            language = language,
            page = page,
        )
    }

    suspend fun searchUsers(query: String, page: Int): Result<PagedResult<UserSummary>> {
        return usersRemoteDataSource.search(
            query = query,
            page = page,
        )
    }
}

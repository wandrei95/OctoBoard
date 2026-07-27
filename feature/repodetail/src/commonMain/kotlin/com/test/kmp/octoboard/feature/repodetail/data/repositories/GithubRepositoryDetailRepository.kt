package com.test.kmp.octoboard.feature.repodetail.data.repositories

import com.test.kmp.octoboard.feature.repodetail.data.datasources.GithubRepositoryDetailRemoteDataSource
import com.test.kmp.octoboard.feature.repodetail.entities.GithubRepositoryContributor
import com.test.kmp.octoboard.feature.repodetail.entities.GithubRepositoryDetails
import com.test.kmp.octoboard.feature.repodetail.entities.GithubRepositoryLanguageBreakdown

internal class GithubRepositoryDetailRepository(
    private val githubRepositoryDetailRemoteDataSource: GithubRepositoryDetailRemoteDataSource,
) {
    suspend fun getRepositoryDetails(
        owner: String,
        repo: String,
    ): Result<GithubRepositoryDetails> {
        return githubRepositoryDetailRemoteDataSource.fetchRepositoryDetails(
            owner = owner,
            repo = repo,
        )
    }

    suspend fun getLanguages(
        owner: String,
        repo: String
    ): Result<GithubRepositoryLanguageBreakdown> {
        return githubRepositoryDetailRemoteDataSource.fetchLanguages(
            owner = owner,
            repo = repo,
        )
    }

    suspend fun getContributors(
        owner: String,
        repo: String
    ): Result<List<GithubRepositoryContributor>> {
        return githubRepositoryDetailRemoteDataSource.fetchContributors(
            owner = owner,
            repo = repo,
        )
    }
}

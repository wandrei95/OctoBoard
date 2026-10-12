package com.test.kmp.octoboard.feature.repodetail.data.repositories

import com.test.kmp.octoboard.feature.repodetail.data.datasources.GithubRepositoryDetailRemoteDataSource
import com.test.kmp.octoboard.feature.repodetail.entities.GithubRepositoryContributor
import com.test.kmp.octoboard.feature.repodetail.entities.GithubRepositoryDetail
import com.test.kmp.octoboard.feature.repodetail.entities.GithubRepositoryLanguageBreakdown
import com.test.kmp.octoboard.shared.favoritesdata.FavoritesStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

internal class GithubRepositoryDetailRepository(
    private val githubRepositoryDetailRemoteDataSource: GithubRepositoryDetailRemoteDataSource,
    private val favoritesStore: FavoritesStore,
) {
    fun getRepositoryDetails(
        owner: String,
        repo: String,
    ): Flow<Result<GithubRepositoryDetail>> = flow {
        val result = githubRepositoryDetailRemoteDataSource.fetchRepositoryDetails(
            owner = owner,
            repo = repo,
        )
        val detailsFlow = result.fold(
            onSuccess = { repository ->
                favoritesStore.observeIsFavorite(repository.id).map { isFavorite ->
                    Result.success(
                        GithubRepositoryDetail(
                            repository = repository,
                            isFavorite = isFavorite,
                        )
                    )
                }
            },
            onFailure = { error -> flowOf(Result.failure(error)) },
        )
        emitAll(detailsFlow)
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

    suspend fun setFavorite(
        id: Long,
        isFavorite: Boolean,
    ) {
        favoritesStore.setFavorite(
            id = id,
            isFavorite = isFavorite,
        )
    }
}

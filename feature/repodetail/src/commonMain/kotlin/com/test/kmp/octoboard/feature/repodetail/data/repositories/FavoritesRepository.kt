package com.test.kmp.octoboard.feature.repodetail.data.repositories

import com.test.kmp.octoboard.feature.repodetail.data.datasources.FavoritesLocalDataSource
import com.test.kmp.octoboard.feature.repodetail.data.datasources.GithubRepositoryDetailRemoteDataSource
import com.test.kmp.octoboard.feature.repodetail.entities.FavoriteRepositories
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest

internal class FavoritesRepository(
    private val githubRepositoryDetailRemoteDataSource: GithubRepositoryDetailRemoteDataSource,
    private val favoritesLocalDataSource: FavoritesLocalDataSource,
) {
    /**
     * Emits again whenever the set of favorite ids changes. Every emission fetches all repos
     * from the API again; nothing is cached.
     */
    fun observeFavorites(): Flow<FavoriteRepositories> {
        return favoritesLocalDataSource.observeFavoriteIds().mapLatest { ids ->
            val results = coroutineScope {
                ids
                    .map { id ->
                        async {
                            githubRepositoryDetailRemoteDataSource.fetchRepositoryById(id)
                        }
                    }
                    .awaitAll()
            }

            val repos = results.mapNotNull { it.getOrNull() }
            FavoriteRepositories(
                repos = repos,
                failedCount = results.size - repos.size,
            )
        }
    }

    suspend fun setFavorite(
        id: Long,
        isFavorite: Boolean,
    ) {
        favoritesLocalDataSource.setFavorite(
            id = id,
            isFavorite = isFavorite,
        )
    }
}

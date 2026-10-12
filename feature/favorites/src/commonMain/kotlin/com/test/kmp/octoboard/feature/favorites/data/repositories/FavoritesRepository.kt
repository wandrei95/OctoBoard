package com.test.kmp.octoboard.feature.favorites.data.repositories

import com.test.kmp.octoboard.feature.favorites.data.datasources.FavoritesRemoteDataSource
import com.test.kmp.octoboard.feature.favorites.entities.FavoriteRepositories
import com.test.kmp.octoboard.shared.favoritesdata.FavoritesStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest

internal class FavoritesRepository(
    private val favoritesRemoteDataSource: FavoritesRemoteDataSource,
    private val favoritesStore: FavoritesStore,
) {
    /**
     * Emits again whenever the set of favorite ids changes. Every emission fetches all repos
     * from the API again; nothing is cached.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeFavorites(): Flow<FavoriteRepositories> {
        return favoritesStore.observeFavoriteIds().mapLatest { ids ->
            val results = coroutineScope {
                ids
                    .map { id ->
                        async {
                            favoritesRemoteDataSource.fetchRepositoryById(id)
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
}

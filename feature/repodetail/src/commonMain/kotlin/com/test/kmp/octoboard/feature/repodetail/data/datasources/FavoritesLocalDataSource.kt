package com.test.kmp.octoboard.feature.repodetail.data.datasources

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.test.kmp.octoboard.feature.repodetail.data.local.FavoritesDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class FavoritesLocalDataSource(
    private val database: FavoritesDatabase,
) {
    fun observeFavoriteIds(): Flow<List<Long>> {
        return database.favoriteRepositoryIdQueries
            .selectAll()
            .asFlow()
            .mapToList(Dispatchers.Default)
    }

    fun observeIsFavorite(id: Long): Flow<Boolean> {
        return database.favoriteRepositoryIdQueries
            .selectById(id)
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
            .map { it != null }
    }

    suspend fun setFavorite(
        id: Long,
        isFavorite: Boolean,
    ) {
        if (isFavorite) {
            database.favoriteRepositoryIdQueries.insertOrReplace(id)
        } else {
            database.favoriteRepositoryIdQueries.deleteById(id)
        }
    }
}

package com.test.kmp.octoboard.feature.favorites.data.di

import com.test.kmp.octoboard.core.database.DatabaseDriverFactory
import com.test.kmp.octoboard.core.network.di.networkKoinModule
import com.test.kmp.octoboard.feature.favorites.data.datasources.FavoritesRemoteDataSource
import com.test.kmp.octoboard.feature.favorites.data.datasources.FavoritesStore
import com.test.kmp.octoboard.feature.favorites.data.local.FavoritesDatabase
import com.test.kmp.octoboard.feature.favorites.data.repositories.FavoritesRepository
import io.ktor.client.HttpClient
import org.koin.dsl.module

val favoritesDataKoinModule = module {
    includes(networkKoinModule)
    single<FavoritesDatabase> {
        FavoritesDatabase(
            driver = get<DatabaseDriverFactory>().createDriver(
                schema = FavoritesDatabase.Schema,
                dbName = "favorites.db",
            ),
        )
    }
    single<FavoritesStore> { FavoritesStore(database = get<FavoritesDatabase>()) }
    single<FavoritesRemoteDataSource> { FavoritesRemoteDataSource(httpClient = get<HttpClient>()) }
    single<FavoritesRepository> {
        FavoritesRepository(
            favoritesRemoteDataSource = get<FavoritesRemoteDataSource>(),
            favoritesStore = get<FavoritesStore>(),
        )
    }
}

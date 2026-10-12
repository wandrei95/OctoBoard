package com.test.kmp.octoboard.feature.favorites.di

import com.test.kmp.octoboard.core.network.di.networkKoinModule
import com.test.kmp.octoboard.feature.favorites.data.datasources.FavoritesRemoteDataSource
import com.test.kmp.octoboard.feature.favorites.data.repositories.FavoritesRepository
import com.test.kmp.octoboard.feature.favorites.presentation.FavoritesViewModel
import com.test.kmp.octoboard.shared.favoritesdata.FavoritesStore
import com.test.kmp.octoboard.shared.favoritesdata.di.favoritesDataKoinModule
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val favoritesKoinModule = module {
    includes(
        networkKoinModule,
        favoritesDataKoinModule,
    )
    single<FavoritesRemoteDataSource> { FavoritesRemoteDataSource(httpClient = get<HttpClient>()) }
    single<FavoritesRepository> {
        FavoritesRepository(
            favoritesRemoteDataSource = get<FavoritesRemoteDataSource>(),
            favoritesStore = get<FavoritesStore>(),
        )
    }
    viewModel<FavoritesViewModel> { FavoritesViewModel(repository = get<FavoritesRepository>()) }
}

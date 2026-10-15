package com.test.kmp.octoboard.feature.favorites.di

import com.test.kmp.octoboard.feature.favorites.data.di.favoritesDataKoinModule
import com.test.kmp.octoboard.feature.favorites.data.repositories.FavoritesRepository
import com.test.kmp.octoboard.feature.favorites.presentation.FavoritesViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val favoritesKoinModule = module {
    includes(favoritesDataKoinModule)
    viewModel<FavoritesViewModel> { FavoritesViewModel(repository = get<FavoritesRepository>()) }
}

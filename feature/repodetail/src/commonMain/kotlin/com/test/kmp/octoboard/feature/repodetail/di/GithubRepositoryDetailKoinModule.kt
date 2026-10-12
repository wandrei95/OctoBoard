package com.test.kmp.octoboard.feature.repodetail.di

import app.cash.sqldelight.db.SqlDriver
import com.test.kmp.octoboard.core.database.DatabaseDriverFactory
import com.test.kmp.octoboard.feature.repodetail.data.datasources.FavoritesLocalDataSource
import com.test.kmp.octoboard.feature.repodetail.data.datasources.GithubRepositoryDetailRemoteDataSource
import com.test.kmp.octoboard.feature.repodetail.data.local.FavoritesDatabase
import com.test.kmp.octoboard.feature.repodetail.data.repositories.FavoritesRepository
import com.test.kmp.octoboard.feature.repodetail.data.repositories.GithubRepositoryDetailRepository
import com.test.kmp.octoboard.feature.repodetail.presentation.FavoritesViewModel
import com.test.kmp.octoboard.feature.repodetail.presentation.GithubRepositoryDetailViewModel
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val githubRepositoryDetailKoinModule = module {
    single<GithubRepositoryDetailRemoteDataSource> {
        GithubRepositoryDetailRemoteDataSource(httpClient = get<HttpClient>())
    }
    single<SqlDriver> {
        get<DatabaseDriverFactory>().createDriver(
            schema = FavoritesDatabase.Schema,
            dbName = "favorites.db",
        )
    }
    single<FavoritesDatabase> {
        FavoritesDatabase(driver = get<SqlDriver>())
    }
    single<FavoritesLocalDataSource> {
        FavoritesLocalDataSource(database = get<FavoritesDatabase>())
    }
    single<GithubRepositoryDetailRepository> {
        GithubRepositoryDetailRepository(
            githubRepositoryDetailRemoteDataSource = get<GithubRepositoryDetailRemoteDataSource>(),
            favoritesLocalDataSource = get<FavoritesLocalDataSource>(),
        )
    }
    single<FavoritesRepository> {
        FavoritesRepository(
            githubRepositoryDetailRemoteDataSource = get<GithubRepositoryDetailRemoteDataSource>(),
            favoritesLocalDataSource = get<FavoritesLocalDataSource>(),
        )
    }
    viewModel<FavoritesViewModel> {
        FavoritesViewModel(repository = get<FavoritesRepository>())
    }
    viewModel<GithubRepositoryDetailViewModel> {
        GithubRepositoryDetailViewModel(
            repository = get<GithubRepositoryDetailRepository>(),
            favoritesRepository = get<FavoritesRepository>(),
        )
    }
}

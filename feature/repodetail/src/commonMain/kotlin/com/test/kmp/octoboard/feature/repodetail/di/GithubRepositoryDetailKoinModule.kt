package com.test.kmp.octoboard.feature.repodetail.di

import com.test.kmp.octoboard.core.network.di.networkKoinModule
import com.test.kmp.octoboard.feature.repodetail.data.datasources.GithubRepositoryDetailRemoteDataSource
import com.test.kmp.octoboard.feature.repodetail.data.repositories.GithubRepositoryDetailRepository
import com.test.kmp.octoboard.feature.repodetail.presentation.GithubRepositoryDetailViewModel
import com.test.kmp.octoboard.shared.favoritesdata.FavoritesStore
import com.test.kmp.octoboard.shared.favoritesdata.di.favoritesDataKoinModule
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val githubRepositoryDetailKoinModule = module {
    includes(
        networkKoinModule,
        favoritesDataKoinModule,
    )
    single<GithubRepositoryDetailRemoteDataSource> {
        GithubRepositoryDetailRemoteDataSource(httpClient = get<HttpClient>())
    }
    single<GithubRepositoryDetailRepository> {
        GithubRepositoryDetailRepository(
            githubRepositoryDetailRemoteDataSource = get<GithubRepositoryDetailRemoteDataSource>(),
            favoritesStore = get<FavoritesStore>(),
        )
    }
    viewModel<GithubRepositoryDetailViewModel> {
        GithubRepositoryDetailViewModel(
            repository = get<GithubRepositoryDetailRepository>(),
        )
    }
}

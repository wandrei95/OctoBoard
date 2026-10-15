package com.test.kmp.octoboard.feature.repodetail.data.di

import com.test.kmp.octoboard.core.network.di.networkKoinModule
import com.test.kmp.octoboard.feature.favorites.data.datasources.FavoritesStore
import com.test.kmp.octoboard.feature.favorites.data.di.favoritesDataKoinModule
import com.test.kmp.octoboard.feature.repodetail.data.datasources.GithubRepositoryDetailRemoteDataSource
import com.test.kmp.octoboard.feature.repodetail.data.repositories.GithubRepositoryDetailRepository
import io.ktor.client.HttpClient
import org.koin.dsl.module

val githubRepositoryDetailDataKoinModule = module {
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
}

package com.test.kmp.octoboard.feature.search.data.di

import com.test.kmp.octoboard.core.network.di.networkKoinModule
import com.test.kmp.octoboard.feature.search.data.datasources.GithubRepositorySearchRemoteDataSource
import com.test.kmp.octoboard.feature.search.data.datasources.GithubUserSearchRemoteDataSource
import com.test.kmp.octoboard.feature.search.data.repositories.SearchRepository
import io.ktor.client.HttpClient
import org.koin.dsl.module

val searchDataKoinModule = module {
    includes(networkKoinModule)
    single<GithubRepositorySearchRemoteDataSource> { GithubRepositorySearchRemoteDataSource(httpClient = get<HttpClient>()) }
    single<GithubUserSearchRemoteDataSource> { GithubUserSearchRemoteDataSource(httpClient = get<HttpClient>()) }
    single<SearchRepository> {
        SearchRepository(
            githubRepositorySearchRemoteDataSource = get<GithubRepositorySearchRemoteDataSource>(),
            githubUserSearchRemoteDataSource = get<GithubUserSearchRemoteDataSource>(),
        )
    }
}

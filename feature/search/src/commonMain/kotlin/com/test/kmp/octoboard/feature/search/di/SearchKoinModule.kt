package com.test.kmp.octoboard.feature.search.di

import com.test.kmp.octoboard.feature.search.data.datasources.GithubRepositorySearchRemoteDataSource
import com.test.kmp.octoboard.feature.search.data.datasources.GithubUserSearchRemoteDataSource
import com.test.kmp.octoboard.feature.search.data.repositories.SearchRepository
import com.test.kmp.octoboard.feature.search.presentation.SearchViewModel
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val searchKoinModule = module {
    single<GithubRepositorySearchRemoteDataSource> { GithubRepositorySearchRemoteDataSource(httpClient = get<HttpClient>()) }
    single<GithubUserSearchRemoteDataSource> { GithubUserSearchRemoteDataSource(httpClient = get<HttpClient>()) }
    single<SearchRepository> {
        SearchRepository(
            githubRepositorySearchRemoteDataSource = get<GithubRepositorySearchRemoteDataSource>(),
            githubUserSearchRemoteDataSource = get<GithubUserSearchRemoteDataSource>(),
        )
    }
    viewModel<SearchViewModel> { SearchViewModel(repository = get<SearchRepository>()) }
}

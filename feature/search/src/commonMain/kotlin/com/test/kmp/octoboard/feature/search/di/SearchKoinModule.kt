package com.test.kmp.octoboard.feature.search.di

import com.test.kmp.octoboard.feature.search.data.datasources.RepositoriesRemoteDataSource
import com.test.kmp.octoboard.feature.search.data.datasources.UsersRemoteDataSource
import com.test.kmp.octoboard.feature.search.data.repositories.SearchRepository
import com.test.kmp.octoboard.feature.search.presentation.SearchViewModel
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val searchKoinModule = module {
    single<RepositoriesRemoteDataSource> { RepositoriesRemoteDataSource(httpClient = get<HttpClient>()) }
    single<UsersRemoteDataSource> { UsersRemoteDataSource(httpClient = get<HttpClient>()) }
    single<SearchRepository> {
        SearchRepository(
            repositoriesRemoteDataSource = get<RepositoriesRemoteDataSource>(),
            usersRemoteDataSource = get<UsersRemoteDataSource>(),
        )
    }
    viewModel<SearchViewModel> { SearchViewModel(repository = get<SearchRepository>()) }
}

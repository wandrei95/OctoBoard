package com.test.kmp.octoboard.feature.userprofile.di

import com.test.kmp.octoboard.feature.userprofile.data.datasources.UserProfileRemoteDataSource
import com.test.kmp.octoboard.feature.userprofile.data.repositories.UserProfileRepository
import com.test.kmp.octoboard.feature.userprofile.presentation.UserProfileViewModel
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val userProfileKoinModule = module {
    single<UserProfileRemoteDataSource> {
        UserProfileRemoteDataSource(httpClient = get<HttpClient>())
    }
    single<UserProfileRepository> {
        UserProfileRepository(userProfileRemoteDataSource = get<UserProfileRemoteDataSource>())
    }
    viewModel<UserProfileViewModel> {
        UserProfileViewModel(repository = get<UserProfileRepository>())
    }
}

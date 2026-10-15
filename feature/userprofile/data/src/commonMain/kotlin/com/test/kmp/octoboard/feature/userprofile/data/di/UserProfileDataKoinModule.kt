package com.test.kmp.octoboard.feature.userprofile.data.di

import com.test.kmp.octoboard.core.network.di.networkKoinModule
import com.test.kmp.octoboard.feature.userprofile.data.datasources.UserProfileRemoteDataSource
import com.test.kmp.octoboard.feature.userprofile.data.repositories.UserProfileRepository
import io.ktor.client.HttpClient
import org.koin.dsl.module

val userProfileDataKoinModule = module {
    includes(networkKoinModule)
    single<UserProfileRemoteDataSource> {
        UserProfileRemoteDataSource(httpClient = get<HttpClient>())
    }
    single<UserProfileRepository> {
        UserProfileRepository(userProfileRemoteDataSource = get<UserProfileRemoteDataSource>())
    }
}

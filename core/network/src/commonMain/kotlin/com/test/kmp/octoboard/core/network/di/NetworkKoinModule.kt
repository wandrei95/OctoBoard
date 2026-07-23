package com.test.kmp.octoboard.core.network.di

import com.test.kmp.octoboard.core.network.AuthTokenProvider
import com.test.kmp.octoboard.core.network.GithubApiClient
import com.test.kmp.octoboard.core.network.RateLimitInfo
import com.test.kmp.octoboard.core.network.createGitHubHttpClient
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.dsl.module

val networkKoinModule = module {
    single { MutableStateFlow(RateLimitInfo.UNKNOWN) }
    single {
        createGitHubHttpClient(
            tokenProvider = get<AuthTokenProvider>(),
            onRateLimitUpdate = { get<MutableStateFlow<RateLimitInfo>>().value = it }
        )
    }
    single { GithubApiClient(get()) }
}

package com.test.kmp.octoboard.core.network.di

import com.test.kmp.octoboard.core.network.AuthTokenProvider
import com.test.kmp.octoboard.core.network.createGitHubHttpClient
import io.ktor.client.HttpClient
import org.koin.dsl.module

val networkKoinModule = module {
    single<HttpClient> { createGitHubHttpClient(tokenProvider = get<AuthTokenProvider>()) }
}

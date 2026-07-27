package com.test.kmp.octoboard.integration.di

import com.test.kmp.octoboard.core.network.AuthTokenProvider
import com.test.kmp.octoboard.core.network.di.networkKoinModule
import com.test.kmp.octoboard.feature.repodetail.di.githubRepositoryDetailKoinModule
import com.test.kmp.octoboard.feature.search.di.searchKoinModule
import org.koin.core.module.Module
import org.koin.dsl.module

val commonAppModules: List<Module> = listOf(
    networkKoinModule,
    searchKoinModule,
    githubRepositoryDetailKoinModule,
    module {
        // Placeholder until :feature:auth's SecureStorage is bound here in phase 5.
        single<AuthTokenProvider> { object : AuthTokenProvider {
            override fun getToken(): String? = null
        } }
    }
)

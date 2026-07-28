package com.test.kmp.octoboard.integration.di

import com.test.kmp.octoboard.core.network.AuthTokenProvider
import com.test.kmp.octoboard.core.network.di.networkKoinModule
import com.test.kmp.octoboard.feature.auth.data.SecureStorage
import com.test.kmp.octoboard.feature.auth.di.authKoinModule
import com.test.kmp.octoboard.feature.repodetail.di.githubRepositoryDetailKoinModule
import com.test.kmp.octoboard.feature.search.di.searchKoinModule
import com.test.kmp.octoboard.feature.userprofile.di.userProfileKoinModule
import org.koin.core.module.Module
import org.koin.dsl.module

val commonAppModules: List<Module> = listOf(
    networkKoinModule,
    searchKoinModule,
    githubRepositoryDetailKoinModule,
    userProfileKoinModule,
    authKoinModule,
    platformKoinModule,
    module {
        single<AuthTokenProvider> { get<SecureStorage>() }
    }
)

package com.test.kmp.octoboard.integration.di

import com.test.kmp.octoboard.core.network.AuthTokenProvider
import com.test.kmp.octoboard.feature.auth.data.SecureStorage
import com.test.kmp.octoboard.feature.auth.di.authKoinModule
import com.test.kmp.octoboard.feature.favorites.di.favoritesKoinModule
import com.test.kmp.octoboard.feature.repodetail.di.githubRepositoryDetailKoinModule
import com.test.kmp.octoboard.feature.search.di.searchKoinModule
import com.test.kmp.octoboard.feature.userprofile.di.userProfileKoinModule
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * The feature modules declare the shared modules they need (network, favorites data) with
 * `includes(...)`, so they are not listed here. What the app itself must still provide:
 * [platformKoinModule] (`SecureStorage`, `DatabaseDriverFactory`, and on Android the `DataStore`)
 * and the `AuthTokenProvider` binding at the end of the list.
 */
val commonAppModules: List<Module> = listOf(
    searchKoinModule,
    githubRepositoryDetailKoinModule,
    favoritesKoinModule,
    userProfileKoinModule,
    authKoinModule,
    platformKoinModule,
    module {
        single<AuthTokenProvider> { get<SecureStorage>() }
    }
)

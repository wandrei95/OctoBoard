package com.test.kmp.octoboard.integration.di

import com.test.kmp.octoboard.core.network.AuthTokenProvider
import com.test.kmp.octoboard.core.network.di.networkKoinModule
import com.test.kmp.octoboard.integration.data.SmokeRepository
import com.test.kmp.octoboard.integration.presentation.SmokeViewModel
import org.koin.core.module.Module
import org.koin.dsl.module

val commonAppModules: List<Module> = listOf(
    networkKoinModule,
    module {
        // Placeholder until :feature:auth's SecureStorage is bound here in phase 5.
        single<AuthTokenProvider> { object : AuthTokenProvider {
            override fun getToken(): String? = null
        } }
        single { SmokeRepository(get()) }
        factory { SmokeViewModel(get()) }
    }
)

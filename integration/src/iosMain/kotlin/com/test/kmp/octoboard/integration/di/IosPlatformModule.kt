package com.test.kmp.octoboard.integration.di

import com.test.kmp.octoboard.feature.auth.data.SecureStorage
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformKoinModule: Module = module {
    single<SecureStorage> { SecureStorage() }
}

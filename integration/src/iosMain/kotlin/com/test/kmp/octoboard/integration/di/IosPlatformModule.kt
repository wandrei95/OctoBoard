package com.test.kmp.octoboard.integration.di

import com.test.kmp.octoboard.core.database.DatabaseDriverFactory
import com.test.kmp.octoboard.feature.auth.data.SecureStorage
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformKoinModule: Module = module {
    single<SecureStorage> { SecureStorage() }
    single<DatabaseDriverFactory> { DatabaseDriverFactory() }
}

package com.test.kmp.octoboard.integration.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.test.kmp.octoboard.feature.auth.data.SecureStorage
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformKoinModule: Module = module {
    single<DataStore<Preferences>> {
        val context = androidContext()
        PreferenceDataStoreFactory.create {
            context.preferencesDataStoreFile("octoboard_secure_prefs")
        }
    }
    single<SecureStorage> { SecureStorage(get()) }
}

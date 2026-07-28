package com.test.kmp.octoboard.feature.auth.di

import com.test.kmp.octoboard.feature.auth.data.SecureStorage
import com.test.kmp.octoboard.feature.auth.presentation.AuthViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val authKoinModule = module {
    viewModel<AuthViewModel> { AuthViewModel(secureStorage = get<SecureStorage>()) }
}

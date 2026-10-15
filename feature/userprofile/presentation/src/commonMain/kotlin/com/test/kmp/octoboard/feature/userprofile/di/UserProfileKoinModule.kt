package com.test.kmp.octoboard.feature.userprofile.di

import com.test.kmp.octoboard.feature.userprofile.data.di.userProfileDataKoinModule
import com.test.kmp.octoboard.feature.userprofile.data.repositories.UserProfileRepository
import com.test.kmp.octoboard.feature.userprofile.presentation.UserProfileViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val userProfileKoinModule = module {
    includes(userProfileDataKoinModule)
    viewModel<UserProfileViewModel> {
        UserProfileViewModel(repository = get<UserProfileRepository>())
    }
}

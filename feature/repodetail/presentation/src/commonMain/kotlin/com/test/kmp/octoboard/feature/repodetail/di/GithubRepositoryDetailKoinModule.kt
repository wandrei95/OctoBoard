package com.test.kmp.octoboard.feature.repodetail.di

import com.test.kmp.octoboard.feature.repodetail.data.di.githubRepositoryDetailDataKoinModule
import com.test.kmp.octoboard.feature.repodetail.data.repositories.GithubRepositoryDetailRepository
import com.test.kmp.octoboard.feature.repodetail.presentation.GithubRepositoryDetailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val githubRepositoryDetailKoinModule = module {
    includes(githubRepositoryDetailDataKoinModule)
    viewModel<GithubRepositoryDetailViewModel> {
        GithubRepositoryDetailViewModel(
            repository = get<GithubRepositoryDetailRepository>(),
        )
    }
}

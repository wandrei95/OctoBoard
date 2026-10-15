package com.test.kmp.octoboard.feature.search.di

import com.test.kmp.octoboard.feature.search.data.di.searchDataKoinModule
import com.test.kmp.octoboard.feature.search.data.repositories.SearchRepository
import com.test.kmp.octoboard.feature.search.presentation.SearchViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val searchKoinModule = module {
    includes(searchDataKoinModule)
    viewModel<SearchViewModel> { SearchViewModel(repository = get<SearchRepository>()) }
}

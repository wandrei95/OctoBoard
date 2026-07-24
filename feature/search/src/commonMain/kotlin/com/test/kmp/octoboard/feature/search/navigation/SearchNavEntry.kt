package com.test.kmp.octoboard.feature.search.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.test.kmp.octoboard.core.uinav.OctoBoardNavigator
import com.test.kmp.octoboard.feature.search.presentation.SearchViewModel
import com.test.kmp.octoboard.feature.search.ui.SearchScreen
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data object SearchNavEntryKey : SearchKey

fun EntryProviderScope<NavKey>.searchNavEntry(navigator: OctoBoardNavigator) {
    entry<SearchNavEntryKey> {
        val viewModel = koinViewModel<SearchViewModel>()
        SearchScreen(
            viewModel = viewModel,
            // RepoDetailKey/UserProfileKey don't exist yet - real navigation arrives in
            // phase 3 (repo detail) and phase 4 (user profile).
            onRepoSelected = { _, _ -> },
            onUserSelected = { _ -> },
        )
    }
}

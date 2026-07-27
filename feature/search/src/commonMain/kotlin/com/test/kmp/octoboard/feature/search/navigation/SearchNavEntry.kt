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

fun EntryProviderScope<NavKey>.searchNavEntry(
    navigator: OctoBoardNavigator,
    metadata: Map<String, Any>,
    onRepoSelected: (navigator: OctoBoardNavigator, owner: String, repo: String) -> Unit,
    onUserSelected: (navigator: OctoBoardNavigator, username: String) -> Unit,
) {
    entry<SearchNavEntryKey>(metadata = metadata) {
        val viewModel = koinViewModel<SearchViewModel>()
        SearchScreen(
            viewModel = viewModel,
            onRepoSelected = { owner, repo ->
                onRepoSelected(
                    navigator,
                    owner,
                    repo,
                )
            },
            onUserSelected = { username ->
                onUserSelected(
                    navigator,
                    username,
                )
            },
        )
    }
}

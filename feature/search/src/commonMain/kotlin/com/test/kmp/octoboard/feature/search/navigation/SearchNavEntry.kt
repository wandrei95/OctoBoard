package com.test.kmp.octoboard.feature.search.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    onModeChanged: (navigator: OctoBoardNavigator) -> Unit,
) {
    entry<SearchNavEntryKey>(metadata = metadata) {
        val viewModel = koinViewModel<SearchViewModel>()
        val uiState by viewModel.uiState.collectAsState()

        var previousMode by remember { mutableStateOf(uiState.mode) }
        LaunchedEffect(uiState.mode) {
            if (uiState.mode != previousMode) {
                previousMode = uiState.mode
                onModeChanged(navigator)
            }
        }

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

package com.test.kmp.octoboard.feature.favorites.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.test.kmp.octoboard.core.uinav.OctoBoardNavigator
import com.test.kmp.octoboard.feature.favorites.presentation.FavoritesViewModel
import com.test.kmp.octoboard.feature.favorites.ui.FavoritesScreen
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data object FavoritesNavEntryKey : NavKey

fun EntryProviderScope<NavKey>.favoritesNavEntry(
    navigator: OctoBoardNavigator,
    metadata: Map<String, Any>,
    onRepoSelected: (navigator: OctoBoardNavigator, owner: String, repo: String) -> Unit,
) {
    entry<FavoritesNavEntryKey>(metadata = metadata) {
        val viewModel = koinViewModel<FavoritesViewModel>()
        FavoritesScreen(
            viewModel = viewModel,
            onRepoSelected = { owner, repo ->
                onRepoSelected(
                    navigator,
                    owner,
                    repo,
                )
            },
        )
    }
}

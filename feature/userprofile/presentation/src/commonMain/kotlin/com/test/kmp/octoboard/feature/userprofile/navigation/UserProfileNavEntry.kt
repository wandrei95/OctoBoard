package com.test.kmp.octoboard.feature.userprofile.navigation

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.test.kmp.octoboard.core.uinav.OctoBoardNavigator
import com.test.kmp.octoboard.feature.userprofile.presentation.UserProfileViewModel
import com.test.kmp.octoboard.feature.userprofile.ui.UserProfileScreen
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data class UserProfileNavEntryKey(
    val username: String,
) : UserProfileKey

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
internal fun EntryProviderScope<NavKey>.userProfileNavEntry(
    navigator: OctoBoardNavigator,
    sceneKey: Any,
    onRepoSelected: (navigator: OctoBoardNavigator, owner: String, repo: String) -> Unit,
) {
    entry<UserProfileNavEntryKey>(
        metadata = ListDetailSceneStrategy.detailPane(sceneKey = sceneKey),
    ) { key ->
        val viewModel = koinViewModel<UserProfileViewModel>()
        LaunchedEffect(key1 = key.username) {
            viewModel.load(key.username)
        }
        UserProfileScreen(
            viewModel = viewModel,
            onRepoSelected = { owner, repo ->
                onRepoSelected(navigator, owner, repo)
            },
        )
    }
}

package com.test.kmp.octoboard.feature.userprofile.navigation

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

fun EntryProviderScope<NavKey>.userProfileNavEntry(
    navigator: OctoBoardNavigator,
    onRepoSelected: (navigator: OctoBoardNavigator, owner: String, repo: String) -> Unit,
) {
    entry<UserProfileNavEntryKey> { key ->
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

package com.test.kmp.octoboard.feature.repodetail.navigation

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.test.kmp.octoboard.core.uinav.OctoBoardNavigator
import com.test.kmp.octoboard.feature.repodetail.presentation.GithubRepositoryDetailViewModel
import com.test.kmp.octoboard.feature.repodetail.ui.GithubRepositoryDetailScreen
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data class GithubRepositoryDetailNavEntryKey(
    val owner: String,
    val repo: String,
) : GithubRepositoryDetailKey

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
internal fun EntryProviderScope<NavKey>.githubRepositoryDetailNavEntry(
    navigator: OctoBoardNavigator,
    sceneKey: Any,
    onContributorSelected: (navigator: OctoBoardNavigator, username: String) -> Unit,
) {
    entry<GithubRepositoryDetailNavEntryKey>(
        metadata = ListDetailSceneStrategy.detailPane(sceneKey = sceneKey),
    ) { key ->
        val viewModel = koinViewModel<GithubRepositoryDetailViewModel>()
        LaunchedEffect(key1 = "${key.owner}/${key.repo}") {
            viewModel.load(
                owner = key.owner,
                repo = key.repo,
            )
        }
        GithubRepositoryDetailScreen(
            viewModel = viewModel,
            onContributorSelected = { username ->
                onContributorSelected(
                    navigator,
                    username,
                )
            },
        )
    }
}

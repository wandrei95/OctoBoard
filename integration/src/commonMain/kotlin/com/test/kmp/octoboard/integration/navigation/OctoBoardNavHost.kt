package com.test.kmp.octoboard.integration.navigation

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.Snapshot
import androidx.navigation3.runtime.NavKey
import com.test.kmp.octoboard.core.designsystem.EmptyState
import com.test.kmp.octoboard.core.uinav.FeatureNavDisplay
import com.test.kmp.octoboard.core.uinav.OctoBoardNavigator
import com.test.kmp.octoboard.feature.repodetail.navigation.GithubRepositoryDetailNavEntries
import com.test.kmp.octoboard.feature.repodetail.navigation.GithubRepositoryDetailNavEntryKey
import com.test.kmp.octoboard.feature.search.navigation.SearchNavEntries
import com.test.kmp.octoboard.feature.search.navigation.SearchNavEntryKey
import com.test.kmp.octoboard.feature.userprofile.navigation.UserProfileNavEntries
import com.test.kmp.octoboard.feature.userprofile.navigation.UserProfileNavEntryKey
import com.test.kmp.octoboard.integration.generated.resources.Res
import com.test.kmp.octoboard.integration.generated.resources.search_detail_placeholder_message
import org.jetbrains.compose.resources.stringResource

/**
 * Distinguishes the search-list/repo-detail list-detail scaffold from other
 * [androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy] groups that may later
 * share the same NavDisplay. Owned here (not by either feature) since it pairs two features'
 * panes into one scene.
 */
private data object GithubRepositoryListDetailSceneKey

/**
 * [GithubRepositoryDetailNavEntryKey] and [UserProfileNavEntryKey] share one interchangeable
 * "detail" slot on the back stack: navigating to either one replaces the current entry if it's
 * also a member of this slot, rather than stacking a new entry per selection. This grouping is an
 * integration-level wiring decision (which features happen to share a slot in this app's nav
 * graph), so it's kept here rather than in either feature module.
 */
private fun OctoBoardNavigator.navigateToDetail(key: NavKey) {
    Snapshot.withMutableSnapshot {
        val top = peek()
        if (top is GithubRepositoryDetailNavEntryKey || top is UserProfileNavEntryKey) {
            back()
        }
        navigate(key)
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun OctoBoardNavHost() {
    val listDetailSceneStrategy = rememberListDetailSceneStrategy<NavKey>()

    // Remembered so the list stays referentially stable across recompositions of this composable -
    // otherwise FeatureNavDisplay's `remember(featureNavEntries)` would see a "changed" key on every
    // recomposition (SearchNavEntries/RepoDetailNavEntries aren't data classes) and rebuild the nav
    // back stack's serializers module/configuration every time.
    val featureNavEntries = remember {
        listOf(
            SearchNavEntries(
                metadata = ListDetailSceneStrategy.listPane(
                    sceneKey = GithubRepositoryListDetailSceneKey,
                    detailPlaceholder = {
                        EmptyState(stringResource(Res.string.search_detail_placeholder_message))
                    },
                ),
                onRepoSelected = { navigator, owner, repo ->
                    navigator.navigateToDetail(
                        GithubRepositoryDetailNavEntryKey(
                            owner = owner,
                            repo = repo,
                        ),
                    )
                },
                onUserSelected = { navigator, username ->
                    navigator.navigateToDetail(UserProfileNavEntryKey(username))
                },
            ),
            GithubRepositoryDetailNavEntries(
                sceneKey = GithubRepositoryListDetailSceneKey,
                onContributorSelected = { navigator, username ->
                    navigator.navigateToDetail(UserProfileNavEntryKey(username))
                },
            ),
            UserProfileNavEntries(
                onRepoSelected = { navigator, owner, repo ->
                    navigator.navigateToDetail(
                        GithubRepositoryDetailNavEntryKey(
                            owner = owner,
                            repo = repo,
                        ),
                    )
                },
            ),
        )
    }

    FeatureNavDisplay(
        startDestination = SearchNavEntryKey,
        featureNavEntries = featureNavEntries,
        sceneStrategies = listOf(listDetailSceneStrategy),
    )
}

package com.test.kmp.octoboard.integration.navigation

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.Snapshot
import androidx.navigation3.runtime.NavKey
import com.test.kmp.octoboard.core.designsystem.EmptyState
import com.test.kmp.octoboard.core.uinav.FeatureNavEntry
import com.test.kmp.octoboard.core.uinav.OctoBoardNavigator
import com.test.kmp.octoboard.feature.favorites.navigation.FavoritesNavEntries
import com.test.kmp.octoboard.feature.repodetail.navigation.GithubRepositoryDetailNavEntries
import com.test.kmp.octoboard.feature.repodetail.navigation.GithubRepositoryDetailNavEntryKey
import com.test.kmp.octoboard.feature.search.navigation.SearchNavEntries
import com.test.kmp.octoboard.feature.userprofile.navigation.UserProfileNavEntries
import com.test.kmp.octoboard.feature.userprofile.navigation.UserProfileNavEntryKey
import com.test.kmp.octoboard.integration.generated.resources.Res
import com.test.kmp.octoboard.integration.generated.resources.integration_detail_placeholder_message
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun rememberOctoBoardFeatureNavEntries(): List<FeatureNavEntry> {
    // Remembered so the list stays referentially stable across recompositions of this composable -
    // otherwise FeatureNavDisplay's `remember(featureNavEntries)` would see a "changed" key on every
    // recomposition (SearchNavEntries/RepoDetailNavEntries aren't data classes) and rebuild the nav
    // back stack's serializers module/configuration every time.
    val featureNavEntries = remember {
        listOf(
            SearchNavEntries(
                metadata = ListDetailSceneStrategy.listPane(
                    sceneKey = MainListDetailSceneKey,
                    detailPlaceholder = {
                        EmptyState(stringResource(Res.string.integration_detail_placeholder_message))
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
                onModeChanged = { navigator -> navigator.resetDetailPane() },
            ),
            FavoritesNavEntries(
                metadata = ListDetailSceneStrategy.listPane(
                    sceneKey = MainListDetailSceneKey,
                    detailPlaceholder = {
                        EmptyState(stringResource(Res.string.integration_detail_placeholder_message))
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
            ),
            GithubRepositoryDetailNavEntries(
                sceneKey = MainListDetailSceneKey,
                onContributorSelected = { navigator, username ->
                    navigator.navigateToDetail(UserProfileNavEntryKey(username))
                },
            ),
            UserProfileNavEntries(
                sceneKey = MainListDetailSceneKey,
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
    return featureNavEntries
}

/**
 * Distinguishes the app's main list-detail scaffold from other
 * [androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy] groups that may later
 * share the same NavDisplay. Owned here (not by any one feature) since it pairs several features'
 * panes (search and favorites lists, repo and user detail) into one scene.
 */
private data object MainListDetailSceneKey

/**
 * [GithubRepositoryDetailNavEntryKey] and [UserProfileNavEntryKey] share one interchangeable
 * "detail" slot on the back stack: navigating to either one replaces the current entry if it's
 * also a member of this slot, rather than stacking a new entry per selection. This grouping is an
 * integration-level wiring decision (which features happen to share a slot in this app's nav
 * graph), so it's kept here rather than in either feature module.
 */
internal fun NavKey.isDetailSlotKey(): Boolean =
    this is GithubRepositoryDetailNavEntryKey || this is UserProfileNavEntryKey

private fun OctoBoardNavigator.navigateToDetail(key: NavKey) {
    Snapshot.withMutableSnapshot {
        if (peek()?.isDetailSlotKey() == true) {
            back()
        }
        navigate(key)
    }
}

/**
 * Switching search modes (repositories/users) makes whatever is currently occupying the detail
 * slot stale — e.g. a repo detail pane left over from before switching to the users tab. Only
 * pops if the slot is actually occupied; a no-op on compact layouts where nothing was selected.
 */
private fun OctoBoardNavigator.resetDetailPane() {
    Snapshot.withMutableSnapshot {
        if (peek()?.isDetailSlotKey() == true) {
            back()
        }
    }
}

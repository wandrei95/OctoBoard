package com.test.kmp.octoboard.feature.repodetail.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.test.kmp.octoboard.core.uinav.FeatureNavEntry
import com.test.kmp.octoboard.core.uinav.OctoBoardNavigator
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

class GithubRepositoryDetailNavEntries(
    private val sceneKey: Any,
    private val onContributorSelected: (navigator: OctoBoardNavigator, username: String) -> Unit,
) : FeatureNavEntry {

    override val serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(GithubRepositoryDetailNavEntryKey::class)
        }
    }

    override fun EntryProviderScope<NavKey>.registerEntries(navigator: OctoBoardNavigator) {
        githubRepositoryDetailNavEntry(
            navigator = navigator,
            sceneKey = sceneKey,
            onContributorSelected = onContributorSelected,
        )
    }
}

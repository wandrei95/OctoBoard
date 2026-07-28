package com.test.kmp.octoboard.feature.search.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.test.kmp.octoboard.core.uinav.FeatureNavEntry
import com.test.kmp.octoboard.core.uinav.OctoBoardNavigator
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

class SearchNavEntries(
    private val metadata: Map<String, Any>,
    private val onRepoSelected: (navigator: OctoBoardNavigator, owner: String, repo: String) -> Unit,
    private val onUserSelected: (navigator: OctoBoardNavigator, username: String) -> Unit,
    private val onModeChanged: (navigator: OctoBoardNavigator) -> Unit,
) : FeatureNavEntry {

    override val serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(SearchNavEntryKey::class)
        }
    }

    override fun EntryProviderScope<NavKey>.registerEntries(navigator: OctoBoardNavigator) {
        searchNavEntry(
            navigator = navigator,
            metadata = metadata,
            onRepoSelected = onRepoSelected,
            onUserSelected = onUserSelected,
            onModeChanged = onModeChanged,
        )
    }
}

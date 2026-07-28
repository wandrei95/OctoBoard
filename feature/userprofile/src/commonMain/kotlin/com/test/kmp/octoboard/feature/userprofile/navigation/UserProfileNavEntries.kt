package com.test.kmp.octoboard.feature.userprofile.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.test.kmp.octoboard.core.uinav.FeatureNavEntry
import com.test.kmp.octoboard.core.uinav.OctoBoardNavigator
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

class UserProfileNavEntries(
    private val sceneKey: Any,
    private val onRepoSelected: (navigator: OctoBoardNavigator, owner: String, repo: String) -> Unit,
) : FeatureNavEntry {

    override val serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(UserProfileNavEntryKey::class)
        }
    }

    override fun EntryProviderScope<NavKey>.registerEntries(navigator: OctoBoardNavigator) {
        userProfileNavEntry(
            navigator = navigator,
            sceneKey = sceneKey,
            onRepoSelected = onRepoSelected,
        )
    }
}

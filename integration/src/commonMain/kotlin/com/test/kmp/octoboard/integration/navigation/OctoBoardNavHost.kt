package com.test.kmp.octoboard.integration.navigation

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import com.test.kmp.octoboard.core.uinav.FeatureNavDisplay
import com.test.kmp.octoboard.core.uinav.FeatureNavEntry
import com.test.kmp.octoboard.core.uinav.OctoBoardNavController

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun OctoBoardNavHost(
    navigator: OctoBoardNavController,
    featureNavEntries: List<FeatureNavEntry>,
) {
    // PopLatest: search and favorites are both list-pane entries in the same scene, so the default
    // PopUntilScaffoldValueChange sees no value change when going back from favorites to search,
    // finds no previous destination, and disables back (the app closes instead).
    val listDetailSceneStrategy = rememberListDetailSceneStrategy<NavKey>(
        backNavigationBehavior = BackNavigationBehavior.PopLatest,
    )

    FeatureNavDisplay(
        navigator = navigator,
        featureNavEntries = featureNavEntries,
        sceneStrategies = listOf(listDetailSceneStrategy),
    )
}

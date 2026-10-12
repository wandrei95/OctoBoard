package com.test.kmp.octoboard.core.uinav

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.ui.NavDisplay

@Composable
fun FeatureNavDisplay(
    navigator: OctoBoardNavController,
    featureNavEntries: List<FeatureNavEntry>,
    sceneStrategies: List<SceneStrategy<NavKey>>,
) {
    NavDisplay(
        backStack = navigator.backStack,
        onBack = { navigator.back() },
        sceneStrategies = sceneStrategies,
        entryProvider = entryProvider {
            featureNavEntries.forEach { featureNavEntry ->
                with(featureNavEntry) { registerEntries(navigator) }
            }
        },
    )
}

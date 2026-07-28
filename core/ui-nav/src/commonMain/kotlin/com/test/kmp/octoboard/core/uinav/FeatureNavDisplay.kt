package com.test.kmp.octoboard.core.uinav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.Snapshot
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.SerializersModule

@Composable
fun FeatureNavDisplay(
    startDestination: NavKey,
    featureNavEntries: List<FeatureNavEntry>,
    sceneStrategies: List<SceneStrategy<NavKey>>,
) {
    val combinedSerializersModule = remember(featureNavEntries) {
        SerializersModule {
            featureNavEntries.forEach { include(it.serializersModule) }
        }
    }
    val configuration = remember(combinedSerializersModule) {
        SavedStateConfiguration { serializersModule = combinedSerializersModule }
    }
    val backStack = rememberNavBackStack(
        configuration = configuration,
        elements = arrayOf(startDestination),
    )
    val navigator = remember {
        object : OctoBoardNavigator {
            override fun navigate(key: NavKey) {
                backStack.add(key)
            }

            override fun back() {
                backStack.removeLastOrNull()
            }

            override fun navigate(
                key: NavKey,
                popUpTo: NavKey,
                inclusive: Boolean,
            ) {
                Snapshot.withMutableSnapshot {
                    val popUpToIndex = backStack.indexOfLast { it == popUpTo }
                    if (popUpToIndex >= 0) {
                        val keepUntilIndex = if (inclusive) {
                            popUpToIndex
                        } else {
                            popUpToIndex + 1
                        }
                        backStack.subList(
                            fromIndex = keepUntilIndex,
                            toIndex = backStack.size,
                        ).clear()
                    }
                    backStack.add(key)
                }
            }

            override fun peek(): NavKey? {
                return backStack.lastOrNull()
            }
        }
    }

    LaunchedEffect(backStack) {
        snapshotFlow { backStack.toList() }.collect { currentBackStack ->
            println("AAAAAAAAAA FeatureNavDisplay backstack: $currentBackStack")
        }
    }

    NavDisplay(
        backStack = backStack,
        onBack = { navigator.back() },
        sceneStrategies = sceneStrategies,
        entryProvider = entryProvider {
            featureNavEntries.forEach { featureNavEntry ->
                with(featureNavEntry) { registerEntries(navigator) }
            }
        },
    )
}

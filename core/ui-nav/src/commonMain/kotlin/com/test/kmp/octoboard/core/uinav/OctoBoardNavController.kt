package com.test.kmp.octoboard.core.uinav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.Snapshot
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.SerializersModule

/**
 * The [OctoBoardNavigator] implementation backed by a Navigation 3 back stack. Created by
 * [rememberOctoBoardNavigator] and handed to [FeatureNavDisplay], which renders the back stack;
 * everything else only sees the [OctoBoardNavigator] interface.
 */
class OctoBoardNavController internal constructor(
    internal val backStack: NavBackStack<NavKey>,
) : OctoBoardNavigator {

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

/**
 * Creates the navigator (and its back stack) outside of [FeatureNavDisplay] so it can be driven
 * from anywhere. [featureNavEntries] supplies the serializers needed to save/restore the back
 * stack, so it must be the same (referentially stable) list later passed to [FeatureNavDisplay].
 */
@Composable
fun rememberOctoBoardNavigator(
    startDestination: NavKey,
    featureNavEntries: List<FeatureNavEntry>,
): OctoBoardNavController {
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
    return remember(backStack) { OctoBoardNavController(backStack) }
}

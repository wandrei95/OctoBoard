package com.test.kmp.octoboard.core.uinav

import androidx.annotation.MainThread
import androidx.navigation3.runtime.NavKey

/**
 * Must only be called from the main thread — like Jetpack Navigation's `NavController`,
 * it is not designed for concurrent use from multiple threads.
 */
interface OctoBoardNavigator {
    @MainThread
    fun navigate(key: NavKey)

    @MainThread
    fun back()

    /**
     * Pops the back stack down to [popUpTo] (or past it, if [inclusive]) and pushes [key],
     * as a single atomic back stack mutation. If [popUpTo] isn't present, this behaves like
     * a plain [navigate].
     */
    @MainThread
    fun navigate(
        key: NavKey,
        popUpTo: NavKey,
        inclusive: Boolean = false,
    )

    /**
     * Pushes [key], first removing the current top of the back stack if it is the same
     * runtime type as [key], as a single atomic back stack mutation. Otherwise behaves like
     * a plain [navigate].
     *
     * Use this when [key] is a new selection for a "slot" that a same-typed entry already at
     * the top of the back stack represents (e.g. choosing a different item to show in a
     * list-detail scene's detail pane) rather than a genuinely new step in navigation
     * history — so repeatedly selecting different items doesn't pile up one back stack
     * entry per selection.
     */
    @MainThread
    fun navigateReplacingTop(key: NavKey)
}

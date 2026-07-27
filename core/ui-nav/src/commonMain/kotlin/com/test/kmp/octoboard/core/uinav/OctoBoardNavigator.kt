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
}

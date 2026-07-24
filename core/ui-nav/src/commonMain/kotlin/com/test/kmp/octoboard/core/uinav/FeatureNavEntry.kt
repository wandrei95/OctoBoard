package com.test.kmp.octoboard.core.uinav

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.modules.SerializersModule

interface FeatureNavEntry {
    val serializersModule: SerializersModule
    fun EntryProviderScope<NavKey>.registerEntries(navigator: OctoBoardNavigator)
}

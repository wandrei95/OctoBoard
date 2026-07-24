package com.test.kmp.octoboard.integration.navigation

import androidx.compose.runtime.Composable
import com.test.kmp.octoboard.core.uinav.FeatureNavDisplay
import com.test.kmp.octoboard.feature.search.navigation.SearchNavEntries
import com.test.kmp.octoboard.feature.search.navigation.SearchNavEntryKey

@Composable
fun OctoBoardNavHost() {
    FeatureNavDisplay(
        startDestination = SearchNavEntryKey,
        featureNavEntries = listOf(SearchNavEntries),
    )
}

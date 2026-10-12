package com.test.kmp.octoboard.integration.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.test.kmp.octoboard.core.designsystem.OctoBoardTheme
import com.test.kmp.octoboard.core.uinav.OctoBoardNavigator
import com.test.kmp.octoboard.core.uinav.rememberOctoBoardNavigator
import com.test.kmp.octoboard.feature.auth.presentation.AuthViewModel
import com.test.kmp.octoboard.feature.auth.ui.AuthBottomSheetContent
import com.test.kmp.octoboard.feature.repodetail.navigation.FavoritesNavEntryKey
import com.test.kmp.octoboard.feature.search.navigation.SearchNavEntryKey
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntegrationRoot() {
    OctoBoardTheme {
        val authViewModel = koinViewModel<AuthViewModel>()
        var isAuthSheetVisible by remember { mutableStateOf(false) }

        val featureNavEntries = rememberOctoBoardFeatureNavEntries()
        val navigator = rememberOctoBoardNavigator(
            startDestination = SearchNavEntryKey,
            featureNavEntries = featureNavEntries,
        )

        Box(Modifier.fillMaxSize()) {
            OctoBoardNavHost(
                navigator = navigator,
                featureNavEntries = featureNavEntries,
            )

            IntegrationFabMenu(
                onFavoritesClick = { navigator.openFavorites() },
                onPatClick = { isAuthSheetVisible = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(
                        end = OctoBoardTheme.dimensions.spacingLarge,
                        bottom = OctoBoardTheme.dimensions.spacingLarge,
                    ),
            )
        }

        if (isAuthSheetVisible) {
            ModalBottomSheet(onDismissRequest = { isAuthSheetVisible = false }) {
                AuthBottomSheetContent(
                    viewModel = authViewModel,
                    onSaved = { isAuthSheetVisible = false },
                )
            }
        }
    }
}

/**
 * Opens the favorites list on top of the current screen. No-op if it's already showing; any repo
 * or user detail currently in the detail slot is dropped first so the list isn't paired with a
 * stale pane.
 */
private fun OctoBoardNavigator.openFavorites() {
    Snapshot.withMutableSnapshot {
        if (peek()?.isDetailSlotKey() == true) {
            back()
        }
        if (peek() != FavoritesNavEntryKey) {
            navigate(FavoritesNavEntryKey)
        }
    }
}

package com.test.kmp.octoboard.integration.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.test.kmp.octoboard.core.designsystem.OctoBoardTheme
import com.test.kmp.octoboard.feature.auth.presentation.AuthViewModel
import com.test.kmp.octoboard.feature.auth.ui.AuthBottomSheetContent
import com.test.kmp.octoboard.integration.generated.resources.Res
import com.test.kmp.octoboard.integration.generated.resources.integration_pat_fab_label
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntegrationRoot() {
    OctoBoardTheme {
        val authViewModel = koinViewModel<AuthViewModel>()
        var isAuthSheetVisible by remember { mutableStateOf(false) }

        Box(Modifier.fillMaxSize()) {
            OctoBoardNavHost()

            FloatingActionButton(
                onClick = { isAuthSheetVisible = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(OctoBoardTheme.dimensions.spacingLarge),
            ) {
                Text(stringResource(Res.string.integration_pat_fab_label))
            }
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

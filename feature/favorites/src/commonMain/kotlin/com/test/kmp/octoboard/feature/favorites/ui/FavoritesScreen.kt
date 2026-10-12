package com.test.kmp.octoboard.feature.favorites.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.test.kmp.octoboard.core.designsystem.EmptyState
import com.test.kmp.octoboard.core.designsystem.ErrorState
import com.test.kmp.octoboard.core.designsystem.GithubRepositoryList
import com.test.kmp.octoboard.core.designsystem.LoadingIndicator
import com.test.kmp.octoboard.core.designsystem.OctoBoardTheme
import com.test.kmp.octoboard.feature.favorites.generated.resources.Res
import com.test.kmp.octoboard.feature.favorites.generated.resources.favorites_empty_message
import com.test.kmp.octoboard.feature.favorites.generated.resources.favorites_error_message
import com.test.kmp.octoboard.feature.favorites.generated.resources.favorites_partial_failure_message
import com.test.kmp.octoboard.feature.favorites.presentation.FavoritesStatus
import com.test.kmp.octoboard.feature.favorites.presentation.FavoritesViewModel
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FavoritesScreen(
    viewModel: FavoritesViewModel,
    onRepoSelected: (owner: String, repo: String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(OctoBoardTheme.dimensions.spacingLarge),
        ) {
            when (val status = uiState.status) {
                is FavoritesStatus.Loading -> LoadingIndicator()
                is FavoritesStatus.Empty -> EmptyState(stringResource(Res.string.favorites_empty_message))
                is FavoritesStatus.Error -> ErrorState(
                    message = stringResource(Res.string.favorites_error_message),
                    onRetry = viewModel::onRetry,
                )
                is FavoritesStatus.Content -> {
                    if (status.failedCount > 0) {
                        Text(
                            text = stringResource(
                                Res.string.favorites_partial_failure_message,
                                status.failedCount,
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = OctoBoardTheme.dimensions.spacingMedium),
                        )
                    }
                    GithubRepositoryList(
                        repos = status.repos,
                        onRepoSelected = onRepoSelected,
                    )
                }
            }
        }
    }
}

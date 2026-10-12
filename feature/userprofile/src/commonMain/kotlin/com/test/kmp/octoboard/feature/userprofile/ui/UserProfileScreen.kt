package com.test.kmp.octoboard.feature.userprofile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.test.kmp.octoboard.core.designsystem.Avatar
import com.test.kmp.octoboard.core.designsystem.ErrorState
import com.test.kmp.octoboard.core.designsystem.InfoChip
import com.test.kmp.octoboard.core.designsystem.LoadingIndicator
import com.test.kmp.octoboard.core.designsystem.OctoBoardTheme
import com.test.kmp.octoboard.feature.userprofile.entities.GithubUserDetails
import com.test.kmp.octoboard.feature.userprofile.generated.resources.Res
import com.test.kmp.octoboard.feature.userprofile.generated.resources.userprofile_followers_chip
import com.test.kmp.octoboard.feature.userprofile.generated.resources.userprofile_following_chip
import com.test.kmp.octoboard.feature.userprofile.generated.resources.userprofile_public_repos_chip
import com.test.kmp.octoboard.feature.userprofile.generated.resources.userprofile_repos_header
import com.test.kmp.octoboard.feature.userprofile.presentation.UserProfileStatus
import com.test.kmp.octoboard.feature.userprofile.presentation.UserProfileUiState
import com.test.kmp.octoboard.feature.userprofile.presentation.UserProfileViewModel
import com.test.kmp.octoboard.shared.entities.GithubRepository
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun UserProfileScreen(
    viewModel: UserProfileViewModel,
    onRepoSelected: (owner: String, repo: String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold { innerPadding ->
        UserProfileScreenContent(
            uiState = uiState,
            onRetry = viewModel::onRetry,
            onLoadMoreRepos = viewModel::loadMoreRepos,
            onRepoSelected = onRepoSelected,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
private fun UserProfileScreenContent(
    uiState: UserProfileUiState,
    onRetry: () -> Unit,
    onLoadMoreRepos: () -> Unit,
    onRepoSelected: (owner: String, repo: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (val status = uiState.status) {
        is UserProfileStatus.Loading -> LoadingIndicator(modifier = modifier)
        is UserProfileStatus.Error -> ErrorState(
            message = status.message,
            onRetry = onRetry,
            modifier = modifier,
        )

        is UserProfileStatus.Content -> UserProfileContent(
            details = status.details,
            repos = status.repos,
            isLoadingMore = uiState.isLoadingMore,
            onLoadMoreRepos = onLoadMoreRepos,
            onRepoSelected = { repo ->
                onRepoSelected(
                    repo.ownerLogin,
                    repo.name,
                )
            },
            modifier = modifier,
        )
    }
}

@Composable
private fun UserProfileContent(
    details: GithubUserDetails,
    repos: List<GithubRepository>,
    isLoadingMore: Boolean,
    onLoadMoreRepos: () -> Unit,
    onRepoSelected: (GithubRepository) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            lastVisible >= repos.size - 3
        }
    }
    LaunchedEffect(
        key1 = shouldLoadMore,
        key2 = repos.size,
    ) {
        if (shouldLoadMore) {
            onLoadMoreRepos()
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(OctoBoardTheme.dimensions.spacingLarge),
        verticalArrangement = Arrangement.spacedBy(OctoBoardTheme.dimensions.spacingLarge),
    ) {
        item { UserProfileHeader(details) }

        item {
            Text(
                text = stringResource(Res.string.userprofile_repos_header),
                style = MaterialTheme.typography.titleMedium,
            )
        }

        items(
            items = repos,
            key = { it.id },
        ) { repo ->
            GithubUserRepositoryRow(
                repo = repo,
                onClick = { onRepoSelected(repo) },
            )
        }

        if (isLoadingMore) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(OctoBoardTheme.dimensions.spacingMedium),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(OctoBoardTheme.dimensions.iconSizeSmall))
                }
            }
        }
    }
}

@Composable
private fun UserProfileHeader(details: GithubUserDetails) {
    Column {
        Row(verticalAlignment = Alignment.Top) {
            Avatar(
                imageUrl = details.avatarUrl,
                fallbackLabel = details.login,
            )
            Spacer(Modifier.size(OctoBoardTheme.dimensions.spacingMedium))
            Column {
                Text(
                    text = details.name ?: details.login,
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = details.login,
                    style = MaterialTheme.typography.bodyMedium,
                )
                details.bio?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

        Spacer(Modifier.size(OctoBoardTheme.dimensions.spacingSmall))
        Column {
            details.company?.let { Text("🏢 $it") }
            details.location?.let { Text("📍 $it") }
            details.blogUrl?.takeIf { it.isNotBlank() }?.let { Text("🔗 $it") }
        }

        Spacer(Modifier.size(OctoBoardTheme.dimensions.spacingMedium))
        Row(
            horizontalArrangement = Arrangement.spacedBy(OctoBoardTheme.dimensions.spacingSmall)
        ) {
            InfoChip(stringResource(Res.string.userprofile_public_repos_chip, details.publicRepos))
            InfoChip(stringResource(Res.string.userprofile_followers_chip, details.followers))
            InfoChip(stringResource(Res.string.userprofile_following_chip, details.following))
        }
    }
}

@Composable
private fun GithubUserRepositoryRow(
    repo: GithubRepository,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Column(modifier = Modifier.padding(OctoBoardTheme.dimensions.spacingMedium)) {
            Text(
                text = repo.name,
                style = MaterialTheme.typography.titleMedium,
            )
            repo.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                )
            }
            Spacer(Modifier.size(OctoBoardTheme.dimensions.spacingSmall))
            Row(horizontalArrangement = Arrangement.spacedBy(OctoBoardTheme.dimensions.spacingSmall)) {
                InfoChip("★ ${repo.stars}")
                repo.language?.let { InfoChip(it) }
            }
        }
    }
}

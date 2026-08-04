package com.test.kmp.octoboard.feature.repodetail.ui

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.test.kmp.octoboard.core.designsystem.Avatar
import com.test.kmp.octoboard.core.designsystem.ErrorState
import com.test.kmp.octoboard.core.designsystem.InfoChip
import com.test.kmp.octoboard.core.designsystem.LoadingIndicator
import com.test.kmp.octoboard.core.designsystem.OctoBoardTheme
import com.test.kmp.octoboard.feature.repodetail.entities.GithubRepositoryContributor
import com.test.kmp.octoboard.feature.repodetail.entities.GithubRepositoryDetail
import com.test.kmp.octoboard.feature.repodetail.entities.GithubRepositoryLanguageBreakdown
import com.test.kmp.octoboard.feature.repodetail.generated.resources.Res
import com.test.kmp.octoboard.feature.repodetail.generated.resources.repodetail_add_favorite_content_description
import com.test.kmp.octoboard.feature.repodetail.generated.resources.repodetail_contributions_chip
import com.test.kmp.octoboard.feature.repodetail.generated.resources.repodetail_contributors_header
import com.test.kmp.octoboard.feature.repodetail.generated.resources.repodetail_forks_chip
import com.test.kmp.octoboard.feature.repodetail.generated.resources.repodetail_languages_header
import com.test.kmp.octoboard.feature.repodetail.generated.resources.repodetail_open_issues_chip
import com.test.kmp.octoboard.feature.repodetail.generated.resources.repodetail_remove_favorite_content_description
import com.test.kmp.octoboard.feature.repodetail.presentation.GithubRepositoryDetailStatus
import com.test.kmp.octoboard.feature.repodetail.presentation.GithubRepositoryDetailUiState
import com.test.kmp.octoboard.feature.repodetail.presentation.GithubRepositoryDetailViewModel
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun GithubRepositoryDetailScreen(
    viewModel: GithubRepositoryDetailViewModel,
    onContributorSelected: (username: String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold { innerPadding ->
        GithubRepositoryDetailScreenContent(
            uiState = uiState,
            onRetry = viewModel::onRetry,
            onContributorSelected = onContributorSelected,
            onToggleFavorite = viewModel::onToggleFavorite,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
private fun GithubRepositoryDetailScreenContent(
    uiState: GithubRepositoryDetailUiState,
    onRetry: () -> Unit,
    onContributorSelected: (username: String) -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (val status = uiState.status) {
        is GithubRepositoryDetailStatus.Loading -> LoadingIndicator(modifier = modifier)
        is GithubRepositoryDetailStatus.Error -> ErrorState(
            message = status.message,
            onRetry = onRetry,
            modifier = modifier,
        )

        is GithubRepositoryDetailStatus.Content -> GithubRepositoryDetailContent(
            details = status.details,
            languages = status.languages,
            contributors = status.contributors,
            onContributorSelected = onContributorSelected,
            onToggleFavorite = onToggleFavorite,
            modifier = modifier,
        )
    }
}

@Composable
private fun GithubRepositoryDetailContent(
    details: GithubRepositoryDetail,
    languages: GithubRepositoryLanguageBreakdown,
    contributors: List<GithubRepositoryContributor>,
    onContributorSelected: (username: String) -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(OctoBoardTheme.dimensions.spacingLarge),
        verticalArrangement = Arrangement.spacedBy(OctoBoardTheme.dimensions.spacingLarge),
    ) {
        item {
            GithubRepositoryDetailHeader(
                details = details,
                onToggleFavorite = onToggleFavorite,
            )
        }

        if (languages.bytesByLanguage.isNotEmpty()) {
            item { LanguagesSection(languages) }
        }

        if (contributors.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(Res.string.repodetail_contributors_header),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            items(
                items = contributors,
                key = { it.id },
            ) { contributor ->
                GithubRepositoryContributorRow(
                    contributor = contributor,
                    onClick = { onContributorSelected(contributor.login) },
                )
            }
        }
    }
}

@Composable
private fun GithubRepositoryDetailHeader(
    details: GithubRepositoryDetail,
    onToggleFavorite: () -> Unit,
) {
    val repository = details.repository
    Column {
        Row(verticalAlignment = Alignment.Top) {
            Avatar(
                imageUrl = repository.ownerAvatarUrl,
                fallbackLabel = repository.ownerLogin,
            )
            Spacer(Modifier.size(OctoBoardTheme.dimensions.spacingMedium))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = repository.fullName,
                    style = MaterialTheme.typography.titleLarge,
                )
                repository.description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (details.isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = stringResource(
                        if (details.isFavorite) {
                            Res.string.repodetail_remove_favorite_content_description
                        } else {
                            Res.string.repodetail_add_favorite_content_description
                        }
                    ),
                    modifier = Modifier.size(OctoBoardTheme.dimensions.iconSizeSmall),
                )
            }
        }
        Spacer(Modifier.size(OctoBoardTheme.dimensions.spacingMedium))
        Row(
            horizontalArrangement = Arrangement.spacedBy(OctoBoardTheme.dimensions.spacingSmall)
        ) {
            InfoChip("★ ${repository.stars}")
            InfoChip(stringResource(Res.string.repodetail_forks_chip, repository.forks))
            InfoChip(stringResource(Res.string.repodetail_open_issues_chip, repository.openIssues))
        }
    }
}

@Composable
private fun LanguagesSection(languages: GithubRepositoryLanguageBreakdown) {
    Column {
        Text(
            text = stringResource(Res.string.repodetail_languages_header),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.size(OctoBoardTheme.dimensions.spacingSmall))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(OctoBoardTheme.dimensions.spacingSmall)) {
            items(languages.bytesByLanguage.entries.sortedByDescending { it.value }) { (language, _) ->
                InfoChip(language)
            }
        }
    }
}

@Composable
private fun GithubRepositoryContributorRow(
    contributor: GithubRepositoryContributor,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(OctoBoardTheme.dimensions.spacingMedium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Avatar(
                imageUrl = contributor.avatarUrl,
                fallbackLabel = contributor.login,
            )
            Spacer(Modifier.size(OctoBoardTheme.dimensions.spacingMedium))
            Text(
                text = contributor.login,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            InfoChip(
                stringResource(
                    Res.string.repodetail_contributions_chip,
                    contributor.contributions
                )
            )
        }
    }
}

package com.test.kmp.octoboard.core.designsystem

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.test.kmp.octoboard.core.common.entities.GithubRepository

private const val LOAD_MORE_THRESHOLD = 3

/**
 * Scrollable list of repositories. Paging is opt-in: pass [onLoadNextPage] to be told when the
 * user nears the end of the list.
 */
@Composable
fun GithubRepositoryList(
    repos: List<GithubRepository>,
    onRepoSelected: (owner: String, repo: String) -> Unit,
    modifier: Modifier = Modifier,
    isLoadingMore: Boolean = false,
    onLoadNextPage: (() -> Unit)? = null,
) {
    val listState = rememberLazyListState()
    val itemCount = repos.size

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            lastVisible >= itemCount - LOAD_MORE_THRESHOLD
        }
    }
    LaunchedEffect(
        key1 = shouldLoadMore,
        key2 = itemCount,
    ) {
        if (shouldLoadMore) {
            onLoadNextPage?.invoke()
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = OctoBoardTheme.dimensions.spacingLarge),
        verticalArrangement = Arrangement.spacedBy(OctoBoardTheme.dimensions.spacingSmall),
    ) {
        items(
            items = repos,
            key = { it.id },
        ) { repo ->
            GithubRepositoryListItem(
                repo = repo,
                onClick = {
                    onRepoSelected(
                        repo.ownerLogin,
                        repo.name,
                    )
                },
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
fun GithubRepositoryListItem(
    repo: GithubRepository,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(OctoBoardTheme.dimensions.spacingMedium),
            verticalAlignment = Alignment.Top
        ) {
            Avatar(
                imageUrl = repo.ownerAvatarUrl,
                fallbackLabel = repo.ownerLogin,
            )
            Spacer(Modifier.size(OctoBoardTheme.dimensions.spacingMedium))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = repo.fullName,
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
}

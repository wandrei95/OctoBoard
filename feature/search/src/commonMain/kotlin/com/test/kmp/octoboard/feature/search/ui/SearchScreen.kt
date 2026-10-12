package com.test.kmp.octoboard.feature.search.ui

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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.test.kmp.octoboard.core.designsystem.Avatar
import com.test.kmp.octoboard.core.designsystem.EmptyState
import com.test.kmp.octoboard.core.designsystem.ErrorState
import com.test.kmp.octoboard.core.designsystem.GithubRepositoryList
import com.test.kmp.octoboard.core.designsystem.LoadingIndicator
import com.test.kmp.octoboard.core.designsystem.OctoBoardTheme
import com.test.kmp.octoboard.feature.search.entities.GithubUserSummary
import com.test.kmp.octoboard.feature.search.entities.SearchMode
import com.test.kmp.octoboard.feature.search.entities.SearchSort
import com.test.kmp.octoboard.feature.search.generated.resources.Res
import com.test.kmp.octoboard.feature.search.generated.resources.search_empty_state_message
import com.test.kmp.octoboard.feature.search.generated.resources.search_language_label
import com.test.kmp.octoboard.feature.search.generated.resources.search_mode_repositories
import com.test.kmp.octoboard.feature.search.generated.resources.search_mode_users
import com.test.kmp.octoboard.feature.search.generated.resources.search_no_results_message
import com.test.kmp.octoboard.feature.search.generated.resources.search_query_label
import com.test.kmp.octoboard.feature.search.generated.resources.search_sort_best_match
import com.test.kmp.octoboard.feature.search.generated.resources.search_sort_by_label
import com.test.kmp.octoboard.feature.search.generated.resources.search_sort_forks
import com.test.kmp.octoboard.feature.search.generated.resources.search_sort_stars
import com.test.kmp.octoboard.feature.search.generated.resources.search_sort_updated
import com.test.kmp.octoboard.feature.search.presentation.SearchResults
import com.test.kmp.octoboard.feature.search.presentation.SearchStatus
import com.test.kmp.octoboard.feature.search.presentation.SearchUiState
import com.test.kmp.octoboard.feature.search.presentation.SearchViewModel
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SearchScreen(
    viewModel: SearchViewModel,
    onRepoSelected: (owner: String, repo: String) -> Unit,
    onUserSelected: (username: String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold { innerPadding ->
        SearchScreenContent(
            uiState = uiState,
            onQueryChange = viewModel::onQueryChange,
            onModeChange = viewModel::onModeChange,
            onSortChange = viewModel::onSortChange,
            onLanguageFilterChange = viewModel::onLanguageFilterChange,
            onLoadNextPage = viewModel::onLoadNextPage,
            onRetry = viewModel::onRetry,
            onRepoSelected = onRepoSelected,
            onUserSelected = onUserSelected,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchScreenContent(
    uiState: SearchUiState,
    onQueryChange: (String) -> Unit,
    onModeChange: (SearchMode) -> Unit,
    onSortChange: (SearchSort) -> Unit,
    onLanguageFilterChange: (String) -> Unit,
    onLoadNextPage: () -> Unit,
    onRetry: () -> Unit,
    onRepoSelected: (owner: String, repo: String) -> Unit,
    onUserSelected: (username: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize().padding(OctoBoardTheme.dimensions.spacingLarge)) {
        OutlinedTextField(
            value = uiState.query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(Res.string.search_query_label)) },
            singleLine = true,
        )

        Spacer(Modifier.size(OctoBoardTheme.dimensions.spacingMedium))

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = uiState.mode == SearchMode.Repositories,
                onClick = { onModeChange(SearchMode.Repositories) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = 0,
                    count = 2,
                ),
            ) { Text(stringResource(Res.string.search_mode_repositories)) }
            SegmentedButton(
                selected = uiState.mode == SearchMode.Users,
                onClick = { onModeChange(SearchMode.Users) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = 1,
                    count = 2,
                ),
            ) { Text(stringResource(Res.string.search_mode_users)) }
        }

        if (uiState.mode == SearchMode.Repositories) {
            Spacer(Modifier.size(OctoBoardTheme.dimensions.spacingMedium))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(OctoBoardTheme.dimensions.spacingSmall)
            ) {
                SortDropdown(
                    selected = uiState.sort,
                    onSortChange = onSortChange,
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = uiState.language,
                    onValueChange = onLanguageFilterChange,
                    modifier = Modifier.weight(1f),
                    label = { Text(stringResource(Res.string.search_language_label)) },
                    singleLine = true,
                )
            }
        }

        Spacer(Modifier.size(OctoBoardTheme.dimensions.spacingMedium))

        Column(modifier = Modifier.fillMaxSize()) {
            when (val status = uiState.status) {
                is SearchStatus.Idle -> EmptyState(stringResource(Res.string.search_empty_state_message))
                is SearchStatus.Loading -> LoadingIndicator()
                is SearchStatus.Empty -> EmptyState(stringResource(Res.string.search_no_results_message))
                is SearchStatus.Error -> ErrorState(
                    message = status.message,
                    onRetry = onRetry,
                )
                is SearchStatus.Content -> SearchResultsList(
                    uiState = uiState,
                    onLoadNextPage = onLoadNextPage,
                    onRepoSelected = onRepoSelected,
                    onUserSelected = onUserSelected,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortDropdown(
    selected: SearchSort,
    onSortChange: (SearchSort) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected.label(),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(Res.string.search_sort_by_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth()
                .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            SearchSort.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label()) },
                    onClick = {
                        onSortChange(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun SearchSort.label(): String = when (this) {
    SearchSort.BestMatch -> stringResource(Res.string.search_sort_best_match)
    SearchSort.Stars -> stringResource(Res.string.search_sort_stars)
    SearchSort.Forks -> stringResource(Res.string.search_sort_forks)
    SearchSort.Updated -> stringResource(Res.string.search_sort_updated)
}

@Composable
private fun SearchResultsList(
    uiState: SearchUiState,
    onLoadNextPage: () -> Unit,
    onRepoSelected: (owner: String, repo: String) -> Unit,
    onUserSelected: (username: String) -> Unit,
) {
    when (val results = uiState.results) {
        is SearchResults.GithubRepositorySearchResults -> GithubRepositoryList(
            repos = results.items,
            onRepoSelected = onRepoSelected,
            isLoadingMore = uiState.isLoadingMore,
            onLoadNextPage = onLoadNextPage,
        )
        is SearchResults.GithubUserSearchResults -> GithubUserList(
            users = results.items,
            isLoadingMore = uiState.isLoadingMore,
            onLoadNextPage = onLoadNextPage,
            onUserSelected = onUserSelected,
        )
    }
}

@Composable
private fun GithubUserList(
    users: List<GithubUserSummary>,
    isLoadingMore: Boolean,
    onLoadNextPage: () -> Unit,
    onUserSelected: (username: String) -> Unit,
) {
    val listState = rememberLazyListState()
    val itemCount = users.size

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            lastVisible >= itemCount - 3
        }
    }
    LaunchedEffect(
        key1 = shouldLoadMore,
        key2 = itemCount,
    ) {
        if (shouldLoadMore) {
            onLoadNextPage()
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = OctoBoardTheme.dimensions.spacingLarge),
        verticalArrangement = Arrangement.spacedBy(OctoBoardTheme.dimensions.spacingSmall),
    ) {
        items(
            items = users,
            key = { it.id },
        ) { user ->
            GithubUserListItem(
                user = user,
                onClick = { onUserSelected(user.login) },
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
private fun GithubUserListItem(
    user: GithubUserSummary,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(OctoBoardTheme.dimensions.spacingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar(
                imageUrl = user.avatarUrl,
                fallbackLabel = user.login,
            )
            Spacer(Modifier.size(OctoBoardTheme.dimensions.spacingMedium))
            Text(
                text = user.login,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

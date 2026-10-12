package com.test.kmp.octoboard.integration.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.test.kmp.octoboard.integration.generated.resources.Res
import com.test.kmp.octoboard.integration.generated.resources.integration_favorites_fab_label
import com.test.kmp.octoboard.integration.generated.resources.integration_fab_menu_close_content_description
import com.test.kmp.octoboard.integration.generated.resources.integration_fab_menu_open_content_description
import com.test.kmp.octoboard.integration.generated.resources.integration_pat_fab_label
import org.jetbrains.compose.resources.stringResource

/**
 * App-level actions collapsed behind a single FAB so they don't cover list content: opening the
 * favorites list and the personal access token sheet.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun IntegrationFabMenu(
    onFavoritesClick: () -> Unit,
    onPatClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    FloatingActionButtonMenu(
        expanded = expanded,
        modifier = modifier,
        button = {
            ToggleFloatingActionButton(
                checked = expanded,
                onCheckedChange = { expanded = it },
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Filled.Close else Icons.Filled.Menu,
                    contentDescription = if (expanded) {
                        stringResource(Res.string.integration_fab_menu_close_content_description)
                    } else {
                        stringResource(Res.string.integration_fab_menu_open_content_description)
                    },
                )
            }
        },
    ) {
        FloatingActionButtonMenuItem(
            onClick = {
                expanded = false
                onFavoritesClick()
            },
            text = { Text(stringResource(Res.string.integration_favorites_fab_label)) },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                )
            },
        )
        FloatingActionButtonMenuItem(
            onClick = {
                expanded = false
                onPatClick()
            },
            text = { Text(stringResource(Res.string.integration_pat_fab_label)) },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                )
            },
        )
    }
}

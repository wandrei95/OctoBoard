package com.test.kmp.octoboard.feature.auth.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.test.kmp.octoboard.core.designsystem.OctoBoardTheme
import com.test.kmp.octoboard.feature.auth.generated.resources.Res
import com.test.kmp.octoboard.feature.auth.generated.resources.auth_clear_button
import com.test.kmp.octoboard.feature.auth.generated.resources.auth_helper_message
import com.test.kmp.octoboard.feature.auth.generated.resources.auth_save_button
import com.test.kmp.octoboard.feature.auth.generated.resources.auth_sheet_title
import com.test.kmp.octoboard.feature.auth.generated.resources.auth_status_not_saved_message
import com.test.kmp.octoboard.feature.auth.generated.resources.auth_status_saved_message
import com.test.kmp.octoboard.feature.auth.generated.resources.auth_token_field_label
import com.test.kmp.octoboard.feature.auth.presentation.AuthViewModel
import org.jetbrains.compose.resources.stringResource

@Composable
fun AuthBottomSheetContent(
    viewModel: AuthViewModel,
    onSaved: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(OctoBoardTheme.dimensions.spacingLarge),
    ) {
        Text(stringResource(Res.string.auth_sheet_title))

        Spacer(Modifier.size(OctoBoardTheme.dimensions.spacingSmall))

        Text(stringResource(Res.string.auth_helper_message))

        Spacer(Modifier.size(OctoBoardTheme.dimensions.spacingMedium))

        Text(
            if (uiState.hasStoredToken) {
                stringResource(Res.string.auth_status_saved_message)
            } else {
                stringResource(Res.string.auth_status_not_saved_message)
            },
        )

        Spacer(Modifier.size(OctoBoardTheme.dimensions.spacingMedium))

        OutlinedTextField(
            value = uiState.tokenInput,
            onValueChange = viewModel::onTokenInputChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(Res.string.auth_token_field_label)) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
        )

        Spacer(Modifier.size(OctoBoardTheme.dimensions.spacingLarge))

        Button(
            onClick = {
                viewModel.onSave()
                onSaved()
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(Res.string.auth_save_button))
        }

        if (uiState.hasStoredToken) {
            TextButton(
                onClick = viewModel::onClear,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(Res.string.auth_clear_button))
            }
        }
    }
}

package com.smartphoneaichat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.smartphoneaichat.R
import com.smartphoneaichat.ui.theme.AccentBlue
import com.smartphoneaichat.ui.theme.DarkBackground
import com.smartphoneaichat.ui.theme.TextPrimary
import com.smartphoneaichat.ui.theme.TextSecondary

/** Local vault provisioning form backed by the MG-03 access boundary. */
@Composable
fun CredentialsSetupScreen(
    username: String,
    password: String,
    canComplete: Boolean,
    errorMessage: String? = null,
    onUsernameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 24.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(64.dp))
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(AccentBlue.copy(alpha = 0.14f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.Key,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = AccentBlue,
            )
        }
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = stringResource(R.string.credentials_title),
            modifier = Modifier.fillMaxWidth(),
            color = TextPrimary,
            style = MaterialTheme.typography.displayLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.credentials_subtitle),
            color = TextSecondary,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(28.dp))
        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.credentials_username)) },
            singleLine = true,
            colors = credentialFieldColors(),
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.credentials_password)) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            colors = credentialFieldColors(),
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.credentials_recovery_warning),
            color = TextSecondary,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
        )
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onComplete,
            enabled = canComplete,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentBlue,
                contentColor = DarkBackground,
            ),
        ) {
            Text(stringResource(R.string.credentials_create_action))
        }
    }
}

@Composable
private fun credentialFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedBorderColor = AccentBlue,
    unfocusedBorderColor = TextSecondary,
    focusedLabelColor = AccentBlue,
    unfocusedLabelColor = TextSecondary,
    cursorColor = AccentBlue,
)

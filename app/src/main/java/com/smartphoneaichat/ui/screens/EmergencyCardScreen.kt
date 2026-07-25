package com.smartphoneaichat.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.smartphoneaichat.presentation.emergency.EmergencyCardUiState
import java.text.DateFormat
import java.util.Date

/** Locked-safe emergency screen: it receives a projection, never a vault record or profile. */
@Composable
fun EmergencyCardScreen(
    state: EmergencyCardUiState,
    isVaultUnlocked: Boolean,
    onRequestPublish: () -> Unit,
    onDismissExposureWarning: () -> Unit,
    onConfirmPublish: () -> Unit,
    onRevoke: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Emergency card",
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.headlineLarge,
        )
        val projection = state.projection
        if (state.isUnavailable) {
            Text("Emergency card is unavailable on this device.", style = MaterialTheme.typography.bodyLarge)
        } else if (projection == null) {
            Text("No emergency card is published on this device.", style = MaterialTheme.typography.bodyLarge)
        } else {
            Text(projection.preferredName, style = MaterialTheme.typography.displaySmall)
            Text(
                "Last refreshed: ${DateFormat.getDateTimeInstance().format(Date(projection.lastRefreshedAtEpochMillis))}",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Text(
            "This is user-selected reference information. Call local emergency services and seek professional care.",
            style = MaterialTheme.typography.bodyLarge,
        )
        if (isVaultUnlocked && state.currentPreferredName != null) {
            if (state.isUpdateAvailable) {
                Text("Update available: your published name differs from your current profile.")
            }
            Button(onClick = onRequestPublish) {
                Text(if (projection == null) "Publish emergency card" else "Update emergency card")
            }
            if (projection != null) {
                OutlinedButton(onClick = onRevoke) { Text("Disable emergency card") }
            }
        }
    }
    if (state.showExposureWarning) {
        AlertDialog(
            onDismissRequest = onDismissExposureWarning,
            title = { Text("Share emergency information?") },
            text = {
                Text("When the app is locked, anyone with this device can see your preferred name: ${state.currentPreferredName}.")
            },
            confirmButton = { Button(onClick = onConfirmPublish) { Text("Publish name") } },
            dismissButton = { OutlinedButton(onClick = onDismissExposureWarning) { Text("Cancel") } },
        )
    }
}

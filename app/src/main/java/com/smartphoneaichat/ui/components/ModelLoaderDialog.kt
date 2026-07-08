package com.smartphoneaichat.ui.components

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.smartphoneaichat.ui.theme.AccentBlue
import com.smartphoneaichat.ui.theme.AccentGreen
import com.smartphoneaichat.ui.theme.AccentRed
import com.smartphoneaichat.ui.theme.DarkSurfaceVariant
import com.smartphoneaichat.ui.theme.TextPrimary
import com.smartphoneaichat.ui.theme.TextSecondary

@Composable
fun ModelLoaderDialog(
    progress: Float,
    phase: String = "",
    onCancel: () -> Unit = {},
    onDismiss: () -> Unit
) {
    val isComplete = progress >= 1f
    val percent by animateIntAsState(
        targetValue = (progress * 100).toInt().coerceIn(0, 100),
        label = "progressPercent"
    )

    AlertDialog(
        onDismissRequest = { if (isComplete) onDismiss() },
        containerColor = DarkSurfaceVariant,
        icon = {
            Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = null,
                tint = if (isComplete) AccentGreen else AccentBlue,
                modifier = Modifier.size(28.dp)
            )
        },
        title = {
            Text(
                text = if (isComplete) "Model Loaded" else if (phase.isNotEmpty()) phase else "Loading Model",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = if (isComplete) AccentGreen else AccentBlue,
                    trackColor = AccentBlue.copy(alpha = 0.15f)
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "$percent%",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isComplete) AccentGreen else TextSecondary
                )
            }
        },
        dismissButton = {
            if (!isComplete) {
                TextButton(onClick = onCancel) {
                    Text("Cancel", color = AccentRed)
                }
            }
        },
        confirmButton = {
            if (isComplete) {
                TextButton(onClick = onDismiss) {
                    Text("Done", color = AccentBlue)
                }
            }
        }
    )
}
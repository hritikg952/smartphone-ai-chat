package com.smartphoneaichat.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.smartphoneaichat.ui.navigation.AppRoute

/** Safe empty destination used until a feature owns its data and actions. */
@Composable
fun VaultDestinationScreen(
    route: AppRoute,
    onNavigate: (AppRoute) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = route.displayName,
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = if (route == AppRoute.Emergency) {
                "No emergency card is configured. Unlock the vault to manage emergency information."
            } else {
                "This area is ready for its health-record feature. No health information is shown here yet."
            },
            style = MaterialTheme.typography.bodyLarge,
        )
        if (route == AppRoute.Profile) {
            OutlinedButton(onClick = { onNavigate(AppRoute.Settings) }) {
                Text("Open Settings")
            }
        }
    }
}

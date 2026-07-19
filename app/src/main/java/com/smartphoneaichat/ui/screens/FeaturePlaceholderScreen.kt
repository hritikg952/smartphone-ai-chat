package com.smartphoneaichat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.smartphoneaichat.ui.navigation.AppRoute
import com.smartphoneaichat.ui.theme.DarkBackground
import com.smartphoneaichat.ui.theme.TextPrimary
import com.smartphoneaichat.ui.theme.TextSecondary

/** Addressable shell route that reserves ownership for a later mini-goal. */
@Composable
fun FeaturePlaceholderScreen(
    route: AppRoute,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 24.dp, vertical = 56.dp),
    ) {
        Button(onClick = onBack) {
            Text("Back")
        }
        Text(
            text = route.displayName,
            modifier = Modifier.padding(top = 32.dp),
            color = TextPrimary,
            style = MaterialTheme.typography.displayLarge,
        )
        Text(
            text = "This protected feature route is ready for its implementation mini-goal.",
            modifier = Modifier.padding(top = 12.dp),
            color = TextSecondary,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

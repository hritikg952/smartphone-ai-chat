package com.smartphoneaichat.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.smartphoneaichat.R
import com.smartphoneaichat.ui.theme.DarkBackground
import com.smartphoneaichat.ui.theme.TextPrimary
import com.smartphoneaichat.ui.theme.TextSecondary
import com.smartphoneaichat.ui.theme.AccentBlue
import com.smartphoneaichat.ui.navigation.AppRoute

/** Initial safe landing surface for an unlocked vault. */
@Composable
fun HomeScreen(
    onNavigate: (AppRoute) -> Unit,
    onLock: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 56.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.home_title),
            color = TextPrimary,
            style = MaterialTheme.typography.displayLarge,
        )
        Text(
            text = stringResource(R.string.home_subtitle),
            modifier = Modifier.padding(bottom = 20.dp),
            color = TextSecondary,
            style = MaterialTheme.typography.bodyLarge,
        )
        Button(
            onClick = onLock,
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentBlue,
                contentColor = DarkBackground,
            ),
        ) {
            Text("Lock vault")
        }
        AppRoute.protectedRoutes
            .filterNot { it == AppRoute.Home }
            .forEach { route ->
                Button(
                    onClick = { onNavigate(route) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentBlue.copy(alpha = 0.16f),
                        contentColor = TextPrimary,
                    ),
                ) {
                    Text(route.displayName)
                }
            }
    }
}

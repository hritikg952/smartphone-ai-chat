package com.smartphoneaichat.ui.app

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.smartphoneaichat.ui.navigation.AppRoute

/** The adaptive, unlocked-only application shell for vault destinations. */
@Composable
fun VaultShell(
    currentRoute: AppRoute,
    profileLabel: String,
    onNavigate: (AppRoute) -> Unit,
    onLock: () -> Unit,
    content: @Composable (Modifier) -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val expanded = maxWidth >= 600.dp
        if (expanded) {
            Row(Modifier.fillMaxSize()) {
                VaultNavigationRail(currentRoute, onNavigate)
                VaultContent(profileLabel, onLock, content)
            }
        } else {
            Scaffold(
                topBar = { VaultTopAppBar(profileLabel, onLock) },
                bottomBar = { VaultNavigationBar(currentRoute, onNavigate) },
                content = { paddingValues -> content(Modifier.padding(paddingValues)) },
            )
        }
    }
}

@Composable
private fun VaultContent(profileLabel: String, onLock: () -> Unit, content: @Composable (Modifier) -> Unit) {
    Scaffold(
        topBar = { VaultTopAppBar(profileLabel, onLock) },
        content = { paddingValues -> content(Modifier.padding(paddingValues)) },
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun VaultTopAppBar(profileLabel: String, onLock: () -> Unit) {
    TopAppBar(
        title = { Text("Health Vault") },
        actions = {
            Text(
                text = profileLabel,
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.labelLarge,
            )
            IconButton(onClick = onLock) {
                Icon(Icons.Default.Lock, contentDescription = "Lock vault")
            }
        },
    )
}

@Composable
private fun VaultNavigationBar(currentRoute: AppRoute, onNavigate: (AppRoute) -> Unit) {
    NavigationBar {
        AppRoute.primaryRoutes.forEach { route ->
            NavigationBarItem(
                selected = currentRoute == route,
                onClick = { onNavigate(route) },
                icon = { Icon(route.icon, contentDescription = route.displayName) },
                label = { Text(route.displayName) },
            )
        }
    }
}

@Composable
private fun VaultNavigationRail(currentRoute: AppRoute, onNavigate: (AppRoute) -> Unit) {
    NavigationRail {
        AppRoute.primaryRoutes.forEach { route ->
            NavigationRailItem(
                selected = currentRoute == route,
                onClick = { onNavigate(route) },
                icon = { Icon(route.icon, contentDescription = route.displayName) },
                label = { Text(route.displayName) },
            )
        }
    }
}

private val AppRoute.icon: ImageVector
    get() = when (this) {
        AppRoute.Home -> Icons.Default.Home
        AppRoute.Records -> Icons.Default.Folder
        AppRoute.Add -> Icons.Default.AddCircle
        AppRoute.Insights -> Icons.Default.Analytics
        AppRoute.Profile -> Icons.Default.Person
        else -> Icons.Default.Folder
    }

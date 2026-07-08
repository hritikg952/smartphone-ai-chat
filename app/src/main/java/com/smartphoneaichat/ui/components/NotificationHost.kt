package com.smartphoneaichat.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.smartphoneaichat.presentation.notification.AppNotificationEvent
import com.smartphoneaichat.presentation.notification.AppNotificationManager
import kotlinx.coroutines.flow.collectLatest

@Composable
fun NotificationHost(
    notificationManager: AppNotificationManager,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    LaunchedEffect(notificationManager) {
        notificationManager.events.collectLatest { event ->
            when (event) {
                is AppNotificationEvent.Success -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is AppNotificationEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Box(modifier = modifier, content = content)
}
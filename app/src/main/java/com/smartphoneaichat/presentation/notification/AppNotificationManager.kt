package com.smartphoneaichat.presentation.notification

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Lightweight, centralized notification system for ephemeral toast-like alerts.
 *
 * Usage:
 *   val manager = remember { AppNotificationManager() }
 *   manager.show(AppNotificationEvent.Success("Image attached"))
 *
 * The UI layer collects [events] via a SharedFlow and displays them with a
 * Snackbar or similar mechanism (see [NotificationHost]).
 *
 * AI INTEGRATION NOTE:
 * This is intentionally decoupled from any UI framework. In a production app
 * you could swap the Snackbar renderer for an in-app toast, a popup, or a
 * notification tray. The event flow remains the same.
 */
class AppNotificationManager {

    private val _events = MutableSharedFlow<AppNotificationEvent>(extraBufferCapacity = 5)
    val events = _events.asSharedFlow()

    /** Emit a notification event that the UI layer will display. */
    fun show(event: AppNotificationEvent) {
        _events.tryEmit(event)
    }
}

/**
 * Sealed hierarchy of notification types.
 *
 * Add new types here as the app grows (e.g., Warning, Info).
 */
sealed interface AppNotificationEvent {
    data class Success(val message: String) : AppNotificationEvent
    data class Error(val message: String) : AppNotificationEvent
}
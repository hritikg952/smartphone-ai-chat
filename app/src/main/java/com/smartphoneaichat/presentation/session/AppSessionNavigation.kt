package com.smartphoneaichat.presentation.session

/**
 * Application-level state used to choose the first safe destination.
 *
 * Feature state belongs to its feature owner rather than this global session.
 */
data class AppSessionState(
    val hasCompletedOnboarding: Boolean,
    val isVaultUnlocked: Boolean,
)

/** Top-level destinations that can be selected before Compose navigation starts. */
enum class AppDestination {
    Onboarding,
    Unlock,
    Home,
}

/** Resolves application session state to the first visible destination. */
object AppDestinationResolver {

    fun resolve(sessionState: AppSessionState): AppDestination = when {
        !sessionState.hasCompletedOnboarding -> AppDestination.Onboarding
        !sessionState.isVaultUnlocked -> AppDestination.Unlock
        else -> AppDestination.Home
    }
}

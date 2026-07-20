package com.smartphoneaichat.presentation.session

typealias AppSessionState = com.smartphoneaichat.domain.model.AppSessionState

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

package com.smartphoneaichat.presentation.session

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AppDestinationResolverTest {

    @Test
    fun freshInstallation_opensOnboarding() {
        val sessionState = AppSessionState(
            hasCompletedOnboarding = false,
            isVaultUnlocked = false,
        )

        val destination = AppDestinationResolver.resolve(sessionState)

        assertEquals(AppDestination.Onboarding, destination)
    }

    @Test
    fun completedOnboardingWithLockedVault_opensUnlock() {
        val sessionState = AppSessionState(
            hasCompletedOnboarding = true,
            isVaultUnlocked = false,
        )

        val destination = AppDestinationResolver.resolve(sessionState)

        assertEquals(AppDestination.Unlock, destination)
    }

    @Test
    fun completedOnboardingWithUnlockedVault_opensHome() {
        val sessionState = AppSessionState(
            hasCompletedOnboarding = true,
            isVaultUnlocked = true,
        )

        val destination = AppDestinationResolver.resolve(sessionState)

        assertEquals(AppDestination.Home, destination)
    }
}

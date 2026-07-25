package com.smartphoneaichat.ui.navigation

import com.smartphoneaichat.domain.model.AppSessionState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AppRoutePolicyTest {

    @Test
    fun primaryRoutesExposeTheHealthVaultInformationArchitecture() {
        assertEquals(
            listOf(
                AppRoute.Home,
                AppRoute.Records,
                AppRoute.Add,
                AppRoute.Insights,
                AppRoute.Profile,
            ),
            AppRoute.primaryRoutes,
        )
    }

    @Test
    fun lockedSessionCanReachPublicEmergencyPlaceholderWithoutUnlockingVault() {
        val lockedSession = AppSessionState(
            hasCompletedOnboarding = true,
            isVaultUnlocked = false,
        )

        assertEquals(AppRoute.Emergency, AppRoutePolicy.resolve(AppRoute.Emergency, lockedSession))
    }

    @Test
    fun lockedSessionRedirectsEveryProtectedRouteToUnlock() {
        val lockedSession = AppSessionState(
            hasCompletedOnboarding = true,
            isVaultUnlocked = false,
        )

        AppRoute.protectedRoutes.forEach { requestedRoute ->
            assertEquals(
                AppRoute.Unlock,
                AppRoutePolicy.resolve(requestedRoute, lockedSession),
                "Expected $requestedRoute to be protected while locked",
            )
        }
    }

    @Test
    fun freshSessionRedirectsProtectedRequestToOnboarding() {
        val freshSession = AppSessionState(
            hasCompletedOnboarding = false,
            isVaultUnlocked = false,
        )

        assertEquals(
            AppRoute.Onboarding,
            AppRoutePolicy.resolve(AppRoute.Reports, freshSession),
        )
    }

    @Test
    fun unlockedSessionCanAddressEveryProtectedRoute() {
        val unlockedSession = AppSessionState(
            hasCompletedOnboarding = true,
            isVaultUnlocked = true,
        )

        AppRoute.protectedRoutes.forEach { requestedRoute ->
            assertEquals(
                requestedRoute,
                AppRoutePolicy.resolve(requestedRoute, unlockedSession),
            )
        }
    }

    @Test
    fun unknownRouteFallsBackToSafeSessionDestination() {
        val unknownRoute = AppRoute.fromPath("not-allowlisted")

        assertEquals(
            AppRoute.Unlock,
            AppRoutePolicy.resolve(
                unknownRoute,
                AppSessionState(
                    hasCompletedOnboarding = true,
                    isVaultUnlocked = false,
                ),
            ),
        )
    }
}

package com.smartphoneaichat.ui.navigation

import com.smartphoneaichat.domain.model.AppSessionState

/** Allowlisted top-level routes for the Health Vault shell. */
enum class AppRoute(
    val path: String,
    val displayName: String,
    val requiresUnlockedVault: Boolean,
) {
    Onboarding("onboarding", "Onboarding", false),
    Unlock("unlock", "Unlock", false),
    Home("home", "Home", true),
    Profiles("profiles", "Profiles", true),
    Emergency("emergency", "Emergency", true),
    Medications("medications", "Medications", true),
    Reports("reports", "Reports", true),
    Vitals("vitals", "Vitals", true),
    Journal("journal", "Journal", true),
    Insurance("insurance", "Insurance", true),
    Search("search", "Search", true),
    Sharing("sharing", "Sharing", true),
    Settings("settings", "Settings", true),
    Assistant("assistant", "Assistant", true),
    ;

    companion object {
        val protectedRoutes: List<AppRoute> = entries.filter { it.requiresUnlockedVault }

        fun fromPath(path: String?): AppRoute? = entries.singleOrNull { it.path == path }
    }
}

/** Applies onboarding and lock redirects before any route can render. */
object AppRoutePolicy {
    fun resolve(
        requestedRoute: AppRoute?,
        sessionState: AppSessionState,
    ): AppRoute = when {
        !sessionState.hasCompletedOnboarding -> AppRoute.Onboarding
        requestedRoute == null -> if (sessionState.isVaultUnlocked) AppRoute.Home else AppRoute.Unlock
        requestedRoute.requiresUnlockedVault && !sessionState.isVaultUnlocked -> AppRoute.Unlock
        requestedRoute == AppRoute.Onboarding -> if (sessionState.isVaultUnlocked) AppRoute.Home else AppRoute.Unlock
        requestedRoute == AppRoute.Unlock && sessionState.isVaultUnlocked -> AppRoute.Home
        else -> requestedRoute
    }
}

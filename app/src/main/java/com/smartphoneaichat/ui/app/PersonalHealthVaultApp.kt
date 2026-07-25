package com.smartphoneaichat.ui.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.smartphoneaichat.R
import com.smartphoneaichat.presentation.onboarding.OnboardingStep
import com.smartphoneaichat.presentation.onboarding.OnboardingUiState
import com.smartphoneaichat.presentation.security.VaultAccessError
import com.smartphoneaichat.presentation.security.VaultAccessUiState
import com.smartphoneaichat.presentation.session.AppSessionState
import com.smartphoneaichat.presentation.emergency.EmergencyCardUiState
import com.smartphoneaichat.ui.navigation.AppRoute
import com.smartphoneaichat.ui.navigation.AppRoutePolicy
import com.smartphoneaichat.ui.screens.CredentialsSetupScreen
import com.smartphoneaichat.ui.screens.HomeScreen
import com.smartphoneaichat.ui.screens.EmergencyCardScreen
import com.smartphoneaichat.ui.screens.OnboardingScreen
import com.smartphoneaichat.ui.screens.UnlockScreen
import com.smartphoneaichat.ui.screens.VaultDestinationScreen
import com.smartphoneaichat.ui.theme.DarkBackground
import com.smartphoneaichat.ui.theme.SmartphoneAIChatTheme

/** Root Compose seam for application-level destination selection. */
@Composable
fun PersonalHealthVaultApp(
    sessionState: AppSessionState,
    modifier: Modifier = Modifier,
    onboardingState: OnboardingUiState = OnboardingUiState(),
    vaultAccessState: VaultAccessUiState = VaultAccessUiState(),
    onGetStarted: () -> Unit = {},
    onUnlock: () -> Unit = {},
    onLock: () -> Unit = {},
    onUsernameChanged: (String) -> Unit = {},
    onPasswordChanged: (String) -> Unit = {},
    onCompleteOnboarding: () -> Unit = {},
    selectedProfileLabel: String = "Self profile",
    emergencyCardState: EmergencyCardUiState = EmergencyCardUiState(),
    onRequestEmergencyPublish: () -> Unit = {},
    onDismissEmergencyExposureWarning: () -> Unit = {},
    onConfirmEmergencyPublish: () -> Unit = {},
    onRevokeEmergencyCard: () -> Unit = {},
) {
    SmartphoneAIChatTheme {
        val navController = rememberNavController()
        val startRoute = remember {
            AppRoutePolicy.resolve(requestedRoute = null, sessionState = sessionState)
        }
        val currentEntry by navController.currentBackStackEntryAsState()
        val currentRoute = AppRoute.fromPath(currentEntry?.destination?.route)
        val accessError = vaultAccessState.error.toMessage()
        val navigateSafely: (AppRoute) -> Unit = { requestedRoute ->
            val safeRoute = AppRoutePolicy.resolve(requestedRoute, sessionState)
            navController.navigate(safeRoute.path) {
                if (safeRoute in AppRoute.primaryRoutes) {
                    popUpTo(AppRoute.Home.path) {
                        inclusive = false
                        saveState = true
                    }
                    restoreState = true
                }
                launchSingleTop = true
            }
        }

        LaunchedEffect(sessionState, currentRoute) {
            val safeRoute = AppRoutePolicy.resolve(currentRoute, sessionState)
            if (currentRoute != null && safeRoute != currentRoute) {
                navController.navigate(safeRoute.path) {
                    popUpTo(currentRoute.path) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }

        Surface(modifier = modifier.fillMaxSize(), color = DarkBackground) {
            val destinations: @Composable (Modifier) -> Unit = { contentModifier ->
                NavHost(
                    navController = navController,
                    startDestination = startRoute.path,
                    modifier = contentModifier,
                ) {
                    composable(AppRoute.Onboarding.path) {
                        when (onboardingState.step) {
                            OnboardingStep.Welcome -> OnboardingScreen(onGetStarted = onGetStarted)
                            OnboardingStep.Credentials -> CredentialsSetupScreen(
                                username = vaultAccessState.username,
                                password = vaultAccessState.password,
                                canComplete = vaultAccessState.canSubmit,
                                errorMessage = accessError,
                                onUsernameChanged = onUsernameChanged,
                                onPasswordChanged = onPasswordChanged,
                                onComplete = onCompleteOnboarding,
                            )
                        }
                    }
                    composable(AppRoute.Unlock.path) {
                        UnlockScreen(
                            username = vaultAccessState.username,
                            password = vaultAccessState.password,
                            canUnlock = vaultAccessState.canSubmit,
                            errorMessage = accessError,
                            onUsernameChanged = onUsernameChanged,
                            onPasswordChanged = onPasswordChanged,
                            onUnlock = onUnlock,
                        )
                    }
                    composable(AppRoute.Home.path) {
                        HomeScreen(
                            onNavigate = navigateSafely,
                            hasEmergencyCard = emergencyCardState.projection != null,
                        )
                    }
                    composable(AppRoute.Emergency.path) {
                        EmergencyCardScreen(
                            state = emergencyCardState,
                            isVaultUnlocked = sessionState.isVaultUnlocked,
                            onRequestPublish = onRequestEmergencyPublish,
                            onDismissExposureWarning = onDismissEmergencyExposureWarning,
                            onConfirmPublish = onConfirmEmergencyPublish,
                            onRevoke = onRevokeEmergencyCard,
                        )
                    }
                    AppRoute.protectedRoutes.filterNot { it == AppRoute.Home }.forEach { route ->
                        composable(route.path) {
                            VaultDestinationScreen(route, onNavigate = navigateSafely)
                        }
                    }
                }
            }

            if (currentRoute?.requiresUnlockedVault == true && sessionState.isVaultUnlocked) {
                VaultShell(
                    currentRoute = requireNotNull(currentRoute),
                    profileLabel = selectedProfileLabel,
                    onNavigate = navigateSafely,
                    onLock = onLock,
                    content = destinations,
                )
            } else {
                destinations(Modifier)
            }
        }
    }
}

@Composable
private fun VaultAccessError?.toMessage(): String? = when (this) {
    VaultAccessError.InvalidCredentials -> stringResource(R.string.vault_error_invalid_credentials)
    VaultAccessError.AlreadyExists -> stringResource(R.string.vault_error_already_exists)
    VaultAccessError.KeyInvalidated -> stringResource(R.string.vault_error_key_invalidated)
    VaultAccessError.Unavailable -> stringResource(R.string.vault_error_unavailable)
    null -> null
}

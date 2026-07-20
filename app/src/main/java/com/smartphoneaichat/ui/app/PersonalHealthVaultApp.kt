package com.smartphoneaichat.ui.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.smartphoneaichat.presentation.onboarding.OnboardingStep
import com.smartphoneaichat.presentation.onboarding.OnboardingUiState
import com.smartphoneaichat.presentation.session.AppSessionState
import com.smartphoneaichat.ui.screens.OnboardingScreen
import com.smartphoneaichat.ui.screens.CredentialsSetupScreen
import com.smartphoneaichat.ui.screens.UnlockScreen
import com.smartphoneaichat.ui.screens.HomeScreen
import com.smartphoneaichat.ui.screens.FeaturePlaceholderScreen
import com.smartphoneaichat.ui.navigation.AppRoute
import com.smartphoneaichat.ui.navigation.AppRoutePolicy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.smartphoneaichat.ui.theme.DarkBackground
import com.smartphoneaichat.ui.theme.SmartphoneAIChatTheme

/** Root Compose seam for application-level destination selection. */
@Composable
fun PersonalHealthVaultApp(
    sessionState: AppSessionState,
    modifier: Modifier = Modifier,
    onboardingState: OnboardingUiState = OnboardingUiState(),
    onGetStarted: () -> Unit = {},
    onUnlock: () -> Unit = {},
    onLock: () -> Unit = {},
    onUsernameChanged: (String) -> Unit = {},
    onPasswordChanged: (String) -> Unit = {},
    onCompleteOnboarding: () -> Unit = {},
) {
    SmartphoneAIChatTheme {
        val navController = rememberNavController()
        val startRoute = remember {
            AppRoutePolicy.resolve(requestedRoute = null, sessionState = sessionState)
        }
        val currentEntry by navController.currentBackStackEntryAsState()
        val currentRoute = AppRoute.fromPath(currentEntry?.destination?.route)

        LaunchedEffect(sessionState, currentRoute) {
            val safeRoute = AppRoutePolicy.resolve(currentRoute, sessionState)
            if (currentRoute != null && safeRoute != currentRoute) {
                navController.navigate(safeRoute.path) {
                    popUpTo(currentRoute.path) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }

        Surface(
            modifier = modifier.fillMaxSize(),
            color = DarkBackground,
        ) {
            NavHost(
                navController = navController,
                startDestination = startRoute.path,
            ) {
                composable(AppRoute.Onboarding.path) {
                    when (onboardingState.step) {
                        OnboardingStep.Welcome -> OnboardingScreen(onGetStarted = onGetStarted)
                        OnboardingStep.Credentials -> CredentialsSetupScreen(
                            username = onboardingState.username,
                            password = onboardingState.password,
                            canComplete = onboardingState.canCompleteSetup,
                            onUsernameChanged = onUsernameChanged,
                            onPasswordChanged = onPasswordChanged,
                            onComplete = onCompleteOnboarding,
                        )
                    }
                }
                composable(AppRoute.Unlock.path) {
                    UnlockScreen(onUnlock = onUnlock)
                }
                composable(AppRoute.Home.path) {
                    HomeScreen(
                        onNavigate = { requestedRoute ->
                            val safeRoute = AppRoutePolicy.resolve(requestedRoute, sessionState)
                            navController.navigate(safeRoute.path) { launchSingleTop = true }
                        },
                        onLock = onLock,
                    )
                }
                AppRoute.protectedRoutes
                    .filterNot { it == AppRoute.Home }
                    .forEach { route ->
                        composable(route.path) {
                            FeaturePlaceholderScreen(
                                route = route,
                                onBack = navController::popBackStack,
                            )
                        }
                    }
            }
        }
    }
}

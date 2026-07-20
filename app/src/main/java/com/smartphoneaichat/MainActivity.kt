package com.smartphoneaichat

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smartphoneaichat.presentation.onboarding.OnboardingViewModel
import com.smartphoneaichat.presentation.security.VaultAccessViewModel
import com.smartphoneaichat.presentation.security.VaultAccessViewModelFactory
import com.smartphoneaichat.ui.app.PersonalHealthVaultApp

/** Single Android entry point for the Personal Health Vault application. */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        enableEdgeToEdge()
        val container = (application as App).healthVaultContainer
        val appSessionStore = container.appSessionStore

        setContent {
            val onboardingViewModel: OnboardingViewModel = viewModel()
            val vaultAccessViewModel: VaultAccessViewModel = viewModel(
                factory = VaultAccessViewModelFactory(
                    keyManager = container.vaultKeyManager,
                    vaultSession = container.vaultSession,
                    appSessionStore = appSessionStore,
                ),
            )
            val onboardingState by onboardingViewModel.state.collectAsStateWithLifecycle()
            val vaultAccessState by vaultAccessViewModel.state.collectAsStateWithLifecycle()
            val sessionState by appSessionStore.state.collectAsStateWithLifecycle()
            PersonalHealthVaultApp(
                sessionState = sessionState,
                onboardingState = onboardingState,
                vaultAccessState = vaultAccessState,
                onGetStarted = onboardingViewModel::onGetStarted,
                onUsernameChanged = vaultAccessViewModel::onUsernameChanged,
                onPasswordChanged = vaultAccessViewModel::onPasswordChanged,
                onCompleteOnboarding = vaultAccessViewModel::createVault,
                onUnlock = vaultAccessViewModel::unlockVault,
                onLock = vaultAccessViewModel::lockVault,
            )
        }
    }

    override fun onStop() {
        (application as App).healthVaultContainer.vaultSession.lock()
        super.onStop()
    }
}

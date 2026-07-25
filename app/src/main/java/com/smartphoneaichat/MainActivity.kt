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
import com.smartphoneaichat.presentation.emergency.EmergencyCardViewModel
import com.smartphoneaichat.presentation.emergency.EmergencyCardViewModelFactory
import com.smartphoneaichat.presentation.medication.MedicationViewModel
import com.smartphoneaichat.presentation.medication.MedicationViewModelFactory
import androidx.compose.runtime.LaunchedEffect
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
                    selfProfileInitializer = container.selfProfileInitializer,
                    profileSessionCoordinator = container.profileSessionCoordinator,
                    auditRepository = container.auditRepository,
                ),
            )
            val emergencyCardViewModel: EmergencyCardViewModel = viewModel(
                factory = EmergencyCardViewModelFactory(
                    emergencyCards = container.emergencyCardRepository,
                    profiles = container.profileRepository,
                    audit = container.auditRepository,
                    vaultSession = container.vaultSession,
                ),
            )
            val medicationViewModel: MedicationViewModel = viewModel(
                factory = MedicationViewModelFactory(
                    medications = container.medicationRepository,
                    providers = container.providerRepository,
                ),
            )
            val onboardingState by onboardingViewModel.state.collectAsStateWithLifecycle()
            val vaultAccessState by vaultAccessViewModel.state.collectAsStateWithLifecycle()
            val emergencyCardState by emergencyCardViewModel.state.collectAsStateWithLifecycle()
            val medicationState by medicationViewModel.state.collectAsStateWithLifecycle()
            val sessionState by appSessionStore.state.collectAsStateWithLifecycle()
            val selectedProfileContext by container.profileSessionCoordinator.currentContext
                .collectAsStateWithLifecycle()
            val selectedProfileLabel = selectedProfileContext?.takeIf { sessionState.isVaultUnlocked }?.let { context ->
                container.profileRepository.get(context)?.let { profile ->
                    "${profile.displayName} · ${profile.relationship.name.lowercase()}"
                }
            } ?: "Self profile"
            LaunchedEffect(sessionState.isVaultUnlocked, selectedProfileContext) {
                emergencyCardViewModel.refresh(sessionState.isVaultUnlocked, selectedProfileContext)
                medicationViewModel.refresh(selectedProfileContext.takeIf { sessionState.isVaultUnlocked })
            }
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
                selectedProfileLabel = selectedProfileLabel,
                emergencyCardState = emergencyCardState,
                onRequestEmergencyPublish = emergencyCardViewModel::requestPublish,
                onDismissEmergencyExposureWarning = emergencyCardViewModel::dismissExposureWarning,
                onConfirmEmergencyPublish = { emergencyCardViewModel.confirmPublish(selectedProfileContext) },
                onRevokeEmergencyCard = { emergencyCardViewModel.revoke(selectedProfileContext) },
                medicationState = medicationState,
                onSaveMedication = { regimen -> selectedProfileContext?.let { medicationViewModel.saveRegimen(it, regimen) } ?: false },
                onSaveProvider = { provider -> selectedProfileContext?.let { medicationViewModel.saveProvider(it, provider) } ?: false },
            )
        }
    }

    override fun onStop() {
        (application as App).healthVaultContainer.vaultSession.lock()
        super.onStop()
    }
}

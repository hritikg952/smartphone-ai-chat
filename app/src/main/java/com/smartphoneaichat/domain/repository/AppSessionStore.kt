package com.smartphoneaichat.domain.repository

import com.smartphoneaichat.domain.model.AppSessionState
import kotlinx.coroutines.flow.StateFlow

/** Owns durable onboarding status and process-local vault access state. */
interface AppSessionStore {
    val state: StateFlow<AppSessionState>

    fun completeOnboarding()
    fun unlockVault()
    fun lockVault()
}

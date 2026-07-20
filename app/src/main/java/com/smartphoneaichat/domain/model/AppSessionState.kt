package com.smartphoneaichat.domain.model

/** Minimal global state used to select a safe application destination. */
data class AppSessionState(
    val hasCompletedOnboarding: Boolean,
    val isVaultUnlocked: Boolean,
)

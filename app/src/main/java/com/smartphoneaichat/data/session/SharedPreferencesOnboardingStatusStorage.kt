package com.smartphoneaichat.data.session

import android.content.Context

/** Persists only the non-secret fact that first-run onboarding has completed. */
class SharedPreferencesOnboardingStatusStorage(
    context: Context,
) : OnboardingStatusStorage {
    private val preferences = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)

    override fun readHasCompletedOnboarding(): Boolean =
        preferences.getBoolean(KEY_HAS_COMPLETED_ONBOARDING, false)

    override fun writeHasCompletedOnboarding(value: Boolean) {
        preferences.edit().putBoolean(KEY_HAS_COMPLETED_ONBOARDING, value).apply()
    }

    private companion object {
        const val FILE_NAME = "health_vault_session"
        const val KEY_HAS_COMPLETED_ONBOARDING = "has_completed_onboarding"
    }
}

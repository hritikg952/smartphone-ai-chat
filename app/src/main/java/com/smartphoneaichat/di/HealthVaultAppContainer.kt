package com.smartphoneaichat.di

import android.app.Application
import com.smartphoneaichat.data.session.DefaultAppSessionStore
import com.smartphoneaichat.data.session.SharedPreferencesOnboardingStatusStorage
import com.smartphoneaichat.domain.repository.AppSessionStore

/** Application-lifetime dependencies used by the active Health Vault product. */
class HealthVaultAppContainer(application: Application) {
    val appSessionStore: AppSessionStore = DefaultAppSessionStore(
        SharedPreferencesOnboardingStatusStorage(application),
    )
}

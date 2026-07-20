package com.smartphoneaichat

import android.app.Application
import com.smartphoneaichat.di.AppContainer
import com.smartphoneaichat.di.HealthVaultAppContainer

class App : Application() {

    val healthVaultContainer: HealthVaultAppContainer by lazy {
        HealthVaultAppContainer(this)
    }

    val legacyAppContainer: AppContainer by lazy {
        check(BuildConfig.LEGACY_RUNTIME_ENABLED) {
            "Legacy chat/model dependencies are available only in the legacy build."
        }
        AppContainer(this)
    }
}

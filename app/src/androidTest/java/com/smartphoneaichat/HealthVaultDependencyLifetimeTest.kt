package com.smartphoneaichat

import androidx.test.core.app.ActivityScenario
import org.junit.Assert.assertSame
import org.junit.Test

class HealthVaultDependencyLifetimeTest {

    @Test
    fun activityRecreationRetainsApplicationScopedSessionStore() {
        lateinit var sessionStoreBeforeRecreation: Any

        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                sessionStoreBeforeRecreation =
                    (activity.application as App).healthVaultContainer.appSessionStore
            }

            scenario.recreate()

            scenario.onActivity { activity ->
                assertSame(
                    sessionStoreBeforeRecreation,
                    (activity.application as App).healthVaultContainer.appSessionStore,
                )
            }
        }
    }
}

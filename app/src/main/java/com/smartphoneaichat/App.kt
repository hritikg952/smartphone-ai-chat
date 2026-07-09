package com.smartphoneaichat

import android.app.Application
import com.smartphoneaichat.di.AppContainer

class App : Application() {

    val appContainer: AppContainer by lazy { AppContainer(this) }
}

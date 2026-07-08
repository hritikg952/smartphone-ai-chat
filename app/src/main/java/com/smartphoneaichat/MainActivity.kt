package com.smartphoneaichat

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smartphoneaichat.di.AppContainer
import com.smartphoneaichat.di.ChatViewModelFactory
import com.smartphoneaichat.ui.screens.ChatScreen
import com.smartphoneaichat.ui.theme.SmartphoneAIChatTheme
import com.smartphoneaichat.presentation.viewmodel.ChatViewModel

/**
 * Single entry point for the application.
 *
 * This is the only Activity in the app. All screens are managed within
 * Compose navigation (currently a single screen [ChatScreen]).
 *
 * ===========================
 * LITERT-LM INTEGRATION
 * ===========================
 *
 * This app uses LiteRT-LM (Google's on-device LLM runtime) as the AI backend.
 *
 * REQUIRED SETUP:
 *
 * 1. HUGGINGFACE TOKEN
 *    Set HF_TOKEN in your gradle.properties or via the HF_TOKEN build config
 *    field in app/build.gradle.kts (used by HuggingFaceModelFileManager.kt).
 *    The model is gated — you must accept the Gemma license at:
 *    https://huggingface.co/litert-community/Gemma3-1B-IT
 *
 * 2. MODEL DOWNLOAD
 *    Tap the Download icon in the top bar → "Load Model".
 *    The app downloads gemma3-1b-it-int4.litertlm (584 MB) from HuggingFace
 *    to the device's internal storage, then initializes the LiteRT-LM engine.
 *
 * 3. GPU ACCELERATION (OPTIONAL)
 *    For faster inference, switch Backend.CPU() to Backend.GPU() in
 *    HuggingFaceModelFileManager.kt. INTERNET permission is already declared
 *    in AndroidManifest.xml.
 *
 * See data/model/HuggingFaceModelFileManager.kt and
 * data/engine/LiteRtInferenceEngine.kt for implementation details.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SmartphoneAIChatTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val context = LocalContext.current
                    val appContainer = remember { AppContainer(context.applicationContext as Application) }
                    val factory = remember { ChatViewModelFactory(appContainer, context.applicationContext as Application) }
                    val viewModel: ChatViewModel = viewModel(factory = factory)
                    ChatScreen(viewModel = viewModel)
                }
            }
        }
    }
}

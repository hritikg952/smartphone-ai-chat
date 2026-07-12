package com.smartphoneaichat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.smartphoneaichat.di.ChatViewModelFactory
import com.smartphoneaichat.di.ScannerViewModelFactory
import com.smartphoneaichat.presentation.viewmodel.ChatViewModel
import com.smartphoneaichat.presentation.viewmodel.ScannerViewModel
import com.smartphoneaichat.ui.components.BottomNavigationBar
import com.smartphoneaichat.ui.navigation.Screen
import com.smartphoneaichat.ui.screens.ChatScreen
import com.smartphoneaichat.ui.screens.ScannerScreen
import com.smartphoneaichat.ui.theme.DarkBackground
import com.smartphoneaichat.ui.theme.SmartphoneAIChatTheme
import com.smartphoneaichat.ui.theme.TextSecondary

/**
 * Single entry point for the application.
 *
 * This is the only Activity in the app. All screens are managed within
 * Compose navigation via [NavHost] with a bottom navigation bar.
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
                val context = LocalContext.current
                val app = context.applicationContext as App
                val factory = remember { ChatViewModelFactory(app.appContainer, app) }
                val viewModel: ChatViewModel = viewModel(factory = factory)
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val showBottomBar = currentRoute in Screen.all.map { it.route }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = DarkBackground,
                    bottomBar = {
                        if (showBottomBar) {
                            BottomNavigationBar(navController = navController)
                        }
                    },
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Chat.route,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    ) {
                        composable(Screen.Chat.route) { entry ->
                            ChatScreen(
                                viewModel = viewModel,
                                savedStateHandle = entry.savedStateHandle,
                            )
                        }
                        composable(Screen.Scanner.route) {
                            val scannerFactory = remember { ScannerViewModelFactory(app) }
                            val scannerViewModel: ScannerViewModel = viewModel(factory = scannerFactory)
                            ScannerScreen(
                                viewModel = scannerViewModel,
                                onImageCaptured = { path ->
                                    navController.previousBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("captured_image_path", path)
                                    navController.popBackStack()
                                }
                            )
                        }
                        composable(Screen.MedicineData.route) {
                            PlaceholderScreen(label = "Medicine Data")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(label: String) {
    Scaffold(
        containerColor = DarkBackground,
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "$label — Coming Soon",
                style = MaterialTheme.typography.headlineSmall,
                color = TextSecondary,
            )
        }
    }
}
package com.smartphoneaichat.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    data object Chat : Screen("chat", "Chat", Icons.AutoMirrored.Filled.Chat)
    data object Scanner : Screen("scanner", "Scanner", Icons.Default.QrCodeScanner)
    data object MedicineData : Screen("medicine_data", "Medicine Data", Icons.Default.MedicalServices)

    companion object {
        val all = listOf(Chat, Scanner, MedicineData)
    }
}

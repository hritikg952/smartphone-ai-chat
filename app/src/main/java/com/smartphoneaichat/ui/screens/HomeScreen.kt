package com.smartphoneaichat.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.smartphoneaichat.ui.navigation.AppRoute

/** Independent presentation states prevent one unavailable feature from blanking Home. */
sealed interface HomeSectionState {
    data object Empty : HomeSectionState
    data object Loading : HomeSectionState
    data class Error(val message: String) : HomeSectionState
}

data class HomeUiState(
    val medication: HomeSectionState = HomeSectionState.Empty,
    val recentRecords: HomeSectionState = HomeSectionState.Empty,
    val vitals: HomeSectionState = HomeSectionState.Empty,
    val appointments: HomeSectionState = HomeSectionState.Empty,
)

/** Initial safe landing surface for an unlocked vault. */
@Composable
fun HomeScreen(
    onNavigate: (AppRoute) -> Unit,
    state: HomeUiState = HomeUiState(),
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Home",
            modifier = Modifier.semantics { heading() },
            style = MaterialTheme.typography.headlineLarge,
        )
        Text(
            text = "Your health records stay organized here. Each section will become available as its feature is added.",
            style = MaterialTheme.typography.bodyLarge,
        )
        HomeSection("Emergency information", "No emergency card is configured.", onNavigate, AppRoute.Emergency)
        HomeSection("Today", sectionText("Medication schedule", state.medication), onNavigate, AppRoute.Medications)
        HomeSection("Recent records", sectionText("No health records yet", state.recentRecords), onNavigate, AppRoute.Records)
        HomeSection("Latest vitals", sectionText("No measurements yet", state.vitals), onNavigate, AppRoute.Vitals)
        HomeSection("Appointments and permissions", sectionText("No appointments or integration warnings", state.appointments), onNavigate, AppRoute.Profile)
    }
}

@Composable
private fun HomeSection(title: String, body: String, onNavigate: (AppRoute) -> Unit, destination: AppRoute) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        Text(text = body, style = MaterialTheme.typography.bodyMedium)
        OutlinedButton(onClick = { onNavigate(destination) }) {
            Text("Open ${destination.displayName}")
        }
    }
}

private fun sectionText(emptyText: String, state: HomeSectionState): String = when (state) {
    HomeSectionState.Empty -> emptyText
    HomeSectionState.Loading -> "Loading…"
    is HomeSectionState.Error -> state.message
}

package com.smartphoneaichat.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.smartphoneaichat.domain.model.DailyMedicationSchedule
import com.smartphoneaichat.domain.model.AsNeededMedicationSchedule
import com.smartphoneaichat.domain.model.MedicationRegimen
import com.smartphoneaichat.domain.model.MedicationStatus
import com.smartphoneaichat.domain.model.Provider
import com.smartphoneaichat.domain.model.UnsupportedMedicationSchedule
import com.smartphoneaichat.domain.model.WeeklyMedicationSchedule
import com.smartphoneaichat.presentation.medication.MedicationUiState
import java.time.LocalDate
import java.time.LocalTime
import java.time.DayOfWeek
import kotlinx.coroutines.launch

@Composable
fun MedicationScreen(
    state: MedicationUiState,
    onSaveMedication: suspend (MedicationRegimen) -> Boolean,
    onSaveProvider: suspend (Provider) -> Boolean,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    var editing by remember { mutableStateOf<MedicationRegimen?>(null) }
    var label by remember { mutableStateOf("") }
    var doseAmount by remember { mutableStateOf("1") }
    var doseUnit by remember { mutableStateOf("tablet") }
    var indication by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var route by remember { mutableStateOf("Oral") }
    var form by remember { mutableStateOf("Tablet") }
    var times by remember { mutableStateOf("08:00") }
    var scheduleKind by remember { mutableStateOf("Daily") }
    var weeklyDays by remember { mutableStateOf("MONDAY") }
    var startDateText by remember { mutableStateOf(LocalDate.now().toString()) }
    var endDateText by remember { mutableStateOf("") }
    var instruction by remember { mutableStateOf("") }
    var selectedProviderId by remember { mutableStateOf<String?>(null) }
    var status by remember { mutableStateOf(MedicationStatus.Active) }
    var providerName by remember { mutableStateOf("") }
    var providerSpecialty by remember { mutableStateOf("") }
    var providerFacility by remember { mutableStateOf("") }
    var providerContact by remember { mutableStateOf("") }
    var editingProvider by remember { mutableStateOf<Provider?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var isSavingMedication by remember { mutableStateOf(false) }
    var isSavingProvider by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Medications", Modifier.semantics { heading() }, style = MaterialTheme.typography.headlineLarge)
        Text("Schedules are local-only. Reminders and dose logging are not enabled.")
        if (state.today.isNotEmpty()) {
            Text("Today", style = MaterialTheme.typography.titleLarge)
            state.today.forEach { dose ->
                Text("${dose.localTime} · ${dose.label} · ${dose.dose} · ${dose.route}\n${dose.source}${dose.instruction.takeIf { it.isNotBlank() }?.let { " · $it" }.orEmpty()}")
            }
        }
        Text(if (editing == null) "Add medication" else "Edit medication", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(label, { label = it }, Modifier.fillMaxWidth(), label = { Text("Medication name") })
        OutlinedTextField(indication, { indication = it }, Modifier.fillMaxWidth(), label = { Text("Indication (optional)") })
        OutlinedTextField(startDateText, { startDateText = it }, Modifier.fillMaxWidth(), label = { Text("Start date (YYYY-MM-DD)") })
        OutlinedTextField(endDateText, { endDateText = it }, Modifier.fillMaxWidth(), label = { Text("End date (optional)") })
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(doseAmount, { doseAmount = it }, Modifier.weight(1f), label = { Text("Amount") })
            OutlinedTextField(doseUnit, { doseUnit = it }, Modifier.weight(1f), label = { Text("Unit") })
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(route, { route = it }, Modifier.weight(1f), label = { Text("Route") })
            OutlinedTextField(form, { form = it }, Modifier.weight(1f), label = { Text("Form") })
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Daily", "Weekly", "As needed", "Complex").forEach { candidate ->
                OutlinedButton(onClick = { scheduleKind = candidate }) { Text(if (scheduleKind == candidate) "✓ $candidate" else candidate) }
            }
        }
        if (scheduleKind == "Daily" || scheduleKind == "Weekly") {
            OutlinedTextField(times, { times = it }, Modifier.fillMaxWidth(), label = { Text("Times (HH:mm, comma-separated)") })
        }
        if (scheduleKind == "Weekly") {
            OutlinedTextField(weeklyDays, { weeklyDays = it }, Modifier.fillMaxWidth(), label = { Text("Weekdays (e.g. MONDAY,WEDNESDAY)") })
        }
        OutlinedTextField(instruction, { instruction = it }, Modifier.fillMaxWidth(), label = { Text("Instruction (optional)") })
        OutlinedTextField(notes, { notes = it }, Modifier.fillMaxWidth(), label = { Text("Notes (optional)") })
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MedicationStatus.entries.forEach { candidate ->
                OutlinedButton(onClick = { status = candidate }) { Text(if (status == candidate) "✓ $candidate" else candidate.name) }
            }
        }
        Text("Prescriber: ${state.providers.firstOrNull { it.id == selectedProviderId }?.name ?: "None"}")
        state.providers.forEach { provider ->
            Text(provider.name, Modifier.fillMaxWidth().clickable { selectedProviderId = provider.id }, style = MaterialTheme.typography.bodyMedium)
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(onClick = {
            val scheduleTimes = if (scheduleKind == "Daily" || scheduleKind == "Weekly") runCatching { times.split(",").map { LocalTime.parse(it.trim()) } }.getOrNull() else emptyList()
            val days = if (scheduleKind == "Weekly") runCatching { weeklyDays.split(",").map { DayOfWeek.valueOf(it.trim().uppercase()) }.toSet() }.getOrNull() else emptySet()
            val startDate = runCatching { LocalDate.parse(startDateText) }.getOrNull()
            val endDate = endDateText.trim().takeIf { it.isNotEmpty() }?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            if (label.isBlank() || route.isBlank() || form.isBlank() ||
                startDate == null || (endDateText.isNotBlank() && endDate == null) || (endDate != null && endDate.isBefore(startDate)) ||
                ((scheduleKind == "Daily" || scheduleKind == "Weekly") && (scheduleTimes.isNullOrEmpty() || scheduleTimes.distinct().size != scheduleTimes.size)) ||
                (scheduleKind == "Weekly" && days.isNullOrEmpty()) ||
                ((scheduleKind == "As needed" || scheduleKind == "Complex") && instruction.isBlank())
            ) {
                error = "Enter a medication name, route, form, and unique times in HH:mm format."
            } else {
                val existing = editing
                val schedule = when (scheduleKind) {
                    "As needed" -> AsNeededMedicationSchedule(instruction.trim())
                    "Weekly" -> WeeklyMedicationSchedule(requireNotNull(days), requireNotNull(scheduleTimes), instruction.trim())
                    "Complex" -> UnsupportedMedicationSchedule(instruction.trim())
                    else -> DailyMedicationSchedule(requireNotNull(scheduleTimes), instruction.trim())
                }
                val draft = MedicationRegimen(
                    id = existing?.id.orEmpty(), profileId = "pending", label = label.trim(), indication = indication.trim().ifBlank { null }, doseAmount = doseAmount.trim(), doseUnit = doseUnit.trim(),
                    route = route.trim(), form = form.trim(), startDate = requireNotNull(startDate), endDate = endDate, status = status,
                    providerId = selectedProviderId, source = "Manual entry", notes = notes.trim(), schedule = schedule,
                    createdAtEpochMillis = existing?.createdAtEpochMillis ?: 0,
                )
                isSavingMedication = true
                scope.launch {
                    try {
                        if (onSaveMedication(draft)) {
                            editing = null; label = ""; indication = ""; notes = ""; instruction = ""; selectedProviderId = null; status = MedicationStatus.Active; scheduleKind = "Daily"; startDateText = LocalDate.now().toString(); endDateText = ""; error = null
                        } else {
                            error = "Medication could not be saved."
                        }
                    } finally {
                        isSavingMedication = false
                    }
                }
            }
        }, enabled = !isSavingMedication) { Text(if (isSavingMedication) "Saving…" else if (editing == null) "Save medication" else "Save changes") }

        Text("Your medications", style = MaterialTheme.typography.titleLarge)
        if (state.regimens.isEmpty()) Text("No medications added yet.")
        state.regimens.forEach { regimen ->
            Text("${regimen.label} · ${regimen.status} · ${regimen.route}", Modifier.fillMaxWidth().clickable {
                editing = regimen; label = regimen.label; indication = regimen.indication.orEmpty(); notes = regimen.notes; doseAmount = regimen.doseAmount; doseUnit = regimen.doseUnit; route = regimen.route; form = regimen.form; status = regimen.status; startDateText = regimen.startDate.toString(); endDateText = regimen.endDate?.toString().orEmpty()
                when (val schedule = regimen.schedule) {
                    is DailyMedicationSchedule -> { scheduleKind = "Daily"; times = schedule.times.joinToString(",") }
                    is WeeklyMedicationSchedule -> { scheduleKind = "Weekly"; times = schedule.times.joinToString(","); weeklyDays = schedule.days.joinToString(",") }
                    is AsNeededMedicationSchedule -> { scheduleKind = "As needed"; times = "" }
                    is UnsupportedMedicationSchedule -> { scheduleKind = "Complex"; times = "" }
                }; instruction = regimen.schedule.originalInstruction; selectedProviderId = regimen.providerId
            })
        }

        Text("Providers", style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(providerName, { providerName = it }, Modifier.fillMaxWidth(), label = { Text("Provider name") })
        OutlinedTextField(providerSpecialty, { providerSpecialty = it }, Modifier.fillMaxWidth(), label = { Text("Specialty (optional)") })
        OutlinedTextField(providerFacility, { providerFacility = it }, Modifier.fillMaxWidth(), label = { Text("Facility (optional)") })
        OutlinedTextField(providerContact, { providerContact = it }, Modifier.fillMaxWidth(), label = { Text("Contact (optional)") })
        OutlinedButton(onClick = {
            if (providerName.isNotBlank()) {
                val existing = editingProvider
                val draft = Provider(
                    id = existing?.id.orEmpty(), profileId = "pending", name = providerName.trim(), specialty = providerSpecialty.trim(),
                    facility = providerFacility.trim(), contact = providerContact.trim(), createdAtEpochMillis = existing?.createdAtEpochMillis ?: 0,
                )
                isSavingProvider = true
                scope.launch {
                    try {
                        if (onSaveProvider(draft)) {
                            editingProvider = null; providerName = ""; providerSpecialty = ""; providerFacility = ""; providerContact = ""
                        } else {
                            error = "Provider could not be saved."
                        }
                    } finally {
                        isSavingProvider = false
                    }
                }
            }
        }, enabled = !isSavingProvider) { Text(if (isSavingProvider) "Saving…" else if (editingProvider == null) "Save provider" else "Save provider changes") }
        state.providers.forEach { provider ->
            Text(
                "${provider.name}${provider.specialty.takeIf(String::isNotBlank)?.let { specialty -> " · $specialty" }.orEmpty()}",
                Modifier.fillMaxWidth().clickable {
                    editingProvider = provider; providerName = provider.name; providerSpecialty = provider.specialty
                    providerFacility = provider.facility; providerContact = provider.contact
                },
            )
        }
    }
}

package com.smartphoneaichat.presentation.medication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.smartphoneaichat.domain.model.AuthorizedSessionContext
import com.smartphoneaichat.domain.model.MedicationRegimen
import com.smartphoneaichat.domain.model.Provider
import com.smartphoneaichat.domain.repository.MedicationRepository
import com.smartphoneaichat.domain.repository.ProviderRepository
import com.smartphoneaichat.domain.repository.HealthRecordSaveResult
import com.smartphoneaichat.domain.service.MedicationScheduleMaterializer
import com.smartphoneaichat.domain.service.ScheduledMedicationDose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import java.time.Clock
import java.time.LocalDate
import java.util.UUID

data class MedicationUiState(
    val regimens: List<MedicationRegimen> = emptyList(),
    val providers: List<Provider> = emptyList(),
    val today: List<ScheduledMedicationDose> = emptyList(),
    val error: String? = null,
)

class MedicationViewModel(
    private val medications: MedicationRepository,
    private val providers: ProviderRepository,
    private val materializer: MedicationScheduleMaterializer = MedicationScheduleMaterializer(),
    private val clock: Clock = Clock.systemDefaultZone(),
) : ViewModel() {
    private val _state = MutableStateFlow(MedicationUiState())
    val state: StateFlow<MedicationUiState> = _state.asStateFlow()
    private var loadJob: Job? = null
    private var stateGeneration = 0L

    fun refresh(context: AuthorizedSessionContext?) {
        val generation = ++stateGeneration
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val loaded = load(context)
            if (generation == stateGeneration) {
                _state.value = loaded
            }
        }
    }

    suspend fun saveRegimen(context: AuthorizedSessionContext, regimen: MedicationRegimen): Boolean {
        val generation = stateGeneration
        val now = clock.millis()
        val persisted = regimen.copy(
            id = regimen.id.ifBlank { "med-${UUID.randomUUID()}" },
            profileId = context.profileId,
            createdAtEpochMillis = regimen.createdAtEpochMillis.takeIf { it > 0 } ?: now,
            updatedAtEpochMillis = now,
        )
        val result = withContext(Dispatchers.IO) { medications.save(context, persisted) }
        if (result != HealthRecordSaveResult.Saved) {
            if (generation == stateGeneration) {
                _state.value = _state.value.copy(error = "Medication could not be saved. Unlock the vault and try again.")
            }
            return false
        }
        val loaded = load(context)
        if (generation == stateGeneration) {
            _state.value = loaded
        }
        return true
    }

    suspend fun saveProvider(context: AuthorizedSessionContext, provider: Provider): Boolean {
        val generation = stateGeneration
        val now = clock.millis()
        val persisted = provider.copy(
            id = provider.id.ifBlank { "provider-${UUID.randomUUID()}" },
            profileId = context.profileId,
            createdAtEpochMillis = provider.createdAtEpochMillis.takeIf { it > 0 } ?: now,
            updatedAtEpochMillis = now,
        )
        val result = withContext(Dispatchers.IO) { providers.save(context, persisted) }
        if (result != HealthRecordSaveResult.Saved) {
            if (generation == stateGeneration) {
                _state.value = _state.value.copy(error = "Provider could not be saved. Unlock the vault and try again.")
            }
            return false
        }
        val loaded = load(context)
        if (generation == stateGeneration) {
            _state.value = loaded
        }
        return true
    }

    private suspend fun load(context: AuthorizedSessionContext?): MedicationUiState = withContext(Dispatchers.IO) {
        if (context == null) {
            return@withContext MedicationUiState()
        }
        runCatching {
            val regimens = medications.list(context)
            val date = LocalDate.now(clock)
            val zone = clock.zone
            MedicationUiState(
                regimens = regimens,
                providers = providers.list(context),
                today = regimens.flatMap { materializer.materializeToday(it, date, zone) }
                    .sortedBy { it.scheduledAt },
            )
        }.getOrElse { MedicationUiState(error = "Medication records are unavailable.") }
    }
}

class MedicationViewModelFactory(
    private val medications: MedicationRepository,
    private val providers: ProviderRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = MedicationViewModel(medications, providers) as T
}

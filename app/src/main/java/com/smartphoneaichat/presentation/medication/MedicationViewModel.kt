package com.smartphoneaichat.presentation.medication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.smartphoneaichat.domain.model.AuthorizedSessionContext
import com.smartphoneaichat.domain.model.MedicationRegimen
import com.smartphoneaichat.domain.model.Provider
import com.smartphoneaichat.domain.repository.MedicationRepository
import com.smartphoneaichat.domain.repository.ProviderRepository
import com.smartphoneaichat.domain.service.MedicationScheduleMaterializer
import com.smartphoneaichat.domain.service.ScheduledMedicationDose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
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

    fun refresh(context: AuthorizedSessionContext?) {
        if (context == null) {
            _state.value = MedicationUiState()
            return
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
        }.onSuccess { _state.value = it }
            .onFailure { _state.value = _state.value.copy(error = "Medication records are unavailable.") }
    }

    fun saveRegimen(context: AuthorizedSessionContext, regimen: MedicationRegimen) {
        val now = clock.millis()
        val persisted = regimen.copy(
            id = regimen.id.ifBlank { "med-${UUID.randomUUID()}" },
            profileId = context.profileId,
            createdAtEpochMillis = regimen.createdAtEpochMillis.takeIf { it > 0 } ?: now,
            updatedAtEpochMillis = now,
        )
        medications.save(context, persisted)
        refresh(context)
    }

    fun saveProvider(context: AuthorizedSessionContext, provider: Provider) {
        val now = clock.millis()
        val persisted = provider.copy(
            id = provider.id.ifBlank { "provider-${UUID.randomUUID()}" },
            profileId = context.profileId,
            createdAtEpochMillis = provider.createdAtEpochMillis.takeIf { it > 0 } ?: now,
            updatedAtEpochMillis = now,
        )
        providers.save(context, persisted)
        refresh(context)
    }
}

class MedicationViewModelFactory(
    private val medications: MedicationRepository,
    private val providers: ProviderRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = MedicationViewModel(medications, providers) as T
}

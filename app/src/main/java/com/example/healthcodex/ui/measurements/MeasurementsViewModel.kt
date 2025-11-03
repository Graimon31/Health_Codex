// app/src/main/java/com/example/healthcodex/ui/measurements/MeasurementsViewModel.kt
package com.example.healthcodex.ui.measurements

import android.content.Context
import com.example.healthcodex.HealthCodexApp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.healthcodex.data.measurements.MeasurementConfidence
import com.example.healthcodex.data.measurements.MeasurementDetails
import com.example.healthcodex.data.measurements.MeasurementEntry
import com.example.healthcodex.data.measurements.MeasurementFilter
import com.example.healthcodex.data.measurements.MeasurementPeriod
import com.example.healthcodex.data.measurements.MeasurementSource
import com.example.healthcodex.data.measurements.MeasurementSourceFilter
import com.example.healthcodex.data.measurements.MeasurementSummary
import com.example.healthcodex.data.measurements.MeasurementType
import com.example.healthcodex.data.measurements.MeasurementValueRange
import com.example.healthcodex.data.measurements.MeasurementsRepository
import com.example.healthcodex.data.profile.ProfileRepository
import com.example.healthcodex.util.Validation
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel orchestrating the measurements feature state and actions.
 */
class MeasurementsViewModel(
    private val repository: MeasurementsRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val filterState = MutableStateFlow(MeasurementFilter())

    private val _uiState = MutableStateFlow(MeasurementsUiState())
    val uiState: StateFlow<MeasurementsUiState> = _uiState.asStateFlow()

    private val _editorState = MutableStateFlow<MeasurementForm?>(null)
    val editorState: StateFlow<MeasurementForm?> = _editorState.asStateFlow()

    private val _events = MutableSharedFlow<MeasurementsEvent>()
    val events = _events.asSharedFlow()

    init {
        observeMeasurements()
        observeBleStatus()
    }

    private fun observeMeasurements() {
        viewModelScope.launch {
            combine(
                repository.measurements.catch { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.localizedMessage ?: "Не удалось загрузить измерения"
                        )
                    }
                    emit(emptyList())
                },
                filterState
            ) { entries, filter ->
                val filtered = applyFilter(entries, filter)
                val groups = filtered
                    .groupBy { it.localDate() }
                    .toSortedMap(compareByDescending { it })
                    .map { (date, list) -> MeasurementDayGroup(date, list.sortedByDescending { it.timestamp }) }
                val summary = buildSummary(filtered)
                MeasurementCollection(
                    raw = entries,
                    filtered = filtered,
                    groups = groups,
                    summary = summary,
                    filter = filter
                )
            }.catch { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.localizedMessage ?: "Ошибка обработки измерений"
                    )
                }
            }.collect { collection ->
                val devices = collection.raw.mapNotNull { it.deviceName?.takeIf(String::isNotBlank) }
                    .distinct()
                    .sorted()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        groups = collection.groups,
                        summary = collection.summary,
                        filter = collection.filter,
                        availableDevices = devices,
                        filteredEntries = collection.filtered,
                        errorMessage = null
                    )
                }
            }
        }
    }

    private fun observeBleStatus() {
        viewModelScope.launch {
            profileRepository.profileFlow.collect { profile ->
                _uiState.update {
                    it.copy(
                        bleConnected = !profile?.bleDeviceAddress.isNullOrBlank(),
                        bleDeviceName = profile?.bleDeviceName
                    )
                }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            delay(350)
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }

    fun setPeriod(period: MeasurementPeriod) {
        filterState.value = filterState.value.copy(period = period)
    }

    fun setCustomPeriod(start: LocalDate, end: LocalDate) {
        filterState.value = filterState.value.copy(period = MeasurementPeriod.Custom(start, end))
    }

    fun toggleType(type: MeasurementType) {
        val current = filterState.value
        val newSelection = current.selectedTypes.toMutableSet().also { set ->
            if (set.contains(type)) set.remove(type) else set.add(type)
            if (set.isEmpty()) MeasurementType.values().forEach { set.add(it) }
        }
        filterState.value = current.copy(selectedTypes = newSelection)
    }

    fun setSourceFilter(filter: MeasurementSourceFilter) {
        filterState.value = filterState.value.copy(source = filter)
    }

    fun setDeviceFilter(deviceName: String?) {
        filterState.value = filterState.value.copy(deviceName = deviceName)
    }

    fun toggleAnomalies() {
        filterState.value = filterState.value.copy(onlyAnomalies = !filterState.value.onlyAnomalies)
    }

    fun updateQuery(query: String) {
        filterState.value = filterState.value.copy(query = query)
    }

    fun setRange(type: MeasurementType, min: Double?, max: Double?) {
        val newRanges = filterState.value.ranges.toMutableMap()
        if (min == null && max == null) {
            newRanges.remove(type)
        } else {
            newRanges[type] = MeasurementValueRange(min, max)
        }
        filterState.value = filterState.value.copy(ranges = newRanges)
    }

    fun clearFilters() {
        filterState.value = MeasurementFilter()
    }

    fun openEditorForNew(type: MeasurementType) {
        val now = LocalDateTime.now()
        _editorState.value = MeasurementForm(
            type = type,
            date = now.toLocalDate(),
            time = now.toLocalTime()
        )
    }

    fun openEditor(entry: MeasurementEntry) {
        _editorState.value = MeasurementForm.fromEntry(entry)
    }

    fun dismissEditor() {
        _editorState.value = null
    }

    fun updateForm(transform: (MeasurementForm) -> MeasurementForm) {
        _editorState.value = _editorState.value?.let(transform)
    }

    fun saveMeasurement() {
        val form = _editorState.value ?: return
        val entry = try {
            form.toEntry()
        } catch (ex: IllegalArgumentException) {
            viewModelScope.launch { _events.emit(MeasurementsEvent.ShowMessage(ex.message ?: "Некорректные данные")) }
            return
        }
        try {
            Validation.validateMeasurement(entry)
        } catch (ex: IllegalArgumentException) {
            viewModelScope.launch { _events.emit(MeasurementsEvent.ShowMessage(ex.message ?: "Ошибка валидации")) }
            return
        }
        viewModelScope.launch {
            repository.upsert(entry)
            _events.emit(MeasurementsEvent.ShowMessage("Измерение сохранено"))
            _editorState.value = null
        }
    }

    fun delete(entry: MeasurementEntry) {
        viewModelScope.launch {
            repository.delete(entry)
            _events.emit(MeasurementsEvent.ShowMessage("Запись удалена"))
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun applyFilter(entries: List<MeasurementEntry>, filter: MeasurementFilter): List<MeasurementEntry> {
        val (start, end) = filter.period.range()
        return entries.filter { entry ->
            val matchesPeriod = entry.inRange(start, end)
            val matchesType = filter.selectedTypes.contains(entry.type)
            val matchesSource = when (val src = filter.source) {
                MeasurementSourceFilter.All -> true
                is MeasurementSourceFilter.Only -> entry.source == src.source
            }
            val matchesDevice = filter.deviceName.isNullOrBlank() || entry.deviceName == filter.deviceName
            val matchesQuery = filter.query.isBlank() || entry.note?.contains(filter.query, ignoreCase = true) == true || entry.type.name.contains(filter.query, ignoreCase = true)
            val matchesAnomaly = !filter.onlyAnomalies || Validation.isAnomalous(entry)
            val matchesRange = filter.ranges[entry.type]?.let { range ->
                val value = entry.details.primaryValue
                val minOk = range.min?.let { min -> value?.let { it >= min } ?: false } ?: true
                val maxOk = range.max?.let { max -> value?.let { it <= max } ?: false } ?: true
                minOk && maxOk
            } ?: true
            matchesPeriod && matchesType && matchesSource && matchesDevice && matchesQuery && matchesAnomaly && matchesRange
        }
    }

    private fun buildSummary(entries: List<MeasurementEntry>): MeasurementSummary {
        val hrValues = entries.filter { it.type == MeasurementType.HEART_RATE }.mapNotNull { it.details.primaryValue }
        val avgHr = hrValues.takeIf { it.isNotEmpty() }?.average()
        val stepEntries = entries.filter { it.type == MeasurementType.STEPS }
        val steps = stepEntries.takeIf { it.isNotEmpty() }?.sumOf { (it.details.primaryValue ?: 0.0).toLong() }
        val calorieEntries = entries.filter { it.type == MeasurementType.CALORIES }
        val calories = calorieEntries.takeIf { it.isNotEmpty() }?.sumOf { (it.details.primaryValue ?: 0.0).toLong() }
        val pressures = entries.filter { it.type == MeasurementType.BLOOD_PRESSURE }
        val avgSys = pressures.mapNotNull { it.details.primaryValue }.takeIf { it.isNotEmpty() }?.average()
        val avgDia = pressures.mapNotNull { it.details.secondaryValue }.takeIf { it.isNotEmpty() }?.average()
        val weightEntries = entries.filter { it.type == MeasurementType.WEIGHT }.sortedBy { it.timestamp }
        val weightDelta = if (weightEntries.size >= 2) {
            val first = weightEntries.first().details.primaryValue
            val last = weightEntries.last().details.primaryValue
            if (first != null && last != null) last - first else null
        } else null
        val spoValues = entries.filter { it.type == MeasurementType.OXYGEN }.mapNotNull { it.details.primaryValue }
        val avgSpo = spoValues.takeIf { it.isNotEmpty() }?.average()
        val sleepMinutes = entries.filter { it.type == MeasurementType.SLEEP }.sumOf {
            val duration = it.details.durationMinutes
            if (duration != null) duration else {
                val start = it.startTimestamp ?: it.details.startInstant
                if (start != null) {
                    val end = it.details.endInstant ?: it.timestamp
                    val minutes = java.time.Duration.between(start, end).toMinutes().toInt()
                    minutes.coerceAtLeast(0)
                } else 0
            }
        }
        val respiratoryAvg = entries.filter { it.type == MeasurementType.RESPIRATORY }.mapNotNull { it.details.primaryValue }.
            takeIf { it.isNotEmpty() }?.average()

        return MeasurementSummary(
            averageHr = avgHr,
            steps = steps,
            calories = calories,
            pressure = if (avgSys != null && avgDia != null) avgSys to avgDia else null,
            weightDelta = weightDelta,
            averageSpo2 = avgSpo,
            sleepMinutes = sleepMinutes.takeIf { it > 0 },
            respiratoryRate = respiratoryAvg
        )
    }

    data class MeasurementCollection(
        val raw: List<MeasurementEntry>,
        val filtered: List<MeasurementEntry>,
        val groups: List<MeasurementDayGroup>,
        val summary: MeasurementSummary,
        val filter: MeasurementFilter
    )

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory {
            val appContext = context.applicationContext ?: context
            val app = appContext as? HealthCodexApp
            val measurementsRepository = app?.measurementsRepository ?: MeasurementsRepository(appContext)
            val profileRepository = app?.profileRepository ?: ProfileRepository(appContext)
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(MeasurementsViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return MeasurementsViewModel(measurementsRepository, profileRepository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class: ${'$'}modelClass")
                }
            }
        }
    }
}

/** UI state consumed by the measurements composables. */
data class MeasurementsUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val groups: List<MeasurementDayGroup> = emptyList(),
    val summary: MeasurementSummary = MeasurementSummary(null, null, null, null, null, null, null, null),
    val filter: MeasurementFilter = MeasurementFilter(),
    val bleConnected: Boolean = false,
    val bleDeviceName: String? = null,
    val availableDevices: List<String> = emptyList(),
    val filteredEntries: List<MeasurementEntry> = emptyList(),
    val errorMessage: String? = null
)

/** Group of measurements for a specific day. */
data class MeasurementDayGroup(
    val date: LocalDate,
    val entries: List<MeasurementEntry>
)

/** Dialog form state used for create/edit flows. */
data class MeasurementForm(
    val id: Long = 0,
    val type: MeasurementType = MeasurementType.HEART_RATE,
    val date: LocalDate = LocalDate.now(),
    val time: LocalTime = LocalTime.now(),
    val startDate: LocalDate? = null,
    val startTime: LocalTime? = null,
    val endDate: LocalDate? = null,
    val endTime: LocalTime? = null,
    val primary: String = "",
    val secondary: String = "",
    val tertiary: String = "",
    val durationMinutes: String = "",
    val statusText: String = "",
    val source: MeasurementSource = MeasurementSource.DEVICE,
    val confidence: MeasurementConfidence = MeasurementConfidence.HIGH,
    val deviceName: String = "",
    val deviceAddress: String = "",
    val note: String = "",
    val tags: String = ""
) {
    fun toEntry(): MeasurementEntry {
        val zone = ZoneId.systemDefault()
        val localDateTime = LocalDateTime.of(date, time)
        val timestamp = localDateTime.atZone(zone).toInstant()
        val startInstant = when {
            startDate != null && startTime != null -> LocalDateTime.of(startDate, startTime).atZone(zone).toInstant()
            else -> null
        }
        val endInstant = when {
            endDate != null && endTime != null -> LocalDateTime.of(endDate, endTime).atZone(zone).toInstant()
            else -> null
        }
        val primaryValue = primary.toDoubleOrNull()
        val secondaryValue = secondary.toDoubleOrNull()
        val tertiaryValue = tertiary.toDoubleOrNull()
        val duration = durationMinutes.toIntOrNull()
        val normalizedTags = tags.split(',').mapNotNull { token ->
            token.trim().takeIf { it.isNotEmpty() }
        }
        val details = MeasurementDetails(
            primaryValue = primaryValue,
            secondaryValue = secondaryValue,
            tertiaryValue = tertiaryValue,
            durationMinutes = duration,
            statusText = statusText.takeIf { it.isNotBlank() },
            startInstant = startInstant,
            endInstant = endInstant
        )
        return MeasurementEntry(
            id = id,
            type = type,
            timestamp = timestamp,
            startTimestamp = startInstant,
            source = source,
            deviceName = deviceName.ifBlank { null },
            deviceAddress = deviceAddress.ifBlank { null },
            note = note.ifBlank { null },
            confidence = confidence,
            details = details,
            tags = normalizedTags
        )
    }

    companion object {
        fun fromEntry(entry: MeasurementEntry): MeasurementForm {
            val zone = ZoneId.systemDefault()
            val localDateTime = LocalDateTime.ofInstant(entry.timestamp, zone)
            val start = entry.startTimestamp ?: entry.details.startInstant
            val end = entry.details.endInstant
            return MeasurementForm(
                id = entry.id,
                type = entry.type,
                date = localDateTime.toLocalDate(),
                time = localDateTime.toLocalTime(),
                startDate = start?.atZone(zone)?.toLocalDate(),
                startTime = start?.atZone(zone)?.toLocalTime(),
                endDate = end?.atZone(zone)?.toLocalDate(),
                endTime = end?.atZone(zone)?.toLocalTime(),
                primary = entry.details.primaryValue?.toString() ?: "",
                secondary = entry.details.secondaryValue?.toString() ?: "",
                tertiary = entry.details.tertiaryValue?.toString() ?: "",
                durationMinutes = entry.details.durationMinutes?.toString() ?: "",
                statusText = entry.details.statusText.orEmpty(),
                source = entry.source,
                confidence = entry.confidence,
                deviceName = entry.deviceName.orEmpty(),
                deviceAddress = entry.deviceAddress.orEmpty(),
                note = entry.note.orEmpty(),
                tags = entry.tags.joinToString(separator = ", ")
            )
        }
    }
}

/** One-off events surfaced to the UI layer. */
sealed class MeasurementsEvent {
    data class ShowMessage(val message: String) : MeasurementsEvent()
}

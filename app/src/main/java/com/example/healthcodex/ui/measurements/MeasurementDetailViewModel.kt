// app/src/main/java/com/example/healthcodex/ui/measurements/MeasurementDetailViewModel.kt
package com.example.healthcodex.ui.measurements

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.healthcodex.HealthCodexApp
import com.example.healthcodex.data.measurements.MeasurementEntry
import com.example.healthcodex.data.measurements.MeasurementsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel powering the measurement detail screen.
 */
class MeasurementDetailViewModel(
    application: Application,
    private val measurementId: Long
) : AndroidViewModel(application) {

    private val repository = (application as? HealthCodexApp)?.measurementsRepository
        ?: MeasurementsRepository(application.applicationContext)

    private val _state = MutableStateFlow(MeasurementDetailState())
    val state: StateFlow<MeasurementDetailState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.measurements.collect { entries ->
                val current = entries.find { it.id == measurementId }
                if (current != null) {
                    val dayEntries = entries.filter { it.localDate() == current.localDate() }
                        .sortedByDescending { it.timestamp }
                    _state.value = MeasurementDetailState(
                        isLoading = false,
                        entry = current,
                        dayEntries = dayEntries
                    )
                } else {
                    _state.value = MeasurementDetailState(isLoading = false, entry = null, dayEntries = emptyList())
                }
            }
        }
    }

    companion object {
        fun factory(application: Application, measurementId: Long): ViewModelProvider.Factory =
            object : ViewModelProvider.AndroidViewModelFactory(application) {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(MeasurementDetailViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return MeasurementDetailViewModel(application, measurementId) as T
                    }
                    return super.create(modelClass)
                }
            }
    }
}

/** Detail screen state container. */
data class MeasurementDetailState(
    val isLoading: Boolean = true,
    val entry: MeasurementEntry? = null,
    val dayEntries: List<MeasurementEntry> = emptyList()
)

// app/src/main/java/com/example/healthcodex/feature/forecast/ForecastViewModel.kt
package com.example.healthcodex.feature.forecast

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.healthcodex.HealthCodexApp
import com.example.healthcodex.data.profile.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * ViewModel that exposes the current health forecast for the user.
 */
class ForecastViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as? HealthCodexApp)?.profileRepository
        ?: ProfileRepository(application.applicationContext)

    private val _state = MutableStateFlow(ForecastViewState())
    val state: StateFlow<ForecastViewState> = _state.asStateFlow()

    init {
        observeForecast()
    }

    private fun observeForecast() {
        viewModelScope.launch {
            repository.profileFlow.collectLatest { profile ->
                _state.value = ForecastViewState(
                    isLoading = false,
                    forecast = ForecastAnalyzer.analyze(profile, getApplication())
                )
            }
        }
    }

    companion object {
        fun factory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.AndroidViewModelFactory(application) {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(ForecastViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return ForecastViewModel(application) as T
                    }
                    return super.create(modelClass)
                }
            }
    }
}

/** UI state of the forecast screen. */
data class ForecastViewState(
    val isLoading: Boolean = true,
    val forecast: HealthForecast = HealthForecast()
)

// app/src/main/java/com/example/healthcodex/ui/measurements/DeviceConnectViewModel.kt
package com.example.healthcodex.ui.measurements

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.healthcodex.HealthCodexApp
import com.example.healthcodex.data.measurements.ConnectedDevice
import com.example.healthcodex.data.measurements.DeviceConnectionStatus
import com.example.healthcodex.data.measurements.MeasurementDeviceType
import com.example.healthcodex.data.measurements.MeasurementsRepository
import com.example.healthcodex.data.profile.ProfileRepository
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for the device connection screen.
 */
data class DeviceConnectUiState(
    val connected: List<ConnectedDevice> = emptyList(),
    val discovered: List<DiscoveredDevice> = emptyList(),
    val isScanning: Boolean = false,
    val isProcessing: Boolean = false
)

/**
 * Model describing a discovered Bluetooth device that can be paired.
 */
data class DiscoveredDevice(
    val name: String,
    val address: String?,
    val type: MeasurementDeviceType
)

/**
 * One-off events emitted by [DeviceConnectViewModel].
 */
sealed interface DeviceConnectEvent {
    data class ShowMessage(val message: String) : DeviceConnectEvent
    data class DeviceLinked(val name: String) : DeviceConnectEvent
}

/**
 * ViewModel responsible for pairing Bluetooth devices with the app.
 */
class DeviceConnectViewModel(
    private val measurementsRepository: MeasurementsRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DeviceConnectUiState())
    val uiState: StateFlow<DeviceConnectUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<DeviceConnectEvent>()
    val events = _events.asSharedFlow()

    init {
        observeConnectedDevices()
        observeDemoMode()
    }

    private fun observeDemoMode() {
        viewModelScope.launch {
            profileRepository.demoMode.collectLatest { demoEnabled ->
                _uiState.update { it.copy(discovered = if (demoEnabled) demoDevices() else emptyList()) }
            }
        }
    }

    private fun observeConnectedDevices() {
        viewModelScope.launch {
            measurementsRepository.connectedDevices.collectLatest { devices ->
                _uiState.update { current ->
                    current.copy(connected = devices.sortedBy { it.name.lowercase() })
                }
            }
        }
    }

    fun refreshDiscovery() {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true) }
            delay(1200)
            val demoEnabled = profileRepository.demoMode.first()
            _uiState.update { it.copy(isScanning = false, discovered = if (demoEnabled) demoDevices() else emptyList()) }
        }
    }

    fun connect(device: DiscoveredDevice) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isProcessing = true) }
                val existing = _uiState.value.connected.firstOrNull { candidate ->
                    candidate.address == device.address || candidate.name == device.name
                }
                val entity = ConnectedDevice(
                    id = existing?.id ?: 0,
                    name = device.name,
                    address = device.address,
                    type = device.type,
                    status = DeviceConnectionStatus.CONNECTED,
                    lastSync = Instant.now()
                )
                measurementsRepository.upsertDevice(entity)
                profileRepository.linkBleDevice(device.name, device.address)
                _events.emit(DeviceConnectEvent.DeviceLinked(device.name))
            } catch (throwable: Throwable) {
                _events.emit(
                    DeviceConnectEvent.ShowMessage(
                        throwable.localizedMessage ?: "Не удалось подключить устройство"
                    )
                )
            } finally {
                _uiState.update { it.copy(isProcessing = false) }
            }
        }
    }

    fun activate(device: ConnectedDevice) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isProcessing = true) }
                measurementsRepository.updateDeviceStatus(
                    device.id,
                    DeviceConnectionStatus.CONNECTED,
                    Instant.now()
                )
                profileRepository.linkBleDevice(device.name, device.address)
                _events.emit(DeviceConnectEvent.DeviceLinked(device.name))
            } catch (throwable: Throwable) {
                _events.emit(
                    DeviceConnectEvent.ShowMessage(
                        throwable.localizedMessage ?: "Не удалось активировать устройство"
                    )
                )
            } finally {
                _uiState.update { it.copy(isProcessing = false) }
            }
        }
    }

    fun forget(device: ConnectedDevice) {
        viewModelScope.launch {
            try {
                measurementsRepository.deleteDevice(device)
                _events.emit(
                    DeviceConnectEvent.ShowMessage(
                        "Устройство \"${device.name}\" удалено из списка"
                    )
                )
            } catch (throwable: Throwable) {
                _events.emit(
                    DeviceConnectEvent.ShowMessage(
                        throwable.localizedMessage ?: "Не удалось удалить устройство"
                    )
                )
            }
        }
    }

    fun addManual(name: String, address: String?, type: MeasurementDeviceType) {
        val normalizedName = name.ifBlank { "Устройство ${UUID.randomUUID().toString().take(4)}" }
        val manualDevice = DiscoveredDevice(
            name = normalizedName,
            address = address.takeUnless { it.isNullOrBlank() },
            type = type
        )
        connect(manualDevice)
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val appContext = context.applicationContext ?: context
                val app = appContext as? HealthCodexApp
                val measurementsRepository = app?.measurementsRepository ?: MeasurementsRepository(appContext)
                val profileRepository = app?.profileRepository ?: ProfileRepository(appContext)
                @Suppress("UNCHECKED_CAST")
                return DeviceConnectViewModel(measurementsRepository, profileRepository) as T
            }
        }

        private fun demoDevices(): List<DiscoveredDevice> = listOf(
            DiscoveredDevice(name = "HealthBand 7", address = "AA:BB:01:02:03:04", type = MeasurementDeviceType.WEARABLE),
            DiscoveredDevice(name = "CardioTrack Pro", address = "AA:BB:05:06:07:08", type = MeasurementDeviceType.WEARABLE),
            DiscoveredDevice(name = "Smart Scale X", address = "C1:D2:E3:F4:55:66", type = MeasurementDeviceType.NON_WEARABLE)
        )
    }
}

// app/src/main/java/com/example/healthcodex/data/db/ConnectedDeviceEntity.kt
package com.example.healthcodex.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.healthcodex.data.measurements.DeviceConnectionStatus
import com.example.healthcodex.data.measurements.MeasurementDeviceType
import java.time.Instant

/**
 * Entity storing known Bluetooth devices and their last connection state.
 */
@Entity(tableName = "connected_devices")
data class ConnectedDeviceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val address: String? = null,
    val type: MeasurementDeviceType = MeasurementDeviceType.WEARABLE,
    val status: DeviceConnectionStatus = DeviceConnectionStatus.INACTIVE,
    val lastSync: Instant? = null
)

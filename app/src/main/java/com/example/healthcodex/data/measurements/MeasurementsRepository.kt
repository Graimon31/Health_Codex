// app/src/main/java/com/example/healthcodex/data/measurements/MeasurementsRepository.kt
package com.example.healthcodex.data.measurements

import android.content.Context
import com.example.healthcodex.data.db.AppDatabase
import com.example.healthcodex.data.db.ConnectedDeviceDao
import com.example.healthcodex.data.db.ConnectedDeviceEntity
import com.example.healthcodex.data.db.MeasurementDao
import com.example.healthcodex.data.db.MeasurementEntity
import com.example.healthcodex.data.measurements.DeviceConnectionStatus
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Instant

/**
 * Repository orchestrating measurement persistence and mapping.
 */
class MeasurementsRepository(
    private val dao: MeasurementDao,
    private val connectedDeviceDao: ConnectedDeviceDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    constructor(context: Context, ioDispatcher: CoroutineDispatcher = Dispatchers.IO) : this(
        AppDatabase.getInstance(context).measurementDao(),
        AppDatabase.getInstance(context).connectedDeviceDao(),
        ioDispatcher
    )

    val measurements: Flow<List<MeasurementEntry>> =
        dao.observeMeasurements().map { list -> list.map { it.toDomain() } }.flowOn(ioDispatcher)

    val connectedDevices: Flow<List<ConnectedDevice>> =
        connectedDeviceDao.observeDevices().map { entities -> entities.map { it.toDomain() } }.flowOn(ioDispatcher)

    suspend fun getMeasurement(id: Long): MeasurementEntry? = withContext(ioDispatcher) {
        dao.getMeasurement(id)?.toDomain()
    }

    suspend fun upsert(entry: MeasurementEntry) = withContext(ioDispatcher) {
        if (entry.id == 0L) {
            dao.insert(entry.toEntity())
        } else {
            dao.update(entry.toEntity())
        }
    }

    suspend fun delete(entry: MeasurementEntry) = withContext(ioDispatcher) {
        dao.delete(entry.toEntity())
    }

    suspend fun upsertDevice(device: ConnectedDevice) = withContext(ioDispatcher) {
        connectedDeviceDao.upsert(device.toEntity())
    }

    suspend fun updateDeviceStatus(id: Long, status: DeviceConnectionStatus, lastSync: Instant?) =
        withContext(ioDispatcher) {
            connectedDeviceDao.updateStatus(id, status, lastSync)
        }

    suspend fun deleteDevice(device: ConnectedDevice) = withContext(ioDispatcher) {
        connectedDeviceDao.delete(device.toEntity())
    }

    private fun MeasurementEntity.toDomain(): MeasurementEntry = MeasurementEntry(
        id = id,
        type = type,
        timestamp = timestamp,
        startTimestamp = startTimestamp,
        source = source,
        deviceId = deviceId,
        deviceType = deviceType,
        deviceName = deviceName,
        deviceAddress = deviceAddress,
        note = note,
        confidence = confidence,
        details = details,
        tags = tags
    )

    private fun MeasurementEntry.toEntity(): MeasurementEntity = MeasurementEntity(
        id = id,
        type = type,
        timestamp = timestamp,
        startTimestamp = startTimestamp,
        source = source,
        deviceId = deviceId,
        deviceType = deviceType,
        deviceName = deviceName,
        deviceAddress = deviceAddress,
        note = note,
        confidence = confidence,
        details = details,
        tags = tags
    )

    private fun ConnectedDeviceEntity.toDomain(): ConnectedDevice = ConnectedDevice(
        id = id,
        name = name,
        address = address,
        type = type,
        status = status,
        lastSync = lastSync
    )

    private fun ConnectedDevice.toEntity(): ConnectedDeviceEntity = ConnectedDeviceEntity(
        id = id,
        name = name,
        address = address,
        type = type,
        status = status,
        lastSync = lastSync
    )
}

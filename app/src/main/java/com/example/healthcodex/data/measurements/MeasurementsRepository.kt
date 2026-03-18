// app/src/main/java/com/example/healthcodex/data/measurements/MeasurementsRepository.kt
package com.example.healthcodex.data.measurements

import android.content.Context
import android.util.Log
import com.example.healthcodex.data.db.AppDatabase
import com.example.healthcodex.data.db.ConnectedDeviceDao
import com.example.healthcodex.data.db.ConnectedDeviceEntity
import com.example.healthcodex.data.db.MeasurementDao
import com.example.healthcodex.data.db.MeasurementEntity
import com.example.healthcodex.data.measurements.DeviceConnectionStatus
import com.example.healthcodex.data.network.ApiClient
import com.example.healthcodex.data.network.MeasurementRequest
import com.example.healthcodex.data.prefs.PrefsRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Repository orchestrating measurement persistence and mapping.
 */
class MeasurementsRepository(
    private val dao: MeasurementDao,
    private val connectedDeviceDao: ConnectedDeviceDao,
    private val apiClient: ApiClient? = null,
    private val prefs: PrefsRepository? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    constructor(context: Context, ioDispatcher: CoroutineDispatcher = Dispatchers.IO) : this(
        AppDatabase.getInstance(context).measurementDao(),
        AppDatabase.getInstance(context).connectedDeviceDao(),
        null,
        null,
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
        // Fire-and-forget sync to server
        if (apiClient != null && prefs != null) {
            try {
                pushToServer(entry)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to sync measurement to server: ${e.message}")
            }
        }
    }

    private suspend fun pushToServer(entry: MeasurementEntry) {
        val url = prefs?.baseUrl?.first() ?: return
        val token = prefs.authToken.first() ?: return
        if (url.isBlank() || token.isBlank()) return

        val api = apiClient?.measurementsApi(url) ?: return
        val request = MeasurementRequest(
            type = mapTypeToServer(entry.type),
            valuePrimary = entry.details.primaryValue ?: 0.0,
            valueSecondary = entry.details.secondaryValue,
            unit = mapTypeToUnit(entry.type),
            measuredAt = DateTimeFormatter.ISO_INSTANT.format(entry.timestamp),
            source = entry.source.name,
            deviceName = entry.deviceName,
            note = entry.note
        )
        val response = api.createMeasurement(request)
        if (response.isSuccessful) {
            Log.d(TAG, "Measurement synced to server: ${response.body()?.id}")
        } else {
            Log.w(TAG, "Server rejected measurement: ${response.code()}")
        }
    }

    private fun mapTypeToServer(type: MeasurementType): String = when (type) {
        MeasurementType.HEART_RATE -> "PULSE"
        MeasurementType.BLOOD_PRESSURE -> "BLOOD_PRESSURE"
        MeasurementType.WEIGHT -> "WEIGHT"
        MeasurementType.OXYGEN -> "SPO2"
        MeasurementType.STEPS -> "STEPS"
        MeasurementType.CALORIES -> "CALORIES"
        MeasurementType.SLEEP -> "SLEEP"
        MeasurementType.RESPIRATORY -> "RESPIRATORY"
    }

    private fun mapTypeToUnit(type: MeasurementType): String = when (type) {
        MeasurementType.HEART_RATE -> "bpm"
        MeasurementType.BLOOD_PRESSURE -> "mmHg"
        MeasurementType.WEIGHT -> "kg"
        MeasurementType.OXYGEN -> "%"
        MeasurementType.STEPS -> "steps"
        MeasurementType.CALORIES -> "kcal"
        MeasurementType.SLEEP -> "hours"
        MeasurementType.RESPIRATORY -> "breaths/min"
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

    /**
     * Inserts synthetic demo measurements if none exist yet.
     * All generated entries carry the "demo" tag so they can be removed cleanly.
     */
    suspend fun seedDemoData() = withContext(ioDispatcher) {
        val existing = dao.getDemoMeasurements()
        if (existing.isEmpty()) {
            val entities = DemoDataGenerator.generate().map { it.toEntity() }
            dao.insertAll(entities)
        }
    }

    /** Removes all measurements carrying the "demo" tag. */
    suspend fun clearDemoData() = withContext(ioDispatcher) {
        dao.deleteDemoMeasurements()
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

    companion object {
        private const val TAG = "MeasurementsRepository"
    }
}

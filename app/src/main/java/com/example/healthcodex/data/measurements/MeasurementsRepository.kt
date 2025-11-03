// app/src/main/java/com/example/healthcodex/data/measurements/MeasurementsRepository.kt
package com.example.healthcodex.data.measurements

import android.content.Context
import com.example.healthcodex.data.db.AppDatabase
import com.example.healthcodex.data.db.MeasurementDao
import com.example.healthcodex.data.db.MeasurementEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Repository orchestrating measurement persistence and mapping.
 */
class MeasurementsRepository(
    private val dao: MeasurementDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    constructor(context: Context, ioDispatcher: CoroutineDispatcher = Dispatchers.IO) : this(
        AppDatabase.getInstance(context).measurementDao(),
        ioDispatcher
    )

    val measurements: Flow<List<MeasurementEntry>> =
        dao.observeMeasurements().map { list -> list.map { it.toDomain() } }.flowOn(ioDispatcher)

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

    private fun MeasurementEntity.toDomain(): MeasurementEntry = MeasurementEntry(
        id = id,
        type = type,
        timestamp = timestamp,
        startTimestamp = startTimestamp,
        source = source,
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
        deviceName = deviceName,
        deviceAddress = deviceAddress,
        note = note,
        confidence = confidence,
        details = details,
        tags = tags
    )
}

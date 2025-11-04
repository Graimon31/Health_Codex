// app/src/main/java/com/example/healthcodex/data/db/ConnectedDeviceDao.kt
package com.example.healthcodex.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.healthcodex.data.measurements.DeviceConnectionStatus
import kotlinx.coroutines.flow.Flow
import java.time.Instant

/**
 * DAO exposing connected Bluetooth device operations.
 */
@Dao
interface ConnectedDeviceDao {
    @Query("SELECT * FROM connected_devices ORDER BY name ASC")
    fun observeDevices(): Flow<List<ConnectedDeviceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ConnectedDeviceEntity): Long

    @Update
    suspend fun update(entity: ConnectedDeviceEntity)

    @Delete
    suspend fun delete(entity: ConnectedDeviceEntity)

    @Query("UPDATE connected_devices SET status = :status, lastSync = :lastSync WHERE id = :id")
    suspend fun updateStatus(id: Long, status: DeviceConnectionStatus, lastSync: Instant?)
}

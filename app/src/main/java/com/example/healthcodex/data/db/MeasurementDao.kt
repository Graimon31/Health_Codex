// app/src/main/java/com/example/healthcodex/data/db/MeasurementDao.kt
package com.example.healthcodex.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data access object exposing measurement persistence operations.
 */
@Dao
interface MeasurementDao {
    @Query("SELECT * FROM measurements ORDER BY timestamp DESC")
    fun observeMeasurements(): Flow<List<MeasurementEntity>>

    @Query("SELECT * FROM measurements WHERE id = :id")
    suspend fun getMeasurement(id: Long): MeasurementEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: MeasurementEntity): Long

    @Update
    suspend fun update(entity: MeasurementEntity)

    @Delete
    suspend fun delete(entity: MeasurementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<MeasurementEntity>)

    @Query("SELECT * FROM measurements WHERE tags LIKE '%\"demo\"%'")
    suspend fun getDemoMeasurements(): List<MeasurementEntity>

    @Query("DELETE FROM measurements WHERE tags LIKE '%\"demo\"%'")
    suspend fun deleteDemoMeasurements()
}

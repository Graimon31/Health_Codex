// app/src/main/java/com/example/healthcodex/data/db/AppDatabase.kt
package com.example.healthcodex.data.db

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.healthcodex.data.measurements.MeasurementConverters
import com.example.healthcodex.data.profile.UserProfileConverters

@Database(
    entities = [UserProfileEntity::class, MeasurementEntity::class, ConnectedDeviceEntity::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(
    CommonConverters::class,
    UserProfileConverters::class,
    MeasurementConverters::class
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): com.example.healthcodex.data.db.UserProfileDao
    abstract fun measurementDao(): com.example.healthcodex.data.db.MeasurementDao
    abstract fun connectedDeviceDao(): com.example.healthcodex.data.db.ConnectedDeviceDao

    companion object {
        private const val DATABASE_NAME = "health_codex.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            val appContext = context.applicationContext
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(appContext).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            val builder = {
                Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
            }
            return runCatching(builder).getOrElse { throwable ->
                Log.w(
                    "AppDatabase",
                    "Recreating corrupted database: ${throwable.message}"
                )
                context.deleteDatabase(DATABASE_NAME)
                builder()
            }
        }
    }
}

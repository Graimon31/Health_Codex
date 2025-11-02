// app/src/main/java/com/example/healthcodex/data/db/AppDatabase.kt
package com.example.healthcodex.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.healthcodex.data.measurements.MeasurementConverters
import com.example.healthcodex.data.profile.UserProfileConverters

@Database(
    entities = [UserProfileEntity::class, MeasurementEntity::class],
    version = 3,
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

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "health_codex.db"
                ).fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}

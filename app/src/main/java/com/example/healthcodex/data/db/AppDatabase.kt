// app/src/main/java/com/example/healthcodex/data/db/AppDatabase.kt
package com.example.healthcodex.data.db

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
                    .addMigrations(MIGRATION_3_4)
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

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                addColumnIfMissing(db, table = "measurements", column = "deviceId", sql = "ALTER TABLE measurements ADD COLUMN deviceId INTEGER")
                addColumnIfMissing(db, table = "measurements", column = "deviceType", sql = "ALTER TABLE measurements ADD COLUMN deviceType TEXT")
                addColumnIfMissing(db, table = "measurements", column = "deviceName", sql = "ALTER TABLE measurements ADD COLUMN deviceName TEXT")
                addColumnIfMissing(db, table = "measurements", column = "deviceAddress", sql = "ALTER TABLE measurements ADD COLUMN deviceAddress TEXT")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `connected_devices` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `address` TEXT,
                        `type` TEXT NOT NULL DEFAULT 'WEARABLE',
                        `status` TEXT NOT NULL DEFAULT 'INACTIVE',
                        `lastSync` TEXT
                    )
                    """.trimIndent()
                )
            }
        }

        private fun addColumnIfMissing(
            db: SupportSQLiteDatabase,
            table: String,
            column: String,
            sql: String
        ) {
            if (!db.hasColumn(table, column)) {
                db.execSQL(sql)
            }
        }

        private fun SupportSQLiteDatabase.hasColumn(table: String, column: String): Boolean {
            query("PRAGMA table_info(`$table`)").use { cursor ->
                val nameIndex = cursor.getColumnIndex("name")
                if (nameIndex == -1) {
                    return false
                }
                while (cursor.moveToNext()) {
                    if (cursor.getString(nameIndex) == column) {
                        return true
                    }
                }
            }
            return false
        }
    }
}

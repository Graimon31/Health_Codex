// app/src/main/java/com/example/healthcodex/data/db/UserProfileDao.kt
package com.example.healthcodex.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile LIMIT 1")
    suspend fun getProfile(): UserProfileEntity?

    @Query("SELECT * FROM user_profile LIMIT 1")
    fun observeProfile(): Flow<UserProfileEntity?>

    @Upsert
    suspend fun upsertProfile(profile: UserProfileEntity)
}

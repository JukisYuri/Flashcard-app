package com.example.mindcard.data.local.dao

import androidx.room.*
import com.example.mindcard.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles WHERE userId = :userId")
    fun getUserProfile(userId: String): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profiles WHERE userId = :userId")
    suspend fun getUserProfileSync(userId: String): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfileEntity)

    @Update
    suspend fun updateUserProfile(profile: UserProfileEntity)

    @Delete
    suspend fun deleteUserProfile(profile: UserProfileEntity)

    @Query("SELECT * FROM user_profiles WHERE lastModified > :timestamp")
    suspend fun getProfilesModifiedAfter(timestamp: Long): List<UserProfileEntity>
}

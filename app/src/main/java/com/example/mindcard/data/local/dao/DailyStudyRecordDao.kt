package com.example.mindcard.data.local.dao

import androidx.room.*
import com.example.mindcard.data.local.entity.DailyStudyRecordEntity

@Dao
interface DailyStudyRecordDao {
    @Query("SELECT * FROM daily_study_records WHERE userId = :userId AND date = :date")
    suspend fun getRecord(userId: String, date: String): DailyStudyRecordEntity?

    @Query("SELECT * FROM daily_study_records WHERE userId = :userId")
    suspend fun getAllRecordsSync(userId: String): List<DailyStudyRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: DailyStudyRecordEntity)

    @Query("DELETE FROM daily_study_records WHERE userId = :userId AND date = :date")
    suspend fun deleteRecord(userId: String, date: String)
}

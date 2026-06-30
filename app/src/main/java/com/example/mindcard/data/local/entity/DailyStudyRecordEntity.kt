package com.example.mindcard.data.local.entity

import androidx.room.Entity
import com.example.mindcard.data.DailyStudyRecord

@Entity(tableName = "daily_study_records", primaryKeys = ["userId", "date"])
data class DailyStudyRecordEntity(
    val userId: String,
    val date: String,
    val dueCards: Int,
    val wordsLearned: Int,
    val xpEarned: Int,
    val timeSpentMin: Int,
    val mastered: Int,
    val lastModified: Long = System.currentTimeMillis()
) {
    fun toDailyStudyRecord(): DailyStudyRecord {
        return DailyStudyRecord(
            userId = userId,
            date = date,
            dueCards = dueCards,
            wordsLearned = wordsLearned,
            xpEarned = xpEarned,
            timeSpentMin = timeSpentMin,
            mastered = mastered
        )
    }

    companion object {
        fun fromDailyStudyRecord(record: DailyStudyRecord, userId: String, date: String): DailyStudyRecordEntity {
            return DailyStudyRecordEntity(
                userId = userId,
                date = date,
                dueCards = record.dueCards,
                wordsLearned = record.wordsLearned,
                xpEarned = record.xpEarned,
                timeSpentMin = record.timeSpentMin,
                mastered = record.mastered
            )
        }
    }
}

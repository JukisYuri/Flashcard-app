package com.example.mindcard.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.mindcard.data.UserProfile

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val userId: String,
    val name: String,
    val title: String,
    val level: Int,
    val currentStreak: Int,
    val bestStreak: Int,
    val totalXp: Int,
    val totalWordsLearned: Int,
    val studyHistory: String, // JSON string
    // Sync fields
    val lastModified: Long = System.currentTimeMillis()
) {
    fun toUserProfile(): UserProfile {
        return UserProfile(
            name = name,
            title = title,
            level = level,
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            totalXp = totalXp,
            totalWordsLearned = totalWordsLearned,
            studyHistory = try {
                val map = mutableMapOf<String, Boolean>()
                val pairs = studyHistory.split(",")
                for (pair in pairs) {
                    val kv = pair.split(":")
                    if (kv.size == 2) {
                        map[kv[0]] = kv[1].toBoolean()
                    }
                }
                map
            } catch (e: Exception) {
                emptyMap()
            }
        )
    }

    companion object {
        fun fromUserProfile(profile: UserProfile, userId: String): UserProfileEntity {
            val studyHistoryStr = profile.studyHistory.entries.joinToString(",") { "${it.key}:${it.value}" }
            return UserProfileEntity(
                userId = userId,
                name = profile.name,
                title = profile.title,
                level = profile.level,
                currentStreak = profile.currentStreak,
                bestStreak = profile.bestStreak,
                totalXp = profile.totalXp,
                totalWordsLearned = profile.totalWordsLearned,
                studyHistory = studyHistoryStr
            )
        }
    }
}

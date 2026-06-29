package com.example.mindcard.data.repository

import androidx.compose.runtime.State
import com.example.mindcard.data.Database
import com.example.mindcard.data.UserProfile

class UserRepository {
    val userProfile: State<UserProfile> = Database.userProfile

    fun updateUserProfile(profile: UserProfile) {
        Database.updateUserProfile(profile)
    }

    fun updateProfileName(newName: String) {
        Database.updateProfileName(newName)
    }

    fun restoreStreak() {
        val currentProfile = Database.userProfile.value
        Database.updateUserProfile(currentProfile.copy(currentStreak = 1))
    }

    fun markTodayAsActive() {
        Database.markTodayAsActive()
    }
}

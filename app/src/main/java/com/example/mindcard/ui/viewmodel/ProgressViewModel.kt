package com.example.mindcard.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.mindcard.data.UserProfile
import com.example.mindcard.data.repository.UserRepository

class ProgressViewModel : ViewModel() {
    private val userRepository = UserRepository()

    val userProfile: State<UserProfile> = userRepository.userProfile
    var streakRestoreActive by mutableStateOf(true)

    fun restoreStreak() {
        userRepository.restoreStreak()
        streakRestoreActive = false
    }
}

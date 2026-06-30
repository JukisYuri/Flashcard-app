package com.example.mindcard.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.mindcard.data.UserProfile
import com.example.mindcard.data.repository.UserRepository
import java.util.Calendar

class ProgressViewModel : ViewModel() {
    private val userRepository = UserRepository()

    val userProfile: State<UserProfile> = userRepository.userProfile
    var streakRestoreActive by mutableStateOf(true)

    var selectedYear by mutableStateOf(Calendar.getInstance().get(Calendar.YEAR))
    var selectedMonth by mutableStateOf(Calendar.getInstance().get(Calendar.MONTH))

    fun restoreStreak() {
        userRepository.restoreStreak()
        streakRestoreActive = false
    }

    fun goToPreviousMonth() {
        if (selectedMonth == 0) {
            selectedMonth = 11
            selectedYear--
        } else {
            selectedMonth--
        }
    }

    fun goToNextMonth() {
        val cal = Calendar.getInstance()
        val currentYear = cal.get(Calendar.YEAR)
        val currentMonth = cal.get(Calendar.MONTH)
        if (selectedYear < currentYear || (selectedYear == currentYear && selectedMonth < currentMonth)) {
            if (selectedMonth == 11) {
                selectedMonth = 0
                selectedYear++
            } else {
                selectedMonth++
            }
        }
    }

    fun getDaysInMonth(): Int {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, selectedYear)
        cal.set(Calendar.MONTH, selectedMonth)
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    fun getStudiedDaysInMonth(): Int {
        val history = userProfile.value.studyHistory
        return history.keys.count { key ->
            val parts = key.split("-")
            parts.size == 3 && parts[0].toIntOrNull() == selectedYear && parts[1].toIntOrNull() == (selectedMonth + 1)
        }
    }

    fun getTotalStudyDays(): Int {
        return userProfile.value.studyHistory.size
    }
}

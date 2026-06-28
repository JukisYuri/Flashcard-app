package com.example.mindcard.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.mindcard.data.UserProfile
import com.example.mindcard.data.repository.UserRepository

class ProfileViewModel : ViewModel() {
    private val userRepository = UserRepository()

    val userProfile: State<UserProfile> = userRepository.userProfile

    var isEditingName by mutableStateOf(false)
    var editNameInput by mutableStateOf("")

    fun startEditing() {
        editNameInput = userProfile.value.name
        isEditingName = true
    }

    fun cancelEditing() {
        isEditingName = false
    }

    fun saveProfileName() {
        if (editNameInput.isNotBlank()) {
            userRepository.updateProfileName(editNameInput.trim())
            isEditingName = false
        }
    }
}

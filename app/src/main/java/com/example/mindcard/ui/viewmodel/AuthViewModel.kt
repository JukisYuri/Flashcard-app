package com.example.mindcard.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.mindcard.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")
    var name by mutableStateOf("")
    var errorMessage by mutableStateOf<String?>(null)
    var isLoading by mutableStateOf(false)

    fun isUserSignedIn(): Boolean {
        return authRepository.isUserSignedIn()
    }

    fun getCurrentUser(): FirebaseUser? {
        return authRepository.getCurrentUser()
    }

    fun login(onSuccess: (String) -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Email and password cannot be empty"
            return
        }
        isLoading = true
        errorMessage = null
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { result ->
                isLoading = false
                val uid = result.user?.uid ?: ""
                onSuccess(uid)
            }
            .addOnFailureListener { e ->
                isLoading = false
                errorMessage = e.localizedMessage ?: "Login failed"
            }
    }

    fun register(onSuccess: (String) -> Unit) {
        if (email.isBlank() || password.isBlank() || name.isBlank()) {
            errorMessage = "Please fill in all fields"
            return
        }
        if (password != confirmPassword) {
            errorMessage = "Passwords do not match"
            return
        }
        isLoading = true
        errorMessage = null
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email.trim(), password)
            .addOnSuccessListener { result ->
                isLoading = false
                val uid = result.user?.uid ?: ""
                com.example.mindcard.data.Database.initializeUserPersistence(uid)
                com.example.mindcard.data.Database.updateProfileName(name.trim())
                onSuccess(uid)
            }
            .addOnFailureListener { e ->
                isLoading = false
                errorMessage = e.localizedMessage ?: "Registration failed"
            }
    }

    fun sendPasswordReset(onSuccess: () -> Unit) {
        if (email.isBlank()) {
            errorMessage = "Please enter your email"
            return
        }
        isLoading = true
        errorMessage = null
        FirebaseAuth.getInstance().sendPasswordResetEmail(email.trim())
            .addOnSuccessListener {
                isLoading = false
                onSuccess()
            }
            .addOnFailureListener { e ->
                isLoading = false
                errorMessage = e.localizedMessage ?: "Reset failed"
            }
    }

    fun logout() {
        authRepository.signOut()
    }
}

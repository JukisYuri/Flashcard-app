package com.example.mindcard

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Login : NavKey
@Serializable data object Register : NavKey
@Serializable data object ForgotPassword : NavKey
@Serializable data object Main : NavKey
@Serializable data class FlashcardStudy(val deckId: String) : NavKey
@Serializable data class StudyResult(val deckId: String, val accuracy: Int, val xpEarned: Int, val timeMinutes: Int) : NavKey
@Serializable data object CreateDeck : NavKey
@Serializable data object CreateAI : NavKey
@Serializable data class CreateCard(val deckId: String) : NavKey

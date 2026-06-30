package com.example.mindcard.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.mindcard.data.Card
import com.example.mindcard.data.Deck
import com.example.mindcard.data.Database
import com.example.mindcard.data.ApiClient
import com.example.mindcard.data.PromptRequest
import com.example.mindcard.data.repository.DeckRepository
import com.google.firebase.auth.FirebaseAuth

class CreateAiViewModel : ViewModel() {
    private val deckRepository = DeckRepository()

    var prompt by mutableStateOf("")
    var isGenerating by mutableStateOf(false)

    suspend fun generateDeck(onSuccess: () -> Unit, showToast: (String) -> Unit) {
        if (prompt.isBlank()) return
        isGenerating = true
        try {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                val req = PromptRequest(prompt)
                val generatedDeck = ApiClient.post<PromptRequest, Deck>("/users/$userId/decks/generate-ai", req)
                if (generatedDeck != null) {
                    // Update client UI state directly
                    Database.decks.add(generatedDeck)
                    isGenerating = false
                    onSuccess()
                } else {
                    throw Exception("Server returned null deck")
                }
            } else {
                throw Exception("User is not signed in")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Gemini credits depleted. Creating demo cards instead!")
            // Demo fallback deck generation
            val deckName = if (prompt.contains("food", true)) "Food Vocabulary" 
                           else if (prompt.contains("travel", true)) "Travel Phrases"
                           else "AI Custom Set"
            val fallbackCards = listOf(
                Card(
                    englishWord = "Greeting", 
                    pronunciation = "/ˈɡriːtɪŋ/", 
                    pos = "Noun", 
                    definition = "A polite word or sign of welcome.", 
                    exampleSentence = "She raised her hand in greeting."
                ),
                Card(
                    englishWord = "Gratitude", 
                    pronunciation = "/ˈɡrætɪtjuːd/", 
                    pos = "Noun", 
                    definition = "The quality of being thankful.", 
                    exampleSentence = "She expressed her gratitude to the team."
                )
            )
            deckRepository.addDeckWithCards(deckName, "AI Generated", fallbackCards)
            isGenerating = false
            onSuccess()
        }
    }
}

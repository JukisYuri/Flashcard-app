package com.example.mindcard.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.mindcard.data.Card
import com.example.mindcard.data.repository.DeckRepository

class CreateCardViewModel : ViewModel() {
    private val deckRepository = DeckRepository()

    var englishWord by mutableStateOf("")
    var pronunciation by mutableStateOf("")
    var pos by mutableStateOf("Noun")
    var definition by mutableStateOf("")
    var exampleSentence by mutableStateOf("")
    var synonyms by mutableStateOf("")

    fun saveCard(deckId: String, onSuccess: () -> Unit) {
        if (englishWord.isNotBlank() && definition.isNotBlank()) {
            deckRepository.addCardToDeck(
                deckId = deckId,
                card = Card(
                    englishWord = englishWord.trim(),
                    pronunciation = pronunciation.trim(),
                    pos = pos,
                    definition = definition.trim(),
                    exampleSentence = exampleSentence.trim(),
                    synonyms = synonyms.trim()
                )
            )
            onSuccess()
        }
    }
}

package com.example.mindcard.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.mindcard.data.Card
import com.example.mindcard.data.Deck
import com.example.mindcard.data.repository.DeckRepository

class CreateDeckViewModel : ViewModel() {
    private val deckRepository = DeckRepository()

    var name by mutableStateOf("")
    var category by mutableStateOf("Languages")
    var showCategoryMenu by mutableStateOf(false)
    var showDeleteConfirm by mutableStateOf(false)

    // Card edit/delete states
    var editingCard by mutableStateOf<Card?>(null)
    var cardFrontEdit by mutableStateOf("")
    var cardPronunciationEdit by mutableStateOf("")
    var cardPosEdit by mutableStateOf("Noun")
    var cardBackEdit by mutableStateOf("")
    var cardExampleEdit by mutableStateOf("")
    var cardSynonymsEdit by mutableStateOf("")
    var showCardEditDialog by mutableStateOf(false)

    var cardToDelete by mutableStateOf<Card?>(null)
    var showCardDeleteConfirm by mutableStateOf(false)

    fun initExistingDeck(deck: Deck?) {
        if (deck != null) {
            name = deck.name
            category = deck.category
        }
    }

    fun saveDeck(existingDeckId: String?, onSuccess: () -> Unit) {
        if (name.isNotBlank()) {
            if (existingDeckId != null) {
                deckRepository.updateDeck(existingDeckId, name.trim(), category)
            } else {
                deckRepository.addDeck(name.trim(), category)
            }
            onSuccess()
        }
    }

    fun deleteDeck(deckId: String, onSuccess: () -> Unit) {
        deckRepository.deleteDeck(deckId)
        showDeleteConfirm = false
        onSuccess()
    }

    fun startEditingCard(card: Card) {
        editingCard = card
        cardFrontEdit = card.englishWord
        cardPronunciationEdit = card.pronunciation
        cardPosEdit = card.pos
        cardBackEdit = card.definition
        cardExampleEdit = card.exampleSentence
        cardSynonymsEdit = card.synonyms
        showCardEditDialog = true
    }

    fun saveCardEdit(deckId: String) {
        val card = editingCard
        if (card != null && cardFrontEdit.isNotBlank() && cardBackEdit.isNotBlank()) {
            deckRepository.updateCardInDeckFull(
                deckId = deckId,
                cardId = card.id,
                englishWord = cardFrontEdit,
                pronunciation = cardPronunciationEdit,
                pos = cardPosEdit,
                definition = cardBackEdit,
                exampleSentence = cardExampleEdit,
                synonyms = cardSynonymsEdit
            )
            showCardEditDialog = false
            editingCard = null
        }
    }

    fun confirmDeleteCard(card: Card) {
        cardToDelete = card
        showCardDeleteConfirm = true
    }

    fun deleteCard(deckId: String) {
        val card = cardToDelete
        if (card != null) {
            deckRepository.deleteCardFromDeck(deckId, card.id)
            showCardDeleteConfirm = false
            cardToDelete = null
        }
    }
}

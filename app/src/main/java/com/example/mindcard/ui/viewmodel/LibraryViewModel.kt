package com.example.mindcard.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.mindcard.data.Card
import com.example.mindcard.data.Database
import com.example.mindcard.data.Deck

class LibraryViewModel : ViewModel() {
    val decks get() = Database.decks
    var searchQuery by mutableStateOf("")
    var selectedLetter by mutableStateOf<Char?>(null)
    var favoriteVersion by mutableStateOf(0)

    fun getAllCardsCount(): Int = decks.sumOf { it.cards.size }

    fun getFavoriteCount(): Int = decks.sumOf { deck -> deck.cards.count { it.isFavorite } }

    fun getFilteredCards(): List<Card> {
        val allCards = decks.flatMap { it.cards }
        return allCards.filter { card ->
            val matchesQuery = card.englishWord.contains(searchQuery, ignoreCase = true) ||
                    card.definition.contains(searchQuery, ignoreCase = true)
            val matchesLetter = selectedLetter == null ||
                    card.englishWord.startsWith(selectedLetter.toString(), ignoreCase = true)
            matchesQuery && matchesLetter
        }
    }

    fun toggleFavorite(deckId: String, cardId: String) {
        Database.toggleFavorite(deckId, cardId)
        favoriteVersion++
    }
}

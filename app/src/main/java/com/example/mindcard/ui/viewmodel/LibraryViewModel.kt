package com.example.mindcard.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import com.example.mindcard.data.Card
import com.example.mindcard.data.Deck
import com.example.mindcard.data.repository.DeckRepository

class LibraryViewModel : ViewModel() {
    private val deckRepository = DeckRepository()

    val decks: SnapshotStateList<Deck> = deckRepository.decks

    var searchQuery by mutableStateOf("")
    var selectedLetter by mutableStateOf<Char?>(null)

    fun getAllCardsCount(): Int {
        return decks.sumOf { it.cards.size }
    }

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
}

package com.example.mindcard.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.mindcard.data.Card
import com.example.mindcard.data.Deck
import com.example.mindcard.data.repository.DeckRepository

class StudyViewModel : ViewModel() {
    private val deckRepository = DeckRepository()

    var currentDeck by mutableStateOf<Deck?>(null)
    var currentCardIndex by mutableIntStateOf(0)
    var isCardFlipped by mutableStateOf(false)

    // Statistics tracking
    var cardsCount by mutableIntStateOf(0)
    var againCount by mutableIntStateOf(0)
    var hardCount by mutableIntStateOf(0)
    var easyCount by mutableIntStateOf(0)

    fun startStudySession(deck: Deck) {
        currentDeck = deck
        currentCardIndex = 0
        isCardFlipped = false
        cardsCount = deck.cards.size
        againCount = 0
        hardCount = 0
        easyCount = 0
    }

    fun handleFeedback(feedback: String, onFinished: (accuracy: Int, xp: Int) -> Unit) {
        when (feedback) {
            "Again" -> againCount++
            "Hard" -> hardCount++
            "Easy" -> easyCount++
        }

        val deck = currentDeck
        if (deck != null) {
            if (currentCardIndex < deck.cards.size - 1) {
                currentCardIndex++
                isCardFlipped = false
            } else {
                val totalRated = againCount + hardCount + easyCount
                val accuracy = if (totalRated > 0) ((easyCount + hardCount * 0.5) / totalRated * 100).toInt() else 100
                val xpEarned = easyCount * 10 + hardCount * 5 + againCount * 2

                deckRepository.recordStudySession(deck.id, accuracy, xpEarned, 1)
                onFinished(accuracy, xpEarned)
            }
        }
    }
}

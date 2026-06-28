package com.example.mindcard.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.mindcard.data.*
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

    // Due cards for this session
    private var dueCards = mutableListOf<Card>()

    fun startStudySession(deck: Deck) {
        currentDeck = deck
        currentCardIndex = 0
        isCardFlipped = false
        cardsCount = deck.cards.size
        againCount = 0
        hardCount = 0
        easyCount = 0

        // Filter due cards
        dueCards = deck.cards.filter { card ->
            val state = card.toCardState()
            FsrsAlgorithm.isDue(state)
        }.toMutableList()

        // If no due cards, study all cards
        if (dueCards.isEmpty()) {
            dueCards = deck.cards.toMutableList()
        }
    }

    fun getDueCardsCount(): Int {
        val deck = currentDeck ?: return 0
        return FsrsAlgorithm.getDueCardsCount(deck.cards)
    }

    fun handleFeedback(feedback: String, onFinished: (accuracy: Int, xp: Int) -> Unit) {
        val rating = when (feedback) {
            "Again" -> { againCount++; Rating.Again }
            "Hard" -> { hardCount++; Rating.Hard }
            "Easy" -> { easyCount++; Rating.Easy }
            else -> { easyCount++; Rating.Good }
        }

        val deck = currentDeck
        if (deck != null && currentCardIndex < dueCards.size) {
            val currentCard = dueCards[currentCardIndex]

            // Apply FSRS algorithm
            val cardState = currentCard.toCardState()
            val newState = FsrsAlgorithm.nextCardState(cardState, rating)

            // Update card in deck
            val updatedCard = currentCard.updateFromCardState(newState)
            deckRepository.updateCardState(deck.id, updatedCard)

            // Move to next card
            if (currentCardIndex < dueCards.size - 1) {
                currentCardIndex++
                isCardFlipped = false
            } else {
                // Session complete
                val totalRated = againCount + hardCount + easyCount
                val accuracy = if (totalRated > 0) ((easyCount + hardCount * 0.5) / totalRated * 100).toInt() else 100
                val xpEarned = easyCount * 10 + hardCount * 5 + againCount * 2

                // Update mastery percentage
                val newMastery = FsrsAlgorithm.calculateMastery(deck.cards)
                deckRepository.updateDeckMastery(deck.id, newMastery)

                deckRepository.recordStudySession(deck.id, accuracy, xpEarned, 1)
                onFinished(accuracy, xpEarned)
            }
        }
    }
}

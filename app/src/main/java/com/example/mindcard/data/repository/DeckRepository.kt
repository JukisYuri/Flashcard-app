package com.example.mindcard.data.repository

import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.mindcard.data.Card
import com.example.mindcard.data.Database
import com.example.mindcard.data.Deck

class DeckRepository {
    val decks: SnapshotStateList<Deck> = Database.decks

    fun addDeck(name: String, category: String, coverUrl: String? = null): Deck {
        return Database.addDeck(name, category, coverUrl)
    }

    fun addDeckWithCards(name: String, category: String, cards: List<Card>, coverUrl: String? = null): Deck {
        return Database.addDeckWithCards(name, category, cards, coverUrl)
    }

    fun updateDeck(deckId: String, name: String, category: String) {
        Database.updateDeck(deckId, name, category)
    }

    fun deleteDeck(deckId: String) {
        Database.deleteDeck(deckId)
    }

    fun addCardToDeck(deckId: String, card: Card) {
        Database.addCardToDeck(deckId, card)
    }

    fun deleteCardFromDeck(deckId: String, cardId: String) {
        Database.deleteCardFromDeck(deckId, cardId)
    }

    fun updateCardInDeckFull(
        deckId: String,
        cardId: String,
        englishWord: String,
        pronunciation: String,
        pos: String,
        definition: String,
        exampleSentence: String,
        synonyms: String
    ) {
        Database.updateCardInDeckFull(
            deckId,
            cardId,
            englishWord,
            pronunciation,
            pos,
            definition,
            exampleSentence,
            synonyms
        )
    }

    fun recordStudySession(deckId: String, accuracy: Int, xp: Int, timeMinutes: Int, cardsReviewed: Int = 1) {
        Database.recordStudySession(deckId, accuracy, xp, timeMinutes, cardsReviewed)
    }

    fun updateCardState(deckId: String, updatedCard: Card) {
        Database.updateCardState(deckId, updatedCard)
    }

    fun updateDeckMastery(deckId: String, mastery: Int) {
        Database.updateDeckMastery(deckId, mastery)
    }

    fun updateDeckTags(deckId: String, tags: List<String>) {
        Database.updateDeckTags(deckId, tags)
    }

    fun seedDemoData() {
        Database.seedDemoData()
    }
}

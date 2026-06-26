package com.example.gizmolearn.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class Card(
    val id: String = UUID.randomUUID().toString(),
    val englishWord: String,
    val pronunciation: String,
    val pos: String, // Noun, Verb, Adj
    val definition: String,
    val exampleSentence: String,
    val synonyms: String = ""
)

data class Deck(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: String,
    val coverUrl: String? = null,
    val cards: List<Card> = emptyList(),
    val masteredPercentage: Int = 0
)

data class UserProfile(
    val name: String = "Guest Learner",
    val title: String = "Language Explorer",
    val level: Int = 1,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val totalXp: Int = 0,
    val totalWordsLearned: Int = 0,
    val studyHistory: Map<String, Boolean> = emptyMap() // "YYYY-MM-DD" -> true
)

object Database {
    val decks = mutableStateListOf<Deck>()
    val userProfile = mutableStateOf(UserProfile())

    // Tracks cards studied in the current session
    var currentSessionAccuracy = 0
    var currentSessionXp = 0
    var currentSessionTime = 0

    init {
        // Starts completely blank. No pre-loaded decks or cards.
    }

    fun addDeck(name: String, category: String, coverUrl: String? = null): Deck {
        val newDeck = Deck(name = name, category = category, coverUrl = coverUrl)
        decks.add(newDeck)
        return newDeck
    }

    fun addCardToDeck(deckId: String, card: Card) {
        val index = decks.indexOfFirst { it.id == deckId }
        if (index != -1) {
            val deck = decks[index]
            val updatedCards = deck.cards + card
            decks[index] = deck.copy(
                cards = updatedCards,
                masteredPercentage = calculateMastered(updatedCards)
            )
        }
    }

    fun deleteDeck(deckId: String) {
        decks.removeAll { it.id == deckId }
    }

    fun updateProfileName(newName: String) {
        userProfile.value = userProfile.value.copy(name = newName)
    }

    fun recordStudySession(deckId: String, accuracy: Int, xp: Int, timeMin: Int) {
        currentSessionAccuracy = accuracy
        currentSessionXp = xp
        currentSessionTime = timeMin

        // Update profile
        val profile = userProfile.value
        val todayStr = SimpleDateFormat("yyyy-MM-DD", Locale.getDefault()).format(Date())
        val updatedHistory = profile.studyHistory.toMutableMap()
        updatedHistory[todayStr] = true

        // Calculate new streak
        val hasLearnedToday = true
        var newStreak = profile.currentStreak
        if (profile.studyHistory[todayStr] != true) {
            newStreak += 1
        }
        if (newStreak == 0) newStreak = 1
        val bestStreak = maxOf(profile.bestStreak, newStreak)

        // Count unique words in all decks
        val uniqueWords = decks.flatMap { it.cards }.map { it.englishWord.lowercase() }.distinct().size

        userProfile.value = profile.copy(
            totalXp = profile.totalXp + xp,
            currentStreak = newStreak,
            bestStreak = bestStreak,
            totalWordsLearned = uniqueWords,
            studyHistory = updatedHistory
        )

        // Update deck mastery progress slightly
        val deckIndex = decks.indexOfFirst { it.id == deckId }
        if (deckIndex != -1) {
            val deck = decks[deckIndex]
            val newMastery = minOf(100, deck.masteredPercentage + (accuracy / 10))
            decks[deckIndex] = deck.copy(masteredPercentage = newMastery)
        }
    }

    private fun calculateMastered(cards: List<Card>): Int {
        // Base mastery calculation for placeholder purposes
        return if (cards.isEmpty()) 0 else 10 // Starts at 10% when cards are added
    }

    fun seedDemoData() {
        // Option to seed some clean dynamic data for demonstration testing
        decks.clear()
        val basicSet = addDeck("Basic Greetings", "Languages", null)
        addCardToDeck(basicSet.id, Card(
            englishWord = "Hello",
            pronunciation = "/həˈloʊ/",
            pos = "Noun",
            definition = "Used as a greeting or to begin a telephone conversation.",
            exampleSentence = "Hello, is anyone there?",
            synonyms = "Hi, Greetings"
        ))
        addCardToDeck(basicSet.id, Card(
            englishWord = "Serendipity",
            pronunciation = "/ˌser.ənˈdɪp.ə.ti/",
            pos = "Noun",
            definition = "The occurrence and development of events by chance in a happy or beneficial way.",
            exampleSentence = "A fortunate stroke of serendipity.",
            synonyms = "Coincidence, Luck"
        ))

        val foodSet = addDeck("Food & Dining", "Languages", null)
        addCardToDeck(foodSet.id, Card(
            englishWord = "Delicious",
            pronunciation = "/dɪˈlɪʃəs/",
            pos = "Adj",
            definition = "Highly pleasant to the taste.",
            exampleSentence = "The food was delicious.",
            synonyms = "Tasty, Yummy"
        ))
    }
}

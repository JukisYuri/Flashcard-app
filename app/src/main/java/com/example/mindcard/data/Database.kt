package com.example.mindcard.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class Card(
    val id: String = UUID.randomUUID().toString(),
    val englishWord: String = "",
    val pronunciation: String = "",
    val pos: String = "", // Noun, Verb, Adj
    val definition: String = "",
    val exampleSentence: String = "",
    val synonyms: String = ""
)

data class Deck(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val category: String = "",
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

    private val db = FirebaseFirestore.getInstance()
    private var profileListener: ListenerRegistration? = null
    private var decksListener: ListenerRegistration? = null
    private var currentUserId: String? = null

    init {
        // Starts completely blank.
    }

    fun initializeUserPersistence(userId: String) {
        if (currentUserId == userId) return // Already initialized for this user

        // Clean up previous listeners
        profileListener?.remove()
        decksListener?.remove()

        currentUserId = userId

        // Listen to profile
        val profileRef = db.collection("users").document(userId)
        profileListener = profileRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val profile = snapshot.toObject(UserProfile::class.java)
                if (profile != null) {
                    userProfile.value = profile
                }
            } else {
                // If profile doesn't exist, create it
                val displayName = FirebaseAuth.getInstance().currentUser?.displayName ?: "Learner"
                val initialProfile = UserProfile(name = displayName)
                profileRef.set(initialProfile)
            }
        }

        // Listen to decks
        val decksRef = db.collection("users").document(userId).collection("decks")
        decksListener = decksRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val fetchedDecks = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Deck::class.java)?.copy(id = doc.id)
                }
                decks.clear()
                decks.addAll(fetchedDecks)
            }
        }
    }

    fun clearPersistence() {
        profileListener?.remove()
        decksListener?.remove()
        profileListener = null
        decksListener = null
        currentUserId = null
        decks.clear()
        userProfile.value = UserProfile()
    }

    fun addDeck(name: String, category: String, coverUrl: String? = null): Deck {
        val newDeck = Deck(name = name, category = category, coverUrl = coverUrl)
        val userId = currentUserId
        if (userId != null) {
            db.collection("users").document(userId).collection("decks").document(newDeck.id).set(newDeck)
        } else {
            decks.add(newDeck)
        }
        return newDeck
    }

    fun addDeckWithCards(name: String, category: String, cards: List<Card>, coverUrl: String? = null): Deck {
        val newDeck = Deck(
            name = name,
            category = category,
            cards = cards,
            coverUrl = coverUrl,
            masteredPercentage = calculateMastered(cards)
        )
        val userId = currentUserId
        if (userId != null) {
            db.collection("users").document(userId).collection("decks").document(newDeck.id).set(newDeck)
        } else {
            decks.add(newDeck)
        }
        return newDeck
    }

    fun updateDeck(deckId: String, name: String, category: String) {
        val index = decks.indexOfFirst { it.id == deckId }
        if (index != -1) {
            val deck = decks[index]
            val updatedDeck = deck.copy(name = name, category = category)
            val userId = currentUserId
            if (userId != null) {
                db.collection("users").document(userId).collection("decks").document(deckId).set(updatedDeck)
            } else {
                decks[index] = updatedDeck
            }
        }
    }

    fun addCardToDeck(deckId: String, card: Card) {
        val index = decks.indexOfFirst { it.id == deckId }
        if (index != -1) {
            val deck = decks[index]
            val updatedCards = deck.cards + card
            val updatedDeck = deck.copy(
                cards = updatedCards,
                masteredPercentage = calculateMastered(updatedCards)
            )
            val userId = currentUserId
            if (userId != null) {
                db.collection("users").document(userId).collection("decks").document(deckId).set(updatedDeck)
            } else {
                decks[index] = updatedDeck
            }
        }
    }

    fun deleteCardFromDeck(deckId: String, cardId: String) {
        val index = decks.indexOfFirst { it.id == deckId }
        if (index != -1) {
            val deck = decks[index]
            val updatedCards = deck.cards.filter { it.id != cardId }
            val updatedDeck = deck.copy(
                cards = updatedCards,
                masteredPercentage = calculateMastered(updatedCards)
            )
            val userId = currentUserId
            if (userId != null) {
                db.collection("users").document(userId).collection("decks").document(deckId).set(updatedDeck)
            } else {
                decks[index] = updatedDeck
            }
        }
    }

    fun updateCardInDeck(deckId: String, cardId: String, newFront: String, newBack: String) {
        val index = decks.indexOfFirst { it.id == deckId }
        if (index != -1) {
            val deck = decks[index]
            val updatedCards = deck.cards.map { card ->
                if (card.id == cardId) card.copy(englishWord = newFront, definition = newBack) else card
            }
            val updatedDeck = deck.copy(
                cards = updatedCards,
                masteredPercentage = calculateMastered(updatedCards)
            )
            val userId = currentUserId
            if (userId != null) {
                db.collection("users").document(userId).collection("decks").document(deckId).set(updatedDeck)
            } else {
                decks[index] = updatedDeck
            }
        }
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
        val index = decks.indexOfFirst { it.id == deckId }
        if (index != -1) {
            val deck = decks[index]
            val updatedCards = deck.cards.map { card ->
                if (card.id == cardId) {
                    card.copy(
                        englishWord = englishWord,
                        pronunciation = pronunciation,
                        pos = pos,
                        definition = definition,
                        exampleSentence = exampleSentence,
                        synonyms = synonyms
                    )
                } else card
            }
            val updatedDeck = deck.copy(
                cards = updatedCards,
                masteredPercentage = calculateMastered(updatedCards)
            )
            val userId = currentUserId
            if (userId != null) {
                db.collection("users").document(userId).collection("decks").document(deckId).set(updatedDeck)
            } else {
                decks[index] = updatedDeck
            }
        }
    }

    fun deleteDeck(deckId: String) {
        val userId = currentUserId
        if (userId != null) {
            db.collection("users").document(userId).collection("decks").document(deckId).delete()
        } else {
            decks.removeAll { it.id == deckId }
        }
    }

    fun updateUserProfile(profile: UserProfile) {
        val userId = currentUserId
        if (userId != null) {
            db.collection("users").document(userId).set(profile)
        } else {
            userProfile.value = profile
        }
    }

    fun updateProfileName(newName: String) {
        updateUserProfile(userProfile.value.copy(name = newName))
    }

    fun recordStudySession(deckId: String, accuracy: Int, xp: Int, timeMin: Int) {
        currentSessionAccuracy = accuracy
        currentSessionXp = xp
        currentSessionTime = timeMin

        // Update profile
        val profile = userProfile.value
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val updatedHistory = profile.studyHistory.toMutableMap()
        updatedHistory[todayStr] = true

        // Calculate new streak
        var newStreak = profile.currentStreak
        if (profile.studyHistory[todayStr] != true) {
            newStreak += 1
        }
        if (newStreak == 0) newStreak = 1
        val bestStreak = maxOf(profile.bestStreak, newStreak)

        // Count unique words in all decks
        val uniqueWords = decks.flatMap { it.cards }.map { it.englishWord.lowercase() }.distinct().size

        val updatedProfile = profile.copy(
            totalXp = profile.totalXp + xp,
            currentStreak = newStreak,
            bestStreak = bestStreak,
            totalWordsLearned = uniqueWords,
            studyHistory = updatedHistory
        )

        updateUserProfile(updatedProfile)

        // Update deck mastery progress slightly
        val deckIndex = decks.indexOfFirst { it.id == deckId }
        if (deckIndex != -1) {
            val deck = decks[deckIndex]
            val newMastery = minOf(100, deck.masteredPercentage + (accuracy / 10))
            val updatedDeck = deck.copy(masteredPercentage = newMastery)
            val userId = currentUserId
            if (userId != null) {
                db.collection("users").document(userId).collection("decks").document(deckId).set(updatedDeck)
            } else {
                decks[deckIndex] = updatedDeck
            }
        }
    }

    private fun calculateMastered(cards: List<Card>): Int {
        return if (cards.isEmpty()) 0 else 10 // Starts at 10% when cards are added
    }

    fun seedDemoData() {
        val userId = currentUserId
        if (userId != null) {
            // Delete all current decks in Firestore first to reset
            decks.forEach { deck ->
                db.collection("users").document(userId).collection("decks").document(deck.id).delete()
            }
        } else {
            decks.clear()
        }

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

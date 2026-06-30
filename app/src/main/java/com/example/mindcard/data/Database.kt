package com.example.mindcard.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlinx.serialization.Serializable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Serializable
data class Card(
    val id: String = UUID.randomUUID().toString(),
    val englishWord: String = "",
    val pronunciation: String = "",
    val pos: String = "", // Noun, Verb, Adj
    val definition: String = "",
    val exampleSentence: String = "",
    val synonyms: String = ""
)

@Serializable
data class Deck(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val category: String = "",
    val coverUrl: String? = null,
    val cards: List<Card> = emptyList(),
    val masteredPercentage: Int = 0
)

@Serializable
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

    private var currentUserId: String? = null

    init {
        // Starts completely blank.
    }

    fun initializeUserPersistence(userId: String) {
        if (currentUserId == userId) return // Already initialized for this user

        currentUserId = userId

        // Fetch profile and decks from Spring Boot backend asynchronously
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val profile = ApiClient.get<UserProfile>("/users/$userId")
                if (profile != null) {
                    withContext(Dispatchers.Main) {
                        userProfile.value = profile
                    }
                }
                val fetchedDecks = ApiClient.get<List<Deck>>("/users/$userId/decks")
                if (fetchedDecks != null) {
                    withContext(Dispatchers.Main) {
                        decks.clear()
                        decks.addAll(fetchedDecks)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clearPersistence() {
        currentUserId = null
        decks.clear()
        userProfile.value = UserProfile()
    }

    fun addDeck(name: String, category: String, coverUrl: String? = null): Deck {
        val newDeck = Deck(name = name, category = category, coverUrl = coverUrl)
        val userId = currentUserId
        if (userId != null) {
            decks.add(newDeck)
            CoroutineScope(Dispatchers.IO).launch {
                ApiClient.post<Deck, Deck>("/users/$userId/decks", newDeck)
            }
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
            decks.add(newDeck)
            CoroutineScope(Dispatchers.IO).launch {
                ApiClient.post<Deck, Deck>("/users/$userId/decks", newDeck)
            }
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
            decks[index] = updatedDeck
            val userId = currentUserId
            if (userId != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    ApiClient.put<Deck, Deck>("/users/$userId/decks/$deckId", updatedDeck)
                }
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
            decks[index] = updatedDeck
            val userId = currentUserId
            if (userId != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    ApiClient.post<Card, Deck>("/users/$userId/decks/$deckId/cards", card)
                }
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
            decks[index] = updatedDeck
            val userId = currentUserId
            if (userId != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    ApiClient.delete("/users/$userId/decks/$deckId/cards/$cardId")
                }
            }
        }
    }

    fun updateCardInDeck(deckId: String, cardId: String, newFront: String, newBack: String) {
        val index = decks.indexOfFirst { it.id == deckId }
        if (index != -1) {
            val deck = decks[index]
            var cardToUpdate: Card? = null
            val updatedCards = deck.cards.map { card ->
                if (card.id == cardId) {
                    val c = card.copy(englishWord = newFront, definition = newBack)
                    cardToUpdate = c
                    c
                } else card
            }
            val updatedDeck = deck.copy(
                cards = updatedCards,
                masteredPercentage = calculateMastered(updatedCards)
            )
            decks[index] = updatedDeck
            val userId = currentUserId
            val cardObj = cardToUpdate
            if (userId != null && cardObj != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    ApiClient.put<Card, Deck>("/users/$userId/decks/$deckId/cards/$cardId", cardObj)
                }
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
            var cardToUpdate: Card? = null
            val updatedCards = deck.cards.map { card ->
                if (card.id == cardId) {
                    val c = card.copy(
                        englishWord = englishWord,
                        pronunciation = pronunciation,
                        pos = pos,
                        definition = definition,
                        exampleSentence = exampleSentence,
                        synonyms = synonyms
                    )
                    cardToUpdate = c
                    c
                } else card
            }
            val updatedDeck = deck.copy(
                cards = updatedCards,
                masteredPercentage = calculateMastered(updatedCards)
            )
            decks[index] = updatedDeck
            val userId = currentUserId
            val cardObj = cardToUpdate
            if (userId != null && cardObj != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    ApiClient.put<Card, Deck>("/users/$userId/decks/$deckId/cards/$cardId", cardObj)
                }
            }
        }
    }

    fun deleteDeck(deckId: String) {
        val index = decks.indexOfFirst { it.id == deckId }
        if (index != -1) {
            decks.removeAt(index)
        }
        val userId = currentUserId
        if (userId != null) {
            CoroutineScope(Dispatchers.IO).launch {
                ApiClient.delete("/users/$userId/decks/$deckId")
            }
        }
    }

    fun updateUserProfile(profile: UserProfile) {
        userProfile.value = profile
        val userId = currentUserId
        if (userId != null) {
            CoroutineScope(Dispatchers.IO).launch {
                ApiClient.put<UserProfile, UserProfile>("/users/$userId", profile)
            }
        }
    }

    fun updateProfileName(newName: String) {
        updateUserProfile(userProfile.value.copy(name = newName))
    }

    fun recordStudySession(deckId: String, accuracy: Int, xp: Int, timeMin: Int) {
        currentSessionAccuracy = accuracy
        currentSessionXp = xp
        currentSessionTime = timeMin

        // Optimistic UI updates
        val profile = userProfile.value
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val updatedHistory = profile.studyHistory.toMutableMap()
        updatedHistory[todayStr] = true

        var newStreak = profile.currentStreak
        if (profile.studyHistory[todayStr] != true) {
            newStreak += 1
        }
        if (newStreak == 0) newStreak = 1
        val bestStreak = maxOf(profile.bestStreak, newStreak)
        val uniqueWords = decks.flatMap { it.cards }.map { it.englishWord.lowercase() }.distinct().size

        val updatedProfile = profile.copy(
            totalXp = profile.totalXp + xp,
            currentStreak = newStreak,
            bestStreak = bestStreak,
            totalWordsLearned = uniqueWords,
            studyHistory = updatedHistory
        )
        userProfile.value = updatedProfile

        val deckIndex = decks.indexOfFirst { it.id == deckId }
        if (deckIndex != -1) {
            val deck = decks[deckIndex]
            val newMastery = minOf(100, deck.masteredPercentage + (accuracy / 10))
            decks[deckIndex] = deck.copy(masteredPercentage = newMastery)
        }

        // Asynchronous server sync
        val userId = currentUserId
        if (userId != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val req = StudySessionRequest(deckId, accuracy, xp, timeMin)
                    val respProfile = ApiClient.post<StudySessionRequest, UserProfile>("/users/$userId/decks/study-session", req)
                    if (respProfile != null) {
                        withContext(Dispatchers.Main) {
                            userProfile.value = respProfile
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun calculateMastered(cards: List<Card>): Int {
        return if (cards.isEmpty()) 0 else 10 // Starts at 10% when cards are added
    }

    fun seedDemoData() {
        val userId = currentUserId
        if (userId != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Clear existing decks on server
                    val existingDecks = ApiClient.get<List<Deck>>("/users/$userId/decks") ?: emptyList()
                    for (deck in existingDecks) {
                        ApiClient.delete("/users/$userId/decks/${deck.id}")
                    }

                    // Add Basic Greetings
                    val basicDeck = Deck(
                        id = UUID.randomUUID().toString(),
                        name = "Basic Greetings",
                        category = "Languages",
                        masteredPercentage = 10,
                        cards = listOf(
                            Card(
                                englishWord = "Hello",
                                pronunciation = "/həˈloʊ/",
                                pos = "Noun",
                                definition = "Used as a greeting or to begin a telephone conversation.",
                                exampleSentence = "Hello, is anyone there?",
                                synonyms = "Hi, Greetings"
                            ),
                            Card(
                                englishWord = "Serendipity",
                                pronunciation = "/ˌser.ənˈdɪp.ə.ti/",
                                pos = "Noun",
                                definition = "The occurrence and development of events by chance in a happy or beneficial way.",
                                exampleSentence = "A fortunate stroke of serendipity.",
                                synonyms = "Coincidence, Luck"
                            )
                        )
                    )
                    ApiClient.post<Deck, Deck>("/users/$userId/decks", basicDeck)

                    // Add Food & Dining
                    val foodDeck = Deck(
                        id = UUID.randomUUID().toString(),
                        name = "Food & Dining",
                        category = "Languages",
                        masteredPercentage = 10,
                        cards = listOf(
                            Card(
                                englishWord = "Delicious",
                                pronunciation = "/dɪˈlɪʃəs/",
                                pos = "Adj",
                                definition = "Highly pleasant to the taste.",
                                exampleSentence = "The food was delicious.",
                                synonyms = "Tasty, Yummy"
                            )
                        )
                    )
                    ApiClient.post<Deck, Deck>("/users/$userId/decks", foodDeck)

                    // Refetch to align client state
                    val freshDecks = ApiClient.get<List<Deck>>("/users/$userId/decks")
                    if (freshDecks != null) {
                        withContext(Dispatchers.Main) {
                            decks.clear()
                            decks.addAll(freshDecks)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}

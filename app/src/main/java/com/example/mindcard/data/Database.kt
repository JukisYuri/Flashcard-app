package com.example.mindcard.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.example.mindcard.data.local.AppDatabase
import com.example.mindcard.data.local.entity.CardEntity
import com.example.mindcard.data.local.entity.DeckEntity
import com.example.mindcard.data.local.entity.UserProfileEntity
import com.example.mindcard.data.sync.SyncManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
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
    val synonyms: String = "",
    // Spaced Repetition fields
    val easeFactor: Double = FsrsDefaults.DEFAULT_EASE_FACTOR,
    val interval: Double = FsrsDefaults.DEFAULT_INTERVAL,
    val repetitions: Int = FsrsDefaults.DEFAULT_REPETITIONS,
    val nextReview: Long = System.currentTimeMillis(),
    val lastReview: Long = 0L,
    val reviewState: String = ReviewState.New.name
) {
    fun toCardState(): CardState {
        return CardState(
            easeFactor = easeFactor,
            interval = interval,
            repetitions = repetitions,
            nextReview = nextReview,
            lastReview = lastReview,
            state = ReviewState.entries.find { it.name == reviewState } ?: ReviewState.New
        )
    }

    fun updateFromCardState(newState: CardState): Card {
        return this.copy(
            easeFactor = newState.easeFactor,
            interval = newState.interval,
            repetitions = newState.repetitions,
            nextReview = newState.nextReview,
            lastReview = newState.lastReview,
            reviewState = newState.state.name
        )
    }
}

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

    private val db by lazy { FirebaseFirestore.getInstance() }
    private var profileListener: ListenerRegistration? = null
    private var decksListener: ListenerRegistration? = null
    private var currentUserId: String? = null

    // Room Database
    private var appDatabase: AppDatabase? = null
    private var syncManager: SyncManager? = null
    private var useOfflineMode = false

    init {
        // Starts completely blank.
    }

    fun initialize(context: Context) {
        appDatabase = AppDatabase.getInstance(context)
        syncManager = SyncManager(context)
    }

    fun initializeUserPersistence(userId: String) {
        if (currentUserId == userId) return // Already initialized for this user

        // Clean up previous listeners
        profileListener?.remove()
        decksListener?.remove()

        currentUserId = userId

        // Check if we should use offline mode
        useOfflineMode = syncManager?.isOnline() != true

        if (useOfflineMode) {
            // Load from Room database
            loadFromLocalDatabase(userId)
        } else {
            // Use Firestore with local caching
            setupFirestoreListeners(userId)
        }
    }

    private fun loadFromLocalDatabase(userId: String) {
        val database = appDatabase ?: return
        val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)

        scope.launch {
            // Load user profile
            val profileEntity = database.userProfileDao().getUserProfileSync(userId)
            if (profileEntity != null) {
                userProfile.value = profileEntity.toUserProfile()
            }

            // Load decks
            val deckEntities = database.deckDao().getAllDecksSync()
            val loadedDecks = mutableListOf<Deck>()

            for (deckEntity in deckEntities) {
                val cardEntities = database.cardDao().getCardsByDeckIdSync(deckEntity.id)
                val cards = cardEntities.map { it.toCard() }
                loadedDecks.add(deckEntity.toDeck().copy(cards = cards))
            }

            decks.clear()
            decks.addAll(loadedDecks)
        }
    }

    private fun setupFirestoreListeners(userId: String) {
        // Listen to profile
        val profileRef = db.collection("users").document(userId)
        profileListener = profileRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                // Fallback to local database
                loadFromLocalDatabase(userId)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val profile = snapshot.toObject(UserProfile::class.java)
                if (profile != null) {
                    userProfile.value = profile
                    // Save to local database
                    saveProfileToLocal(userId, profile)
                }
            } else {
                // If profile doesn't exist, create it
                val displayName = FirebaseAuth.getInstance().currentUser?.displayName ?: "Learner"
                val initialProfile = UserProfile(name = displayName)
                profileRef.set(initialProfile)
                saveProfileToLocal(userId, initialProfile)
            }
        }

        // Listen to decks
        val decksRef = db.collection("users").document(userId).collection("decks")
        decksListener = decksRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                // Fallback to local database
                loadFromLocalDatabase(userId)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val fetchedDecks = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Deck::class.java)?.copy(id = doc.id)
                }
                decks.clear()
                decks.addAll(fetchedDecks)

                // Save to local database
                saveDecksToLocal(fetchedDecks)
            }
        }
    }

    private fun saveProfileToLocal(userId: String, profile: UserProfile) {
        val database = appDatabase ?: return
        val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)

        scope.launch {
            val entity = UserProfileEntity.fromUserProfile(profile, userId)
            database.userProfileDao().insertUserProfile(entity)
        }
    }

    private fun saveDecksToLocal(decksList: List<Deck>) {
        val database = appDatabase ?: return
        val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)

        scope.launch {
            for (deck in decksList) {
                val deckEntity = DeckEntity.fromDeck(deck)
                database.deckDao().insertDeck(deckEntity)

                for (card in deck.cards) {
                    val cardEntity = CardEntity.fromCard(card, deck.id)
                    database.cardDao().insertCard(cardEntity)
                }
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
            if (useOfflineMode) {
                // Save to Room
                val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
                scope.launch {
                    syncManager?.addDeck(DeckEntity.fromDeck(newDeck))
                }
            } else {
                db.collection("users").document(userId).collection("decks").document(newDeck.id).set(newDeck)
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
            if (useOfflineMode) {
                val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
                scope.launch {
                    syncManager?.addDeck(DeckEntity.fromDeck(newDeck))
                    for (card in cards) {
                        syncManager?.addCard(CardEntity.fromCard(card, newDeck.id))
                    }
                }
            } else {
                db.collection("users").document(userId).collection("decks").document(newDeck.id).set(newDeck)
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
            val userId = currentUserId
            if (userId != null) {
                if (useOfflineMode) {
                    val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
                    scope.launch {
                        syncManager?.updateDeck(DeckEntity.fromDeck(updatedDeck))
                    }
                } else {
                    db.collection("users").document(userId).collection("decks").document(deckId).set(updatedDeck)
                }
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
                if (useOfflineMode) {
                    val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
                    scope.launch {
                        syncManager?.addCard(CardEntity.fromCard(card, deckId))
                        syncManager?.updateDeck(DeckEntity.fromDeck(updatedDeck))
                    }
                } else {
                    db.collection("users").document(userId).collection("decks").document(deckId).set(updatedDeck)
                }
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
                if (useOfflineMode) {
                    val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
                    scope.launch {
                        syncManager?.deleteCard(cardId)
                        syncManager?.updateDeck(DeckEntity.fromDeck(updatedDeck))
                    }
                } else {
                    db.collection("users").document(userId).collection("decks").document(deckId).set(updatedDeck)
                }
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
                if (useOfflineMode) {
                    val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
                    scope.launch {
                        val updatedCard = updatedCards.find { it.id == cardId }
                        if (updatedCard != null) {
                            syncManager?.updateCard(CardEntity.fromCard(updatedCard, deckId))
                        }
                        syncManager?.updateDeck(DeckEntity.fromDeck(updatedDeck))
                    }
                } else {
                    db.collection("users").document(userId).collection("decks").document(deckId).set(updatedDeck)
                }
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
                if (useOfflineMode) {
                    val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
                    scope.launch {
                        val updatedCard = updatedCards.find { it.id == cardId }
                        if (updatedCard != null) {
                            syncManager?.updateCard(CardEntity.fromCard(updatedCard, deckId))
                        }
                        syncManager?.updateDeck(DeckEntity.fromDeck(updatedDeck))
                    }
                } else {
                    db.collection("users").document(userId).collection("decks").document(deckId).set(updatedDeck)
                }
            } else {
                decks[index] = updatedDeck
            }
        }
    }

    fun deleteDeck(deckId: String) {
        val userId = currentUserId
        if (userId != null) {
            if (useOfflineMode) {
                val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
                scope.launch {
                    syncManager?.deleteDeck(deckId)
                }
            } else {
                db.collection("users").document(userId).collection("decks").document(deckId).delete()
            }
        } else {
            decks.removeAll { it.id == deckId }
        }
    }

    fun updateUserProfile(profile: UserProfile) {
        val userId = currentUserId
        if (userId != null) {
            if (useOfflineMode) {
                val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
                scope.launch {
                    syncManager?.updateUserProfile(UserProfileEntity.fromUserProfile(profile, userId))
                }
            } else {
                db.collection("users").document(userId).set(profile)
            }
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
                if (useOfflineMode) {
                    val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
                    scope.launch {
                        syncManager?.updateDeck(DeckEntity.fromDeck(updatedDeck))
                    }
                } else {
                    db.collection("users").document(userId).collection("decks").document(deckId).set(updatedDeck)
                }
            } else {
                decks[deckIndex] = updatedDeck
            }
        }
    }

    fun updateCardState(deckId: String, updatedCard: Card) {
        val index = decks.indexOfFirst { it.id == deckId }
        if (index != -1) {
            val deck = decks[index]
            val updatedCards = deck.cards.map { card ->
                if (card.id == updatedCard.id) updatedCard else card
            }
            val updatedDeck = deck.copy(cards = updatedCards)
            val userId = currentUserId
            if (userId != null) {
                if (useOfflineMode) {
                    val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
                    scope.launch {
                        syncManager?.updateCard(CardEntity.fromCard(updatedCard, deckId))
                    }
                } else {
                    db.collection("users").document(userId).collection("decks").document(deckId).set(updatedDeck)
                }
            } else {
                decks[index] = updatedDeck
            }
        }
    }

    fun updateDeckMastery(deckId: String, mastery: Int) {
        val index = decks.indexOfFirst { it.id == deckId }
        if (index != -1) {
            val deck = decks[index]
            val updatedDeck = deck.copy(masteredPercentage = mastery)
            val userId = currentUserId
            if (userId != null) {
                if (useOfflineMode) {
                    val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
                    scope.launch {
                        syncManager?.updateDeck(DeckEntity.fromDeck(updatedDeck))
                    }
                } else {
                    db.collection("users").document(userId).collection("decks").document(deckId).set(updatedDeck)
                }
            } else {
                decks[index] = updatedDeck
            }
        }
    }

    private fun calculateMastered(cards: List<Card>): Int {
        if (cards.isEmpty()) return 0
        val masteredCount = cards.count { card ->
            card.interval >= 21.0 || card.repetitions >= 3
        }
        return ((masteredCount.toDouble() / cards.size) * 100).toInt()
    }

    fun seedDemoData() {
        val userId = currentUserId
        if (userId != null) {
            // Delete all current decks in Firestore first to reset
            decks.forEach { deck ->
                if (useOfflineMode) {
                    val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
                    scope.launch {
                        syncManager?.deleteDeck(deck.id)
                    }
                } else {
                    db.collection("users").document(userId).collection("decks").document(deck.id).delete()
                }
            }
        } else {
            decks.clear()
        }

        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L

        val basicSet = addDeck("Basic Greetings", "Languages", null)
        addCardToDeck(basicSet.id, Card(
            englishWord = "Hello",
            pronunciation = "/həˈloʊ/",
            pos = "Noun",
            definition = "Used as a greeting or to begin a telephone conversation.",
            exampleSentence = "Hello, is anyone there?",
            synonyms = "Hi, Greetings",
            easeFactor = 2.5,
            interval = 1.0,
            repetitions = 1,
            nextReview = now - oneDayMs,
            lastReview = now - 2 * oneDayMs,
            reviewState = ReviewState.Review.name
        ))
        addCardToDeck(basicSet.id, Card(
            englishWord = "Serendipity",
            pronunciation = "/ˌser.ənˈdɪp.ə.ti/",
            pos = "Noun",
            definition = "The occurrence and development of events by chance in a happy or beneficial way.",
            exampleSentence = "A fortunate stroke of serendipity.",
            synonyms = "Coincidence, Luck",
            easeFactor = 2.5,
            interval = 6.0,
            repetitions = 2,
            nextReview = now + 3 * oneDayMs,
            lastReview = now - 3 * oneDayMs,
            reviewState = ReviewState.Review.name
        ))

        val foodSet = addDeck("Food & Dining", "Languages", null)
        addCardToDeck(foodSet.id, Card(
            englishWord = "Delicious",
            pronunciation = "/dɪˈlɪʃəs/",
            pos = "Adj",
            definition = "Highly pleasant to the taste.",
            exampleSentence = "The food was delicious.",
            synonyms = "Tasty, Yummy",
            easeFactor = 2.5,
            interval = 0.0,
            repetitions = 0,
            nextReview = now,
            lastReview = 0L,
            reviewState = ReviewState.New.name
        ))
    }

    // ==================== SYNC OPERATIONS ====================

    fun syncNow() {
        val userId = currentUserId ?: return
        val manager = syncManager ?: return

        if (manager.isOnline()) {
            val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
            scope.launch {
                // First sync from Firestore to local
                manager.syncAllFromFirestore()
                // Then sync local changes to Firestore
                manager.syncAllToFirestore()
                // Reload data
                loadFromLocalDatabase(userId)
            }
        }
    }

    fun isOnline(): Boolean {
        return syncManager?.isOnline() != false
    }

    fun forceOfflineMode() {
        useOfflineMode = true
    }

    fun forceOnlineMode() {
        useOfflineMode = false
        val userId = currentUserId
        if (userId != null) {
            setupFirestoreListeners(userId)
        }
    }
}

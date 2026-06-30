package com.example.mindcard.data

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.example.mindcard.widget.FlashcardWidget
import com.example.mindcard.data.local.AppDatabase
import com.example.mindcard.data.local.entity.CardEntity
import com.example.mindcard.data.local.entity.DeckEntity
import com.example.mindcard.data.local.entity.UserProfileEntity
import com.example.mindcard.data.sync.SyncManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.Calendar
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
    val synonyms: String = "",
    val isFavorite: Boolean = false,
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

@Serializable
data class Deck(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val category: String = "",
    val coverUrl: String? = null,
    val cards: List<Card> = emptyList(),
    val masteredPercentage: Int = 0,
    val tags: List<String> = emptyList()
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
    private var skipNextDeckListener = false

    private var currentUserId: String? = null

    // Room Database
    private var appDatabase: AppDatabase? = null
    private var syncManager: SyncManager? = null
    private var useOfflineMode = false
    private var widgetContext: Context? = null

    init {
        // Starts completely blank.
    }

    fun initialize(context: Context) {
        appDatabase = AppDatabase.getInstance(context)
        syncManager = SyncManager(context)
        widgetContext = context
        refreshWidget()
    }

    fun refreshWidget() {
        val context = widgetContext ?: return
        try {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, FlashcardWidget::class.java)
            )
            for (appWidgetId in appWidgetIds) {
                FlashcardWidget.updateWidget(context, appWidgetManager, appWidgetId)
            }
        } catch (e: Exception) {
            // Widget not found or error
        }
    }

    fun initializeUserPersistence(userId: String) {
        if (currentUserId == userId) return // Already initialized for this user

        currentUserId = userId

        // Check if we should use offline mode
        useOfflineMode = syncManager?.isOnline() != true

        if (useOfflineMode) {
            // Load from Room database
            loadFromLocalDatabase(userId)
        } else {
            // Fetch profile and decks from Spring Boot backend asynchronously
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val profile = ApiClient.get<UserProfile>("/users/$userId")
                    if (profile != null) {
                        withContext(Dispatchers.Main) {
                            userProfile.value = profile
                        }
                        saveProfileToLocal(userId, profile)
                    }
                    val fetchedDecks = ApiClient.get<List<Deck>>("/users/$userId/decks")
                    if (fetchedDecks != null) {
                        withContext(Dispatchers.Main) {
                            decks.clear()
                            decks.addAll(fetchedDecks)
                        }
                        saveDecksToLocal(fetchedDecks)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Fallback to local Room database on connection error
                    loadFromLocalDatabase(userId)
                }
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
                loadFromLocalDatabase(userId)
                return@addSnapshotListener
            }
            if (skipNextDeckListener) {
                skipNextDeckListener = false
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val fetchedDecks = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Deck::class.java)?.copy(id = doc.id)
                }
                decks.clear()
                decks.addAll(fetchedDecks)
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
>>>>>>> master
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
            val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
            scope.launch {
                if (useOfflineMode) {
                    syncManager?.addDeck(DeckEntity.fromDeck(newDeck))
                } else {
                    ApiClient.post<Deck, Deck>("/users/$userId/decks", newDeck)
                    syncManager?.addDeck(DeckEntity.fromDeck(newDeck))
                }
                refreshWidget()
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
            val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
            scope.launch {
                if (useOfflineMode) {
                    syncManager?.addDeck(DeckEntity.fromDeck(newDeck))
                    for (card in cards) {
                        syncManager?.addCard(CardEntity.fromCard(card, newDeck.id))
                    }
                } else {
                    ApiClient.post<Deck, Deck>("/users/$userId/decks", newDeck)
                    syncManager?.addDeck(DeckEntity.fromDeck(newDeck))
                    for (card in cards) {
                        syncManager?.addCard(CardEntity.fromCard(card, newDeck.id))
                    }
                }
                refreshWidget()
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
                val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
                scope.launch {
                    if (useOfflineMode) {
                        syncManager?.updateDeck(DeckEntity.fromDeck(updatedDeck))
                    } else {
                        ApiClient.put<Deck, Deck>("/users/$userId/decks/$deckId", updatedDeck)
                        syncManager?.updateDeck(DeckEntity.fromDeck(updatedDeck))
                    }
                    refreshWidget()
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
                val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
                scope.launch {
                    if (useOfflineMode) {
                        syncManager?.addCard(CardEntity.fromCard(card, deckId))
                        syncManager?.updateDeck(DeckEntity.fromDeck(updatedDeck))
                    } else {
                        ApiClient.post<Card, Deck>("/users/$userId/decks/$deckId/cards", card)
                        syncManager?.addCard(CardEntity.fromCard(card, deckId))
                        syncManager?.updateDeck(DeckEntity.fromDeck(updatedDeck))
                    }
                    refreshWidget()
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
                val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
                scope.launch {
                    if (useOfflineMode) {
                        syncManager?.deleteCard(cardId)
                        syncManager?.updateDeck(DeckEntity.fromDeck(updatedDeck))
                    } else {
                        ApiClient.delete("/users/$userId/decks/$deckId/cards/$cardId")
                        syncManager?.deleteCard(cardId)
                        syncManager?.updateDeck(DeckEntity.fromDeck(updatedDeck))
                    }
                    refreshWidget()
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
                val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
                scope.launch {
                    if (useOfflineMode) {
                        syncManager?.updateCard(CardEntity.fromCard(cardObj, deckId))
                        syncManager?.updateDeck(DeckEntity.fromDeck(updatedDeck))
                    } else {
                        ApiClient.put<Card, Deck>("/users/$userId/decks/$deckId/cards/$cardId", cardObj)
                        syncManager?.updateCard(CardEntity.fromCard(cardObj, deckId))
                        syncManager?.updateDeck(DeckEntity.fromDeck(updatedDeck))
                    }
                    refreshWidget()
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
                val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
                scope.launch {
                    if (useOfflineMode) {
                        syncManager?.updateCard(CardEntity.fromCard(cardObj, deckId))
                        syncManager?.updateDeck(DeckEntity.fromDeck(updatedDeck))
                    } else {
                        ApiClient.put<Card, Deck>("/users/$userId/decks/$deckId/cards/$cardId", cardObj)
                        syncManager?.updateCard(CardEntity.fromCard(cardObj, deckId))
                        syncManager?.updateDeck(DeckEntity.fromDeck(updatedDeck))
                    }
                    refreshWidget()
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
            val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
            scope.launch {
                if (useOfflineMode) {
                    syncManager?.deleteDeck(deckId)
                } else {
                    ApiClient.delete("/users/$userId/decks/$deckId")
                    syncManager?.deleteDeck(deckId)
                }
                refreshWidget()
            }
        }
    }

    fun updateUserProfile(profile: UserProfile) {
        userProfile.value = profile
        val userId = currentUserId
        if (userId != null) {
            val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
            scope.launch {
                if (useOfflineMode) {
                    syncManager?.updateUserProfile(UserProfileEntity.fromUserProfile(profile, userId))
                } else {
                    ApiClient.put<UserProfile, UserProfile>("/users/$userId", profile)
                    syncManager?.updateUserProfile(UserProfileEntity.fromUserProfile(profile, userId))
                }
            }
        }
    }

    fun updateProfileName(newName: String) {
        updateUserProfile(userProfile.value.copy(name = newName))
    }

    fun markTodayAsActive() {
        val profile = userProfile.value
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val updatedHistory = profile.studyHistory.toMutableMap()

        if (updatedHistory[todayStr] == true) return

        val yesterdayCal = Calendar.getInstance()
        yesterdayCal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(yesterdayCal.time)

        val studiedYesterday = updatedHistory[yesterdayStr] == true
        val newStreak = if (studiedYesterday) profile.currentStreak + 1 else 1

        updatedHistory[todayStr] = true
        val bestStreak = maxOf(profile.bestStreak, newStreak)

        val updatedProfile = profile.copy(
            currentStreak = newStreak,
            bestStreak = bestStreak,
            studyHistory = updatedHistory
        )
        updateUserProfile(updatedProfile)
    }

    fun recordStudySession(deckId: String, accuracy: Int, xp: Int, timeMin: Int) {
        currentSessionAccuracy = accuracy
        currentSessionXp = xp
        currentSessionTime = timeMin

        // Optimistic UI updates
        val profile = userProfile.value
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val updatedHistory = profile.studyHistory.toMutableMap()

        val yesterdayCal = Calendar.getInstance()
        yesterdayCal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(yesterdayCal.time)
        val studiedYesterday = updatedHistory[yesterdayStr] == true

        var newStreak = profile.currentStreak
        if (updatedHistory[todayStr] != true) {
            newStreak = if (studiedYesterday) profile.currentStreak + 1 else 1
            updatedHistory[todayStr] = true
        }
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
        // Update deck mastery progress slightly
        val deckIndex = decks.indexOfFirst { it.id == deckId }
        if (deckIndex != -1) {
            val deck = decks[deckIndex]
            val newMastery = minOf(100, deck.masteredPercentage + (accuracy / 10))
            val updatedDeck = deck.copy(masteredPercentage = newMastery)
            decks[deckIndex] = updatedDeck

            val userId = currentUserId
            if (userId != null) {
                val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
                scope.launch {
                    if (useOfflineMode) {
                        syncManager?.updateDeck(DeckEntity.fromDeck(updatedDeck))
                        syncManager?.updateUserProfile(UserProfileEntity.fromUserProfile(updatedProfile, userId))
                    } else {
                        // Asynchronous server sync
                        try {
                            val req = StudySessionRequest(deckId, accuracy, xp, timeMin)
                            val respProfile = ApiClient.post<StudySessionRequest, UserProfile>("/users/$userId/decks/study-session", req)
                            if (respProfile != null) {
                                withContext(Dispatchers.Main) {
                                    userProfile.value = respProfile
                                }
                                syncManager?.updateUserProfile(UserProfileEntity.fromUserProfile(respProfile, userId))
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        syncManager?.updateDeck(DeckEntity.fromDeck(updatedDeck))
                    }
                    refreshWidget()
                }
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
            // Always update local state first for immediate UI feedback
            decks[index] = updatedDeck
            // Then sync to Firestore if online
            val userId = currentUserId
            if (userId != null && !useOfflineMode) {
                db.collection("users").document(userId).collection("decks").document(deckId).set(updatedDeck)
            }
        }
    }

    fun toggleFavorite(deckId: String, cardId: String) {
        val index = decks.indexOfFirst { it.id == deckId }
        if (index != -1) {
            val deck = decks[index]
            val card = deck.cards.find { it.id == cardId } ?: return
            val updatedCard = card.copy(isFavorite = !card.isFavorite)
            val updatedCards = deck.cards.map { if (it.id == cardId) updatedCard else it }
            val updatedDeck = deck.copy(cards = updatedCards)
            decks[index] = updatedDeck
            // Keep skip flag until next Firestore sync cycle completes
            skipNextDeckListener = true
        }
    }

    fun syncFavoritesToFirestore() {
        val userId = currentUserId ?: return
        skipNextDeckListener = false
        for (deck in decks) {
            db.collection("users").document(userId).collection("decks").document(deck.id).set(deck)
        }
    }

    fun getAllFavoriteCards(): List<Card> {
        return decks.flatMap { deck -> deck.cards.filter { it.isFavorite } }
    }

    fun updateDeckTags(deckId: String, tags: List<String>) {
        val index = decks.indexOfFirst { it.id == deckId }
        if (index != -1) {
            val deck = decks[index]
            val updatedDeck = deck.copy(tags = tags)
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
        val userId = currentUserId ?: return
        val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
        scope.launch {
            try {
                val decksToSeed = listOf(
                    Deck(
                        id = UUID.randomUUID().toString(),
                        name = "Daily Conversations",
                        category = "Languages",
                        cards = listOf(
                            Card(englishWord = "Good morning", pronunciation = "/ɡʊd ˈmɔːrnɪŋ/", pos = "Phrase", definition = "A greeting used in the morning.", exampleSentence = "Good morning, how are you?"),
                            Card(englishWord = "Thank you", pronunciation = "/θæŋk juː/", pos = "Phrase", definition = "Used to express gratitude.", exampleSentence = "Thank you for your help."),
                            Card(englishWord = "Excuse me", pronunciation = "/ɪkˈskjuːz miː/", pos = "Phrase", definition = "Used to get attention or apologize.", exampleSentence = "Excuse me, where is the station?"),
                            Card(englishWord = "I'm sorry", pronunciation = "/aɪm ˈsɔːri/", pos = "Phrase", definition = "An expression of regret or apology.", exampleSentence = "I'm sorry for being late."),
                            Card(englishWord = "How are you?", pronunciation = "/haʊ ɑːr juː/", pos = "Phrase", definition = "A common greeting asking about well-being.", exampleSentence = "How are you doing today?"),
                            Card(englishWord = "Nice to meet you", pronunciation = "/naɪs tə miːt juː/", pos = "Phrase", definition = "A polite response to introductions.", exampleSentence = "Nice to meet you, John."),
                            Card(englishWord = "See you later", pronunciation = "/siː juː ˈleɪtər/", pos = "Phrase", definition = "A casual farewell phrase.", exampleSentence = "See you later, bye!"),
                            Card(englishWord = "Take care", pronunciation = "/teɪk kɛr/", pos = "Phrase", definition = "A warm farewell wishing someone well.", exampleSentence = "Take care on your journey."),
                            Card(englishWord = "Have a good day", pronunciation = "/hæv ə ɡʊd deɪ/", pos = "Phrase", definition = "A friendly farewell for daytime.", exampleSentence = "Have a good day at work!"),
                            Card(englishWord = "Good night", pronunciation = "/ɡʊd naɪt/", pos = "Phrase", definition = "A farewell used before going to bed.", exampleSentence = "Good night, sweet dreams.")
                        )
                    ),
                    Deck(
                        id = UUID.randomUUID().toString(),
                        name = "Business English",
                        category = "Languages",
                        cards = listOf(
                            Card(englishWord = "Deadline", pronunciation = "/ˈdɛdlaɪn/", pos = "Noun", definition = "The latest time or date by which something should be completed.", exampleSentence = "The deadline is Friday at 5 PM."),
                            Card(englishWord = "Meeting", pronunciation = "/ˈmiːtɪŋ/", pos = "Noun", definition = "An assembly of people for discussion.", exampleSentence = "Let's discuss this in the meeting."),
                            Card(englishWord = "Presentation", pronunciation = "/ˌprɛzənˈteɪʃən/", pos = "Noun", definition = "A formal talk or demonstration.", exampleSentence = "She gave a great presentation."),
                            Card(englishWord = "Negotiate", pronunciation = "/nɪˈɡoʊʃieɪt/", pos = "Verb", definition = "To discuss terms to reach agreement.", exampleSentence = "We need to negotiate a new contract."),
                            Card(englishWord = "Collaborate", pronunciation = "/kəˈlæbəreɪt/", pos = "Verb", definition = "To work jointly with others.", exampleSentence = "Let's collaborate on this project."),
                            Card(englishWord = "Delegate", pronunciation = "/ˈdɛlɪɡeɪt/", pos = "Verb", definition = "To entrust a task to another person.", exampleSentence = "You should delegate some tasks."),
                            Card(englishWord = "Budget", pronunciation = "/ˈbʌdʒɪt/", pos = "Noun", definition = "An estimate of income and expenditure.", exampleSentence = "We are working on next year's budget."),
                            Card(englishWord = "Revenue", pronunciation = "/ˈrɛvənjuː/", pos = "Noun", definition = "Income, especially of a company.", exampleSentence = "Our revenue increased this quarter."),
                            Card(englishWord = "Strategy", pronunciation = "/ˈstrætədʒi/", pos = "Noun", definition = "A plan of action designed to achieve a goal.", exampleSentence = "We need a marketing strategy."),
                            Card(englishWord = "Innovation", pronunciation = "/ˌɪnəˈveɪʃən/", pos = "Noun", definition = "A new method, idea, or product.", exampleSentence = "Innovation is key to our success.")
                        )
                    ),
                    Deck(
                        id = UUID.randomUUID().toString(),
                        name = "Science Terms",
                        category = "Science",
                        cards = listOf(
                            Card(englishWord = "Photosynthesis", pronunciation = "/ˌfoʊtoʊˈsɪnθəsɪs/", pos = "Noun", definition = "The process by which plants convert sunlight into energy.", exampleSentence = "Photosynthesis requires carbon dioxide and light."),
                            Card(englishWord = "Gravity", pronunciation = "/ˈɡrævɪti/", pos = "Noun", definition = "The force that attracts objects toward each other.", exampleSentence = "Gravity keeps us on the ground."),
                            Card(englishWord = "Molecule", pronunciation = "/ˈmɒlɪkjuːl/", pos = "Noun", definition = "A group of atoms bonded together.", exampleSentence = "Water is a simple molecule."),
                            Card(englishWord = "Evolution", pronunciation = "/ˌɛvəˈluːʃən/", pos = "Noun", definition = "The development of species over time.", exampleSentence = "Darwin wrote about evolution."),
                            Card(englishWord = "Experiment", pronunciation = "/ɪkˈspɛrɪmənt/", pos = "Noun", definition = "A scientific procedure to test a hypothesis.", exampleSentence = "We conducted an experiment in class."),
                            Card(englishWord = "Hypothesis", pronunciation = "/haɪˈpɒθəsɪs/", pos = "Noun", definition = "A proposed explanation for a phenomenon.", exampleSentence = "We need to test our hypothesis."),
                            Card(englishWord = "Ecosystem", pronunciation = "/ˈiːkoʊˌsɪstəm/", pos = "Noun", definition = "A community of living organisms.", exampleSentence = "The forest is a diverse ecosystem."),
                            Card(englishWord = "Organism", pronunciation = "/ˈɔːrɡənɪzəm/", pos = "Noun", definition = "Any living thing.", exampleSentence = "A bacteria is a single-celled organism."),
                            Card(englishWord = "Magnetic", pronunciation = "/mæɡˈnɛtɪk/", pos = "Adj", definition = "Relating to magnetism.", exampleSentence = "Compass needles point to the magnetic north."),
                            Card(englishWord = "Chemical", pronunciation = "/ˈkɛmɪkəl/", pos = "Noun", definition = "Relating to chemistry or substances.", exampleSentence = "Oxygen is a chemical element.")
                        )
                    )
                )

                if (useOfflineMode) {
                    val currentDecks = decks.toList()
                    for (deck in currentDecks) {
                        syncManager?.deleteDeck(deck.id)
                    }
                    for (deck in decksToSeed) {
                        syncManager?.addDeck(DeckEntity.fromDeck(deck))
                        for (card in deck.cards) {
                            syncManager?.addCard(CardEntity.fromCard(card, deck.id))
                        }
                    }
                    withContext(Dispatchers.Main) {
                        decks.clear()
                        decks.addAll(decksToSeed)
                    }
                } else {
                    val existingDecks = ApiClient.get<List<Deck>>("/users/$userId/decks") ?: emptyList()
                    for (deck in existingDecks) {
                        ApiClient.delete("/users/$userId/decks/${deck.id}")
                        syncManager?.deleteDeck(deck.id)
                    }

                    for (deck in decksToSeed) {
                        ApiClient.post<Deck, Deck>("/users/$userId/decks", deck)
                        syncManager?.addDeck(DeckEntity.fromDeck(deck))
                        for (card in deck.cards) {
                            syncManager?.addCard(CardEntity.fromCard(card, deck.id))
                        }
                    }

                    val freshDecks = ApiClient.get<List<Deck>>("/users/$userId/decks")
                    if (freshDecks != null) {
                        withContext(Dispatchers.Main) {
                            decks.clear()
                            decks.addAll(freshDecks)
                        }
                    }
                }
                refreshWidget()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun syncNow() {
        val userId = currentUserId
        if (userId != null && !useOfflineMode) {
            initializeUserPersistence(userId)
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
            initializeUserPersistence(userId)
        }
    }
}

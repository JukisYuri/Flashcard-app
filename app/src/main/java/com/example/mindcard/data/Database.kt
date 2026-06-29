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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
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

data class Deck(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val category: String = "",
    val coverUrl: String? = null,
    val cards: List<Card> = emptyList(),
    val masteredPercentage: Int = 0,
    val tags: List<String> = emptyList()
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
    private var skipNextDeckListener = false

    private val db by lazy { FirebaseFirestore.getInstance() }
    private var profileListener: ListenerRegistration? = null
    private var decksListener: ListenerRegistration? = null
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

        val profile = userProfile.value
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val updatedHistory = profile.studyHistory.toMutableMap()

        if (updatedHistory[todayStr] != true) {
            val yesterdayCal = Calendar.getInstance()
            yesterdayCal.add(Calendar.DAY_OF_YEAR, -1)
            val yesterdayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(yesterdayCal.time)
            val studiedYesterday = updatedHistory[yesterdayStr] == true
            val newStreak = if (studiedYesterday) profile.currentStreak + 1 else 1
            updatedHistory[todayStr] = true
            val bestStreak = maxOf(profile.bestStreak, newStreak)

            val uniqueWords = decks.flatMap { it.cards }.map { it.englishWord.lowercase() }.distinct().size

            val updatedProfile = profile.copy(
                totalXp = profile.totalXp + xp,
                currentStreak = newStreak,
                bestStreak = bestStreak,
                totalWordsLearned = uniqueWords,
                studyHistory = updatedHistory
            )
            updateUserProfile(updatedProfile)
        } else {
            val uniqueWords = decks.flatMap { it.cards }.map { it.englishWord.lowercase() }.distinct().size
            val updatedProfile = profile.copy(
                totalXp = profile.totalXp + xp,
                totalWordsLearned = uniqueWords
            )
            updateUserProfile(updatedProfile)
        }

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
        val userId = currentUserId
        if (userId != null) {
            decks.forEach { deck ->
                if (useOfflineMode) {
                    val scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
                    scope.launch { syncManager?.deleteDeck(deck.id) }
                } else {
                    db.collection("users").document(userId).collection("decks").document(deck.id).delete()
                }
            }
        } else {
            decks.clear()
        }

        val now = System.currentTimeMillis()
        val oneDayMs = 24 * 60 * 60 * 1000L

        // === DECK 1: Daily Conversations ===
        val convDeck = addDeck("Daily Conversations", "Languages", null)
        val convWords = listOf(
            Triple("Good morning", "/ɡʊd ˈmɔːrnɪŋ/", "A greeting used in the morning."),
            Triple("Thank you", "/θæŋk juː/", "Used to express gratitude."),
            Triple("Excuse me", "/ɪkˈskjuːz miː/", "Used to get attention or apologize."),
            Triple("I'm sorry", "/aɪm ˈsɔːri/", "An expression of regret or apology."),
            Triple("How are you?", "/haʊ ɑːr juː/", "A common greeting asking about well-being."),
            Triple("Nice to meet you", "/naɪs tə miːt juː/", "A polite response to introductions."),
            Triple("See you later", "/siː juː ˈleɪtər/", "A casual farewell phrase."),
            Triple("Take care", "/teɪk kɛr/", "A warm farewell wishing someone well."),
            Triple("Have a good day", "/hæv ə ɡʊd deɪ/", "A friendly farewell for daytime."),
            Triple("Good night", "/ɡʊd naɪt/", "A farewell used before going to bed.")
        )
        convWords.forEach { (word, pron, def) ->
            addCardToDeck(convDeck.id, Card(
                englishWord = word, pronunciation = pron, pos = "Phrase",
                definition = def, exampleSentence = " \"$word\" is commonly used in daily conversation.",
                synonyms = "", easeFactor = 2.5, interval = 1.0, repetitions = 0,
                nextReview = now, lastReview = 0L, reviewState = ReviewState.New.name
            ))
        }

        // === DECK 2: Business English ===
        val bizDeck = addDeck("Business English", "Languages", null)
        val bizWords = listOf(
            Triple("Deadline", "/ˈdɛdlaɪn/", "The latest time or date by which something should be completed."),
            Triple("Meeting", "/ˈmiːtɪŋ/", "An assembly of people for discussion."),
            Triple("Presentation", "/ˌprɛzənˈteɪʃən/", "A formal talk or demonstration."),
            Triple("Negotiate", "/nɪˈɡoʊʃieɪt/", "To discuss terms to reach agreement."),
            Triple("Collaborate", "/kəˈlæbəreɪt/", "To work jointly with others."),
            Triple("Delegate", "/ˈdɛlɪɡeɪt/", "To entrust a task to another person."),
            Triple("Budget", "/ˈbʌdʒɪt/", "An estimate of income and expenditure."),
            Triple("Revenue", "/ˈrɛvənjuː/", "Income, especially of a company."),
            Triple("Strategy", "/ˈstrætədʒi/", "A plan of action designed to achieve a goal."),
            Triple("Innovation", "/ˌɪnəˈveɪʃən/", "A new method, idea, or product.")
        )
        bizWords.forEach { (word, pron, def) ->
            addCardToDeck(bizDeck.id, Card(
                englishWord = word, pronunciation = pron, pos = "Noun",
                definition = def, exampleSentence = "We need to discuss the $word in today's meeting.",
                synonyms = "", easeFactor = 2.5, interval = 1.0, repetitions = 0,
                nextReview = now, lastReview = 0L, reviewState = ReviewState.New.name
            ))
        }

        // === DECK 3: Science Terms ===
        val sciDeck = addDeck("Science Terms", "Science", null)
        val sciWords = listOf(
            Triple("Photosynthesis", "/ˌfoʊtoʊˈsɪnθəsɪs/", "The process by which plants convert sunlight into energy."),
            Triple("Gravity", "/ˈɡrævɪti/", "The force that attracts objects toward each other."),
            Triple("Molecule", "/ˈmɒlɪkjuːl/", "A group of atoms bonded together."),
            Triple("Evolution", "/ˌɛvəˈluːʃən/", "The development of species over time."),
            Triple("Experiment", "/ɪkˈspɛrɪmənt/", "A scientific procedure to test a hypothesis."),
            Triple("Hypothesis", "/haɪˈpɒθəsɪs/", "A proposed explanation for a phenomenon."),
            Triple("Ecosystem", "/ˈiːkoʊˌsɪstəm/", "A community of living organisms."),
            Triple("Organism", "/ˈɔːrɡənɪzəm/", "Any living thing."),
            Triple("Magnetic", "/mæɡˈnɛtɪk/", "Relating to magnetism."),
            Triple("Chemical", "/ˈkɛmɪkəl/", "Relating to chemistry or substances.")
        )
        sciWords.forEach { (word, pron, def) ->
            addCardToDeck(sciDeck.id, Card(
                englishWord = word, pronunciation = pron, pos = "Noun",
                definition = def, exampleSentence = "The $word plays a crucial role in science.",
                synonyms = "", easeFactor = 2.5, interval = 1.0, repetitions = 0,
                nextReview = now, lastReview = 0L, reviewState = ReviewState.New.name
            ))
        }

        // === DECK 4: Travel Vocabulary ===
        val travelDeck = addDeck("Travel Vocabulary", "Languages", null)
        val travelWords = listOf(
            Triple("Passport", "/ˈpɑːspɔːrt/", "An official document for international travel."),
            Triple("Airport", "/ˈɛrpɔːrt/", "A place where aircraft take off and land."),
            Triple("Reservation", "/ˌrɛzərˈveɪʃən/", "A booking for a hotel or restaurant."),
            Triple("Luggage", "/ˈlʌɡɪdʒ/", "Bags and suitcases for travel."),
            Triple("Itinerary", "/aɪˈtɪnəreri/", "A planned route or journey schedule."),
            Triple("Boarding pass", "/ˈbɔːrdɪŋ pæs/", "A document for boarding an aircraft."),
            Triple("Customs", "/ˈkʌstəmz/", "An area at borders for checking goods."),
            Triple("Currency", "/ˈkʌrənsi/", "A system of money in general use."),
            Triple("Souvenir", "/ˌsuːvəˈnɪr/", "An item bought to remember a place."),
            Triple("Excursion", "/ɪkˈskɜːrʒən/", "A short journey for pleasure.")
        )
        travelWords.forEach { (word, pron, def) ->
            addCardToDeck(travelDeck.id, Card(
                englishWord = word, pronunciation = pron, pos = "Noun",
                definition = def, exampleSentence = "I need my $word for the trip.",
                synonyms = "", easeFactor = 2.5, interval = 1.0, repetitions = 0,
                nextReview = now, lastReview = 0L, reviewState = ReviewState.New.name
            ))
        }

        // === DECK 5: Emotions & Feelings ===
        val emoDeck = addDeck("Emotions & Feelings", "Languages", null)
        val emoWords = listOf(
            Triple("Grateful", "/ˈɡreɪtfəl/", "Feeling or showing thanks."),
            Triple("Anxious", "/ˈæŋkʃəs/", "Feeling worried or nervous."),
            Triple("Euphoric", "/juːˈfɔːrɪk/", "Feeling intense happiness."),
            Triple("Melancholy", "/ˈmɛlənkɒli/", "A feeling of deep sadness."),
            Triple("Nostalgic", "/nɒˈstældʒɪk/", "Feeling sentimental about the past."),
            Triple("Curious", "/ˈkjʊəriəs/", "Eager to know or learn something."),
            Triple("Confident", "/ˈkɒnfɪdənt/", "Feeling certain about one's abilities."),
            Triple("Overwhelmed", "/ˌoʊvərˈwɛlmd/", "Feeling buried under difficulty."),
            Triple("Content", "/kənˈtɛnt/", "In a state of peaceful happiness."),
            Triple("Ambivalent", "/æmˈbɪvələnt/", "Having mixed feelings about something.")
        )
        emoWords.forEach { (word, pron, def) ->
            addCardToDeck(emoDeck.id, Card(
                englishWord = word, pronunciation = pron, pos = "Adj",
                definition = def, exampleSentence = "I feel $word when I think about this.",
                synonyms = "", easeFactor = 2.5, interval = 1.0, repetitions = 0,
                nextReview = now, lastReview = 0L, reviewState = ReviewState.New.name
            ))
        }

        // === DECK 6: Academic Words ===
        val acadDeck = addDeck("Academic Words", "Science", null)
        val acadWords = listOf(
            Triple("Analyze", "/ˈænəlaɪz/", "To examine something in detail."),
            Triple("Synthesize", "/ˈsɪnθəsaɪz/", "To combine elements into a whole."),
            Triple("Evaluate", "/ɪˈvæljueɪt/", "To form an idea of the value of something."),
            Triple("Significant", "/sɪɡˈnɪfɪkənt/", "Sufficiently great or important."),
            Triple("Fundamental", "/ˌfʌndəˈmɛntəl/", "Forming a necessary base or core."),
            Triple("Controversy", "/ˈkɒntrəvɜːsi/", "Prolonged public disagreement."),
            Triple("Consequence", "/ˈkɒnsɪkwəns/", "A result of an action."),
            Triple("Ambiguous", "/æmˈbɪɡjuəs/", "Open to more than one interpretation."),
            Triple("Substantial", "/səbˈstænʃəl/", "Of considerable importance or size."),
            Triple("Phenomenon", "/fɪˈnɒmɪnən/", "A fact or event that can be observed.")
        )
        acadWords.forEach { (word, pron, def) ->
            addCardToDeck(acadDeck.id, Card(
                englishWord = word, pronunciation = pron, pos = "Verb",
                definition = def, exampleSentence = "It is important to $word this concept.",
                synonyms = "", easeFactor = 2.5, interval = 1.0, repetitions = 0,
                nextReview = now, lastReview = 0L, reviewState = ReviewState.New.name
            ))
        }

        // === DECK 7: Idioms & Phrases ===
        val idiomDeck = addDeck("Idioms & Phrases", "Languages", null)
        val idiomWords = listOf(
            Triple("Break the ice", "/breɪk ðə aɪs/", "To initiate conversation in a social setting."),
            Triple("Hit the nail on the head", "", "To be exactly right about something."),
            Triple("Bite off more than you can chew", "", "To take on more than you can handle."),
            Triple("A piece of cake", "", "Something very easy to do."),
            Triple("Let the cat out of the bag", "", "To reveal a secret accidentally."),
            Triple("Burn the midnight oil", "", "To work late into the night."),
            Triple("Kill two birds with one stone", "", "To accomplish two things with one action."),
            Triple("Cost an arm and a leg", "", "To be very expensive."),
            Triple("Under the weather", "", "Feeling ill or unwell."),
            Triple("Once in a blue moon", "", "Very rarely.")
        )
        idiomWords.forEach { (word, pron, def) ->
            addCardToDeck(idiomDeck.id, Card(
                englishWord = word, pronunciation = pron, pos = "Phrase",
                definition = def, exampleSentence = "\"$word\" is a common English idiom.",
                synonyms = "", easeFactor = 2.5, interval = 1.0, repetitions = 0,
                nextReview = now, lastReview = 0L, reviewState = ReviewState.New.name
            ))
        }

        refreshWidget()
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

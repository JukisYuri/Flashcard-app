package com.example.mindcard.data.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.mindcard.data.local.AppDatabase
import com.example.mindcard.data.local.entity.CardEntity
import com.example.mindcard.data.local.entity.DeckEntity
import com.example.mindcard.data.local.entity.UserProfileEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class SyncManager(private val context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun isOnline(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun getUserId(): String? = auth.currentUser?.uid

    // ==================== DECK OPERATIONS ====================

    suspend fun addDeck(deck: DeckEntity) {
        // Always save to local first
        db.deckDao().insertDeck(deck)

        // Try to sync to Firestore if online
        if (isOnline()) {
            try {
                syncDeckToFirestore(deck)
            } catch (e: Exception) {
                // Will be synced later when connection is available
            }
        }
    }

    suspend fun updateDeck(deck: DeckEntity) {
        val updatedDeck = deck.copy(lastModified = System.currentTimeMillis())
        db.deckDao().updateDeck(updatedDeck)

        if (isOnline()) {
            try {
                syncDeckToFirestore(updatedDeck)
            } catch (e: Exception) {
                // Will be synced later
            }
        }
    }

    suspend fun deleteDeck(deckId: String) {
        db.deckDao().softDeleteDeck(deckId)

        if (isOnline()) {
            try {
                val userId = getUserId() ?: return
                firestore.collection("users").document(userId)
                    .collection("decks").document(deckId).delete().await()
            } catch (e: Exception) {
                // Will be synced later
            }
        }
    }

    suspend fun getDecks(): List<DeckEntity> {
        return db.deckDao().getAllDecksSync()
    }

    // ==================== CARD OPERATIONS ====================

    suspend fun addCard(card: CardEntity) {
        db.cardDao().insertCard(card)

        if (isOnline()) {
            try {
                syncCardToFirestore(card)
            } catch (e: Exception) {
                // Will be synced later
            }
        }
    }

    suspend fun updateCard(card: CardEntity) {
        val updatedCard = card.copy(lastModified = System.currentTimeMillis())
        db.cardDao().updateCard(updatedCard)

        if (isOnline()) {
            try {
                syncCardToFirestore(updatedCard)
            } catch (e: Exception) {
                // Will be synced later
            }
        }
    }

    suspend fun deleteCard(cardId: String) {
        db.cardDao().softDeleteCard(cardId)

        if (isOnline()) {
            try {
                val userId = getUserId() ?: return
                // Find and delete from Firestore
                val deckDocs = firestore.collection("users").document(userId)
                    .collection("decks").get().await()
                for (deckDoc in deckDocs.documents) {
                    val cardsRef = deckDoc.reference.collection("cards")
                    val cardDoc = cardsRef.document(cardId).get().await()
                    if (cardDoc.exists()) {
                        cardDoc.reference.delete().await()
                        break
                    }
                }
            } catch (e: Exception) {
                // Will be synced later
            }
        }
    }

    suspend fun getCardsByDeckId(deckId: String): List<CardEntity> {
        return db.cardDao().getCardsByDeckIdSync(deckId)
    }

    // ==================== USER PROFILE OPERATIONS ====================

    suspend fun updateUserProfile(profile: UserProfileEntity) {
        db.userProfileDao().insertUserProfile(profile)

        if (isOnline()) {
            try {
                syncProfileToFirestore(profile)
            } catch (e: Exception) {
                // Will be synced later
            }
        }
    }

    suspend fun getUserProfile(userId: String): UserProfileEntity? {
        return db.userProfileDao().getUserProfileSync(userId)
    }

    // ==================== FIREBASE SYNC ====================

    private suspend fun syncDeckToFirestore(deck: DeckEntity) {
        val userId = getUserId() ?: return
        val deckData = hashMapOf(
            "id" to deck.id,
            "name" to deck.name,
            "category" to deck.category,
            "coverUrl" to deck.coverUrl,
            "masteredPercentage" to deck.masteredPercentage
        )
        firestore.collection("users").document(userId)
            .collection("decks").document(deck.id).set(deckData).await()
    }

    private suspend fun syncCardToFirestore(card: CardEntity) {
        val userId = getUserId() ?: return
        val cardData = hashMapOf(
            "id" to card.id,
            "englishWord" to card.englishWord,
            "pronunciation" to card.pronunciation,
            "pos" to card.pos,
            "definition" to card.definition,
            "exampleSentence" to card.exampleSentence,
            "synonyms" to card.synonyms,
            "easeFactor" to card.easeFactor,
            "interval" to card.interval,
            "repetitions" to card.repetitions,
            "nextReview" to card.nextReview,
            "lastReview" to card.lastReview,
            "reviewState" to card.reviewState
        )
        firestore.collection("users").document(userId)
            .collection("decks").document(card.deckId)
            .collection("cards").document(card.id).set(cardData).await()
    }

    private suspend fun syncProfileToFirestore(profile: UserProfileEntity) {
        val userId = getUserId() ?: return
        val profileData = hashMapOf(
            "name" to profile.name,
            "title" to profile.title,
            "level" to profile.level,
            "currentStreak" to profile.currentStreak,
            "bestStreak" to profile.bestStreak,
            "totalXp" to profile.totalXp,
            "totalWordsLearned" to profile.totalWordsLearned,
            "studyHistory" to profile.studyHistory
        )
        firestore.collection("users").document(userId).set(profileData).await()
    }

    // ==================== FULL SYNC ====================

    suspend fun syncAllFromFirestore() {
        val userId = getUserId() ?: return

        try {
            // Sync decks
            val deckDocs = firestore.collection("users").document(userId)
                .collection("decks").get().await()

            val deckEntities = deckDocs.documents.mapNotNull { doc ->
                doc.toObject(DeckEntity::class.java)?.copy(id = doc.id)
            }
            db.deckDao().insertDecks(deckEntities)

            // Sync cards for each deck
            for (deckDoc in deckDocs.documents) {
                val cardDocs = deckDoc.reference.collection("cards").get().await()
                val cardEntities = cardDocs.documents.mapNotNull { cardDoc ->
                    cardDoc.toObject(CardEntity::class.java)?.copy(
                        id = cardDoc.id,
                        deckId = deckDoc.id
                    )
                }
                db.cardDao().insertCards(cardEntities)
            }

            // Sync user profile
            val profileDoc = firestore.collection("users").document(userId).get().await()
            if (profileDoc.exists()) {
                val profile = profileDoc.toObject(UserProfileEntity::class.java)
                if (profile != null) {
                    db.userProfileDao().insertUserProfile(profile.copy(userId = userId))
                }
            }
        } catch (e: Exception) {
            // Handle sync error
        }
    }

    suspend fun syncAllToFirestore() {
        val userId = getUserId() ?: return

        try {
            // Sync decks
            val decks = db.deckDao().getAllDecksSync()
            for (deck in decks) {
                syncDeckToFirestore(deck)
            }

            // Sync cards
            for (deck in decks) {
                val cards = db.cardDao().getCardsByDeckIdSync(deck.id)
                for (card in cards) {
                    syncCardToFirestore(card)
                }
            }

            // Sync profile
            val profile = db.userProfileDao().getUserProfileSync(userId)
            if (profile != null) {
                syncProfileToFirestore(profile)
            }
        } catch (e: Exception) {
            // Handle sync error
        }
    }

    fun cancelSync() {
        scope.cancel()
    }
}

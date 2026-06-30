package com.example.mindcard.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindcard.data.Database
import com.example.mindcard.data.Deck
import com.example.mindcard.data.FsrsAlgorithm
import com.example.mindcard.data.UserProfile
import com.example.mindcard.data.repository.DeckRepository
import com.example.mindcard.data.repository.UserRepository
import com.example.mindcard.data.sync.SyncManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val userRepository = UserRepository()
    private val deckRepository = DeckRepository()

    val userProfile: State<UserProfile> = userRepository.userProfile
    val dailyRecord: State<com.example.mindcard.data.DailyStudyRecord> = com.example.mindcard.data.Database.dailyRecord
    val decks: SnapshotStateList<Deck> = deckRepository.decks
    val currentSessionTime: Int get() = com.example.mindcard.data.Database.currentSessionTime

    var showReviewGate by mutableStateOf(false)
    var reviewGateAccuracy by mutableIntStateOf(0)
    var reviewGateXp by mutableIntStateOf(0)
    var reviewGateCards by mutableIntStateOf(0)
    var showReviewGateResult by mutableStateOf(false)

    init {
        userRepository.markTodayAsActive()
    }

    fun getTotalDueCards(): Int {
        return decks.sumOf { deck ->
            FsrsAlgorithm.getDueCardsCount(deck.cards)
        }
    }

    fun seedDemoData() {
        deckRepository.seedDemoData()
    }

    fun deleteDeck(deckId: String) {
        deckRepository.deleteDeck(deckId)
    }

    fun openReviewGate() {
        val due = getTotalDueCards()
        if (!showReviewGate && due > 0) {
            showReviewGate = true
        }
    }

    fun onReviewGateFinished(accuracy: Int, xp: Int, cardsReviewed: Int) {
        showReviewGate = false
        reviewGateAccuracy = accuracy
        reviewGateXp = xp
        reviewGateCards = cardsReviewed
        if (cardsReviewed > 0) {
            showReviewGateResult = true
        }
    }

    fun dismissReviewGateResult() {
        showReviewGateResult = false
    }

    fun deleteDeckPermanently(deckId: String, syncManager: SyncManager) {
        Database.decks.removeIf { it.id == deckId }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                syncManager.deleteDeck(deckId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

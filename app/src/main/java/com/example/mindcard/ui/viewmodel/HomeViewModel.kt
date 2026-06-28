package com.example.mindcard.ui.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import com.example.mindcard.data.Deck
import com.example.mindcard.data.UserProfile
import com.example.mindcard.data.repository.DeckRepository
import com.example.mindcard.data.repository.UserRepository

class HomeViewModel : ViewModel() {
    private val userRepository = UserRepository()
    private val deckRepository = DeckRepository()

    val userProfile: State<UserProfile> = userRepository.userProfile
    val decks: SnapshotStateList<Deck> = deckRepository.decks
    val currentSessionTime: Int get() = com.example.mindcard.data.Database.currentSessionTime

    fun seedDemoData() {
        deckRepository.seedDemoData()
    }
}

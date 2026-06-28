package com.example.mindcard.data.local.dao

import androidx.room.*
import com.example.mindcard.data.local.entity.DeckEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeckDao {
    @Query("SELECT * FROM decks WHERE isDeleted = 0")
    fun getAllDecks(): Flow<List<DeckEntity>>

    @Query("SELECT * FROM decks WHERE isDeleted = 0")
    suspend fun getAllDecksSync(): List<DeckEntity>

    @Query("SELECT * FROM decks WHERE id = :deckId AND isDeleted = 0")
    suspend fun getDeckById(deckId: String): DeckEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeck(deck: DeckEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDecks(decks: List<DeckEntity>)

    @Update
    suspend fun updateDeck(deck: DeckEntity)

    @Delete
    suspend fun deleteDeck(deck: DeckEntity)

    @Query("UPDATE decks SET isDeleted = 1, lastModified = :timestamp WHERE id = :deckId")
    suspend fun softDeleteDeck(deckId: String, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT * FROM decks WHERE lastModified > :timestamp AND isDeleted = 0")
    suspend fun getDecksModifiedAfter(timestamp: Long): List<DeckEntity>

    @Query("UPDATE decks SET masteredPercentage = :mastery, lastModified = :timestamp WHERE id = :deckId")
    suspend fun updateDeckMastery(deckId: String, mastery: Int, timestamp: Long = System.currentTimeMillis())
}

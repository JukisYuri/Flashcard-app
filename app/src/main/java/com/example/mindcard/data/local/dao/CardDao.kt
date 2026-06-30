package com.example.mindcard.data.local.dao

import androidx.room.*
import com.example.mindcard.data.local.entity.CardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {
    @Query("SELECT * FROM cards WHERE deckId = :deckId AND isDeleted = 0")
    fun getCardsByDeckId(deckId: String): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE deckId = :deckId AND isDeleted = 0")
    suspend fun getCardsByDeckIdSync(deckId: String): List<CardEntity>

    @Query("SELECT * FROM cards WHERE id = :cardId AND isDeleted = 0")
    suspend fun getCardById(cardId: String): CardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: CardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCards(cards: List<CardEntity>)

    @Update
    suspend fun updateCard(card: CardEntity)

    @Delete
    suspend fun deleteCard(card: CardEntity)

    @Query("SELECT * FROM cards WHERE isDeleted = 1")
    suspend fun getSoftDeletedCards(): List<CardEntity>

    @Query("UPDATE cards SET isDeleted = 1, lastModified = :timestamp WHERE id = :cardId")
    suspend fun softDeleteCard(cardId: String, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT * FROM cards WHERE lastModified > :timestamp AND isDeleted = 0")
    suspend fun getCardsModifiedAfter(timestamp: Long): List<CardEntity>

    @Query("SELECT COUNT(*) FROM cards WHERE deckId = :deckId AND isDeleted = 0")
    suspend fun getCardCount(deckId: String): Int

    @Query("SELECT * FROM cards WHERE isDeleted = 0")
    suspend fun getAllCards(): List<CardEntity>
}

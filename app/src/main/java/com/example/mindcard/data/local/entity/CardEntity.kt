package com.example.mindcard.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.mindcard.data.Card

@Entity(tableName = "cards")
data class CardEntity(
    @PrimaryKey val id: String,
    val deckId: String,
    val englishWord: String,
    val pronunciation: String,
    val pos: String,
    val definition: String,
    val exampleSentence: String,
    val synonyms: String,
    // Spaced Repetition fields
    val easeFactor: Double,
    val interval: Double,
    val repetitions: Int,
    val nextReview: Long,
    val lastReview: Long,
    val reviewState: String,
    // Sync fields
    val lastModified: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
) {
    fun toCard(): Card {
        return Card(
            id = id,
            englishWord = englishWord,
            pronunciation = pronunciation,
            pos = pos,
            definition = definition,
            exampleSentence = exampleSentence,
            synonyms = synonyms,
            easeFactor = easeFactor,
            interval = interval,
            repetitions = repetitions,
            nextReview = nextReview,
            lastReview = lastReview,
            reviewState = reviewState
        )
    }

    companion object {
        fun fromCard(card: Card, deckId: String): CardEntity {
            return CardEntity(
                id = card.id,
                deckId = deckId,
                englishWord = card.englishWord,
                pronunciation = card.pronunciation,
                pos = card.pos,
                definition = card.definition,
                exampleSentence = card.exampleSentence,
                synonyms = card.synonyms,
                easeFactor = card.easeFactor,
                interval = card.interval,
                repetitions = card.repetitions,
                nextReview = card.nextReview,
                lastReview = card.lastReview,
                reviewState = card.reviewState
            )
        }
    }
}

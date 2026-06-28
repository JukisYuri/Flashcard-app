package com.example.mindcard.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.mindcard.data.Deck

@Entity(tableName = "decks")
data class DeckEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val coverUrl: String?,
    val masteredPercentage: Int,
    // Sync fields
    val lastModified: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
) {
    fun toDeck(): Deck {
        return Deck(
            id = id,
            name = name,
            category = category,
            coverUrl = coverUrl,
            masteredPercentage = masteredPercentage
        )
    }

    companion object {
        fun fromDeck(deck: Deck): DeckEntity {
            return DeckEntity(
                id = deck.id,
                name = deck.name,
                category = deck.category,
                coverUrl = deck.coverUrl,
                masteredPercentage = deck.masteredPercentage
            )
        }
    }
}

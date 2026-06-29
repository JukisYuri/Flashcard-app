package com.example.mindcard.ui.viewmodel

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import com.example.mindcard.data.Card
import com.example.mindcard.data.Deck
import com.example.mindcard.data.repository.DeckRepository
import java.io.File

class CreateDeckViewModel : ViewModel() {
    private val deckRepository = DeckRepository()

    var name by mutableStateOf("")
    var category by mutableStateOf("Languages")
    var tags by mutableStateOf("")
    var showCategoryMenu by mutableStateOf(false)
    var showDeleteConfirm by mutableStateOf(false)

    // Card edit/delete states
    var editingCard by mutableStateOf<Card?>(null)
    var cardFrontEdit by mutableStateOf("")
    var cardPronunciationEdit by mutableStateOf("")
    var cardPosEdit by mutableStateOf("Noun")
    var cardBackEdit by mutableStateOf("")
    var cardExampleEdit by mutableStateOf("")
    var cardSynonymsEdit by mutableStateOf("")
    var showCardEditDialog by mutableStateOf(false)

    var cardToDelete by mutableStateOf<Card?>(null)
    var showCardDeleteConfirm by mutableStateOf(false)
    var showImportDialog by mutableStateOf(false)

    fun initExistingDeck(deck: Deck?) {
        if (deck != null) {
            name = deck.name
            category = deck.category
            tags = deck.tags.joinToString(", ")
        }
    }

    fun saveDeck(existingDeckId: String?, onSuccess: () -> Unit) {
        if (name.isNotBlank()) {
            val tagList = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            if (existingDeckId != null) {
                deckRepository.updateDeck(existingDeckId, name.trim(), category)
                deckRepository.updateDeckTags(existingDeckId, tagList)
            } else {
                val deck = deckRepository.addDeck(name.trim(), category)
                deckRepository.updateDeckTags(deck.id, tagList)
            }
            onSuccess()
        }
    }

    fun deleteDeck(deckId: String, onSuccess: () -> Unit) {
        deckRepository.deleteDeck(deckId)
        showDeleteConfirm = false
        onSuccess()
    }

    fun startEditingCard(card: Card) {
        editingCard = card
        cardFrontEdit = card.englishWord
        cardPronunciationEdit = card.pronunciation
        cardPosEdit = card.pos
        cardBackEdit = card.definition
        cardExampleEdit = card.exampleSentence
        cardSynonymsEdit = card.synonyms
        showCardEditDialog = true
    }

    fun saveCardEdit(deckId: String) {
        val card = editingCard
        if (card != null && cardFrontEdit.isNotBlank() && cardBackEdit.isNotBlank()) {
            deckRepository.updateCardInDeckFull(
                deckId = deckId,
                cardId = card.id,
                englishWord = cardFrontEdit,
                pronunciation = cardPronunciationEdit,
                pos = cardPosEdit,
                definition = cardBackEdit,
                exampleSentence = cardExampleEdit,
                synonyms = cardSynonymsEdit
            )
            showCardEditDialog = false
            editingCard = null
        }
    }

    fun confirmDeleteCard(card: Card) {
        cardToDelete = card
        showCardDeleteConfirm = true
    }

    fun deleteCard(deckId: String) {
        val card = cardToDelete
        if (card != null) {
            deckRepository.deleteCardFromDeck(deckId, card.id)
            showCardDeleteConfirm = false
            cardToDelete = null
        }
    }

    fun exportToCSV(deck: Deck, context: Context) {
        try {
            val csvBuilder = StringBuilder()
            csvBuilder.appendLine("englishWord,pronunciation,pos,definition,exampleSentence,synonyms")

            for (card in deck.cards) {
                csvBuilder.appendLine(
                    "\"${card.englishWord}\",\"${card.pronunciation}\",\"${card.pos}\",\"${card.definition}\",\"${card.exampleSentence}\",\"${card.synonyms}\""
                )
            }

            val fileName = "${deck.name.replace(" ", "_")}_export.csv"
            val file = File(context.cacheDir, fileName)
            file.writeText(csvBuilder.toString())

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "MindCard Deck: ${deck.name}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Share Deck"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun importFromCSV(uri: android.net.Uri, context: Context, onImported: () -> Unit) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return
            val content = inputStream.bufferedReader().readText()
            inputStream.close()

            val lines = content.lines().filter { it.isNotBlank() }
            if (lines.size < 2) return

            // Parse CSV header to find column indices
            val header = parseCsvLine(lines.first())
            val colMap = mutableMapOf<String, Int>()
            header.forEachIndexed { i, col -> colMap[col.trim().lowercase()] = i }

            val wordIdx = colMap["englishword"] ?: colMap["english"] ?: colMap["word"] ?: 0
            val pronIdx = colMap["pronunciation"] ?: colMap["pronounce"] ?: -1
            val posIdx = colMap["pos"] ?: colMap["partofspeech"] ?: colMap["speech"] ?: -1
            val defIdx = colMap["definition"] ?: colMap["meaning"] ?: colMap["back"] ?: colMap["def"] ?: 3
            val exampleIdx = colMap["examplesentence"] ?: colMap["example"] ?: colMap["sentence"] ?: -1
            val synIdx = colMap["synonyms"] ?: colMap["synonym"] ?: colMap["syn"] ?: -1

            val cards = lines.drop(1).mapNotNull { line ->
                val parts = parseCsvLine(line)
                val word = parts.getOrNull(wordIdx)?.trim() ?: return@mapNotNull null
                val def = parts.getOrNull(defIdx)?.trim() ?: return@mapNotNull null
                if (word.isEmpty() || def.isEmpty()) return@mapNotNull null

                Card(
                    englishWord = word,
                    pronunciation = parts.getOrNull(pronIdx)?.trim().takeIf { !it.isNullOrEmpty() } ?: "",
                    pos = parts.getOrNull(posIdx)?.trim().takeIf { !it.isNullOrEmpty() } ?: "Noun",
                    definition = def,
                    exampleSentence = parts.getOrNull(exampleIdx)?.trim().takeIf { !it.isNullOrEmpty() } ?: "",
                    synonyms = parts.getOrNull(synIdx)?.trim().takeIf { !it.isNullOrEmpty() } ?: ""
                )
            }

            if (cards.isNotEmpty()) {
                val deckName = "Imported Deck"
                deckRepository.addDeckWithCards(deckName, "Imported", cards)
                onImported()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' && !inQuotes -> inQuotes = true
                c == '"' && inQuotes -> {
                    if (i + 1 < line.length && line[i + 1] == '"') {
                        current.append('"')
                        i++
                    } else {
                        inQuotes = false
                    }
                }
                c == ',' && !inQuotes -> {
                    result.add(current.toString())
                    current.clear()
                }
                else -> current.append(c)
            }
            i++
        }
        result.add(current.toString())
        return result
    }
}

package com.example.mindcard.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.mindcard.BuildConfig
import com.example.mindcard.data.Card
import com.example.mindcard.data.repository.DeckRepository
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class AiResponseDeck(
    val deckName: String,
    val category: String,
    val cards: List<AiResponseCard>
)

@Serializable
data class AiResponseCard(
    val englishWord: String,
    val pronunciation: String = "",
    val pos: String = "Noun",
    val definition: String,
    val exampleSentence: String = "",
    val synonyms: String = ""
)

class CreateAiViewModel : ViewModel() {
    private val deckRepository = DeckRepository()

    var prompt by mutableStateOf("")
    var isGenerating by mutableStateOf(false)

    suspend fun generateDeck(onSuccess: () -> Unit, showToast: (String) -> Unit) {
        if (prompt.isBlank()) return
        isGenerating = true
        try {
            val systemInstruction = """
                You are an expert language teacher. Create a deck of flashcards based on the user's request.
                You MUST return a JSON object with the following schema:
                {
                  "deckName": "Name of the deck",
                  "category": "Math, Science, Languages, History, etc.",
                  "cards": [
                    {
                      "englishWord": "Word/Phrase to learn",
                      "pronunciation": "Phonetic pronunciation e.g. /ˌser.ənˈdɪp.ə.ti/",
                      "pos": "Noun, Verb, or Adj",
                      "definition": "Clear concise translation/definition",
                      "exampleSentence": "An illustrative example sentence using the word",
                      "synonyms": "comma separated synonyms if any"
                    }
                  ]
                }
                Create exactly 5 to 10 high-quality cards.
            """.trimIndent()

            val fullPrompt = "$systemInstruction\n\nUser request: $prompt"
            var responseText = ""

            try {
                val generativeModel = GenerativeModel(
                    modelName = "gemini-2.5-flash",
                    apiKey = BuildConfig.GEMINI_API_KEY,
                    generationConfig = generationConfig {
                        responseMimeType = "application/json"
                    }
                )
                val response = generativeModel.generateContent(fullPrompt)
                responseText = response.text ?: ""
            } catch (err: Exception) {
                err.printStackTrace()
                // Fallback to gemini-2.0-flash
                val generativeModelFallback = GenerativeModel(
                    modelName = "gemini-2.0-flash",
                    apiKey = BuildConfig.GEMINI_API_KEY,
                    generationConfig = generationConfig {
                        responseMimeType = "application/json"
                    }
                )
                val response = generativeModelFallback.generateContent(fullPrompt)
                responseText = response.text ?: ""
            }

            // Clean markdown json syntax block
            var cleanJson = responseText.trim()
            if (cleanJson.startsWith("```")) {
                cleanJson = cleanJson.substringAfter("\n")
                if (cleanJson.contains("```")) {
                    cleanJson = cleanJson.substringBeforeLast("```")
                }
            }
            cleanJson = cleanJson.trim()

            val parsedDeck = Json.decodeFromString<AiResponseDeck>(cleanJson)
            val cardsList = parsedDeck.cards.map { card ->
                Card(
                    englishWord = card.englishWord,
                    pronunciation = card.pronunciation,
                    pos = card.pos,
                    definition = card.definition,
                    exampleSentence = card.exampleSentence,
                    synonyms = card.synonyms
                )
            }
            deckRepository.addDeckWithCards(parsedDeck.deckName, parsedDeck.category, cardsList)
            isGenerating = false
            onSuccess()
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Gemini credits depleted. Creating demo cards instead!")
            // Demo fallback deck generation
            val deckName = if (prompt.contains("food", true)) "Food Vocabulary" 
                           else if (prompt.contains("travel", true)) "Travel Phrases"
                           else "AI Custom Set"
            val fallbackCards = listOf(
                Card(
                    englishWord = "Greeting", 
                    pronunciation = "/ˈɡriːtɪŋ/", 
                    pos = "Noun", 
                    definition = "A polite word or sign of welcome.", 
                    exampleSentence = "She raised her hand in greeting."
                ),
                Card(
                    englishWord = "Gratitude", 
                    pronunciation = "/ˈɡrætɪtjuːd/", 
                    pos = "Noun", 
                    definition = "The quality of being thankful.", 
                    exampleSentence = "She expressed her gratitude to the team."
                )
            )
            deckRepository.addDeckWithCards(deckName, "AI Generated", fallbackCards)
            isGenerating = false
            onSuccess()
        }
    }
}

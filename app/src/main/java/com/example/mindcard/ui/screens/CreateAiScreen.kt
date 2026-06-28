package com.example.mindcard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.NavKey
import com.example.mindcard.BuildConfig
import com.example.mindcard.Main
import com.example.mindcard.data.Card
import com.example.mindcard.data.Database
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.launch
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAiScreen(
    onNavigate: (NavKey) -> Unit,
    modifier: Modifier = Modifier
) {
    var prompt by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Generate with AI", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { onNavigate(Main) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundFrost)
            )
        },
        containerColor = BackgroundFrost
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (!isGenerating) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFFE1E0FF))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text("✨ AI GENERATOR", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryIndigo)
                    }

                    Text(
                        text = "Build Your Deck",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryIndigo
                    )

                    Text(
                        text = "Tell Mind Card what you want to master today.",
                        fontSize = 15.sp,
                        color = OutlineColor
                    )

                    OutlinedTextField(
                        value = prompt,
                        onValueChange = { prompt = it },
                        label = { Text("What do you want to learn?") },
                        placeholder = { Text("e.g. Essential conversational Spanish for ordering food in food stall...") },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    )

                    // Quick templates
                    Text(
                        text = "Quick Templates",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF191C1E),
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("✈️ Travel Survival", "💼 Business Talk", "🍔 Food Vocabulary").forEach { template ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color.White, RoundedCornerShape(12.dp))
                                    .border(1.dp, OutlineVariantColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .clickable { prompt = "Generate a deck for $template" }
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    template,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF191C1E),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = {
                            if (prompt.isNotEmpty()) {
                                scope.launch {
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
                                        Database.addDeckWithCards(parsedDeck.deckName, parsedDeck.category, cardsList)
                                        isGenerating = false
                                        onNavigate(Main)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        scope.launch {
                                            android.widget.Toast.makeText(
                                                context,
                                                "Gemini credits depleted. Creating demo cards instead!",
                                                android.widget.Toast.LENGTH_LONG
                                            ).show()
                                        }
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
                                        Database.addDeckWithCards(deckName, "AI Generated", fallbackCards)
                                        isGenerating = false
                                        onNavigate(Main)
                                    }
                                }
                            }
                        },
                        enabled = prompt.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                    ) {
                        Text("✨ Generate My Set", modifier = Modifier.padding(vertical = 8.dp), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(color = PrimaryIndigo)
                    Text("Mind Card AI is crafting your flashcards...", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

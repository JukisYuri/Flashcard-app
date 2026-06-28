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
import androidx.navigation3.runtime.NavKey
import com.example.mindcard.Main
import com.example.mindcard.data.Card
import com.example.mindcard.data.Database
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAiScreen(
    onNavigate: (NavKey) -> Unit,
    modifier: Modifier = Modifier
) {
    var prompt by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

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
                        placeholder = { Text("e.g. Essential conversational Spanish for ordering food in Madrid...") },
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
                                    delay(2000) // Simulate AI creation loader
                                    // Seed a deck based on prompt keyword
                                    val deckName = if (prompt.contains("food", true)) "Food Vocabulary" 
                                                   else if (prompt.contains("travel", true)) "Travel Phrases"
                                                   else "AI Custom Set"
                                    val newDeck = Database.addDeck(deckName, "AI Generated")
                                    Database.addCardToDeck(
                                        newDeck.id, 
                                        Card(
                                            englishWord = "Greeting", 
                                            pronunciation = "/ˈɡriːtɪŋ/", 
                                            pos = "Noun", 
                                            definition = "A polite word or sign of welcome.", 
                                            exampleSentence = "She raised her hand in greeting."
                                        )
                                    )
                                    Database.addCardToDeck(
                                        newDeck.id, 
                                        Card(
                                            englishWord = "Gratitude", 
                                            pronunciation = "/ˈɡrætɪtjuːd/", 
                                            pos = "Noun", 
                                            definition = "The quality of being thankful.", 
                                            exampleSentence = "She expressed her gratitude to the team."
                                        )
                                    )
                                    isGenerating = false
                                    onNavigate(Main)
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

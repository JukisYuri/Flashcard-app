package com.example.mindcard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Add
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
import com.example.mindcard.CreateAI
import com.example.mindcard.CreateCard
import com.example.mindcard.Main
import com.example.mindcard.data.Card
import com.example.mindcard.data.Database
import com.example.mindcard.data.Deck
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDeckScreen(
    onNavigate: (NavKey) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Languages") }
    var showCategoryMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Deck", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { onNavigate(Main) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (name.isNotEmpty()) {
                            Database.addDeck(name, category)
                            onNavigate(Main)
                        }
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundFrost)
            )
        },
        containerColor = BackgroundFrost
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // AI Generator shortcut button
            Button(
                onClick = { onNavigate(CreateAI) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text("✨  Generate with AI", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Set Details section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .border(1.dp, OutlineVariantColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Set Details", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191C1E))

                // Name input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Set Name") },
                    placeholder = { Text("e.g. Spanish Verbs") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Category selector
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        label = { Text("Category") },
                        readOnly = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { showCategoryMenu = true }) {
                                Text("▼", fontSize = 12.sp)
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = showCategoryMenu,
                        onDismissRequest = { showCategoryMenu = false }
                    ) {
                        listOf("Languages", "Science", "History", "Math").forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    showCategoryMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Actions section for adding cards
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .border(1.dp, OutlineVariantColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Add Cards", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191C1E))

                Text(
                    text = "Save the deck first to add cards manually, or use AI generation to build vocabulary instantly.",
                    fontSize = 13.sp,
                    color = OutlineColor,
                    textAlign = TextAlign.Center
                )

                Button(
                    onClick = {
                        if (name.isNotEmpty()) {
                            val newDeck = Database.addDeck(name, category)
                            onNavigate(CreateCard(newDeck.id))
                        }
                    },
                    enabled = name.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save & Add Card Manually")
                }
            }
        }
    }
}

// ---------------------------------------------------------
// CREATE AI SCREEN
// ---------------------------------------------------------
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

// ---------------------------------------------------------
// CREATE CARD SCREEN
// ---------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCardScreen(
    deckId: String,
    onNavigate: (NavKey) -> Unit,
    modifier: Modifier = Modifier
) {
    val deck = Database.decks.firstOrNull { it.id == deckId }
    if (deck == null) {
        onNavigate(Main)
        return
    }

    var englishWord by remember { mutableStateOf("") }
    var pronunciation by remember { mutableStateOf("") }
    var pos by remember { mutableStateOf("Noun") }
    var definition by remember { mutableStateOf("") }
    var exampleSentence by remember { mutableStateOf("") }
    var synonyms by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Flashcard", fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .border(1.dp, OutlineVariantColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = englishWord,
                    onValueChange = { englishWord = it },
                    label = { Text("English Word") },
                    placeholder = { Text("e.g. Serendipity") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = pronunciation,
                    onValueChange = { pronunciation = it },
                    label = { Text("Pronunciation") },
                    placeholder = { Text("e.g. /ˌser.ənˈdɪp.ə.ti/") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // POS Selector Segmented Control
                Column {
                    Text("Part of Speech", fontSize = 12.sp, color = OutlineColor, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BackgroundFrost, RoundedCornerShape(12.dp))
                            .padding(4.dp)
                    ) {
                        listOf("Noun", "Verb", "Adj").forEach { category ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (pos == category) Color.White else Color.Transparent)
                                    .clickable { pos = category }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = category,
                                    fontWeight = FontWeight.Bold,
                                    color = if (pos == category) PrimaryIndigo else OutlineColor,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .border(1.dp, OutlineVariantColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = definition,
                    onValueChange = { definition = it },
                    label = { Text("Definition") },
                    placeholder = { Text("Enter word definition...") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = exampleSentence,
                    onValueChange = { exampleSentence = it },
                    label = { Text("Example Sentence") },
                    placeholder = { Text("Use the word in a sentence...") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = synonyms,
                    onValueChange = { synonyms = it },
                    label = { Text("Synonyms") },
                    placeholder = { Text("e.g. chance, luck") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(
                onClick = {
                    if (englishWord.isNotEmpty() && definition.isNotEmpty()) {
                        Database.addCardToDeck(
                            deckId,
                            Card(
                                englishWord = englishWord,
                                pronunciation = pronunciation,
                                pos = pos,
                                definition = definition,
                                exampleSentence = exampleSentence,
                                synonyms = synonyms
                            )
                        )
                        onNavigate(Main)
                    }
                },
                enabled = englishWord.isNotEmpty() && definition.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
            ) {
                Text("Save Card", modifier = Modifier.padding(vertical = 8.dp), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

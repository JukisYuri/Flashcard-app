package com.example.mindcard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDeckScreen(
    deckId: String? = null,
    onNavigate: (NavKey) -> Unit,
    modifier: Modifier = Modifier
) {
    val existingDeck = remember(deckId, Database.decks) {
        if (deckId != null) Database.decks.firstOrNull { it.id == deckId } else null
    }

    var name by remember { mutableStateOf(existingDeck?.name ?: "") }
    var category by remember { mutableStateOf(existingDeck?.category ?: "Languages") }
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // Card edit/delete state variables
    var editingCard by remember { mutableStateOf<Card?>(null) }
    var cardFrontEdit by remember { mutableStateOf("") }
    var cardPronunciationEdit by remember { mutableStateOf("") }
    var cardPosEdit by remember { mutableStateOf("Noun") }
    var cardBackEdit by remember { mutableStateOf("") }
    var cardExampleEdit by remember { mutableStateOf("") }
    var cardSynonymsEdit by remember { mutableStateOf("") }
    var showCardEditDialog by remember { mutableStateOf(false) }

    var cardToDelete by remember { mutableStateOf<Card?>(null) }
    var showCardDeleteConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existingDeck != null) "Edit Deck" else "Create Deck", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { onNavigate(Main) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (name.isNotEmpty()) {
                            if (existingDeck != null) {
                                Database.updateDeck(existingDeck.id, name, category)
                            } else {
                                Database.addDeck(name, category)
                            }
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
            // AI Generator shortcut button (Hide if editing existing deck)
            if (existingDeck == null) {
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

            // Cards in this Deck list (Only visible in Edit mode)
            if (existingDeck != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(20.dp))
                        .border(1.dp, OutlineVariantColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Cards in this Deck (${existingDeck.cards.size})", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191C1E))

                    if (existingDeck.cards.isEmpty()) {
                        Text(
                            text = "This deck is currently empty.",
                            fontSize = 14.sp,
                            color = OutlineColor,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        existingDeck.cards.forEach { card ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(BackgroundFrost, RoundedCornerShape(12.dp))
                                    .border(1.dp, OutlineVariantColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Q: ${card.englishWord}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF191C1E)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "A: ${card.definition}",
                                        fontSize = 13.sp,
                                        color = OutlineColor
                                    )
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            editingCard = card
                                            cardFrontEdit = card.englishWord
                                            cardPronunciationEdit = card.pronunciation
                                            cardPosEdit = card.pos
                                            cardBackEdit = card.definition
                                            cardExampleEdit = card.exampleSentence
                                            cardSynonymsEdit = card.synonyms
                                            showCardEditDialog = true
                                        }
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit Card", tint = PrimaryIndigo)
                                    }
                                    IconButton(
                                        onClick = {
                                            cardToDelete = card
                                            showCardDeleteConfirm = true
                                        }
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Card", tint = Color(0xFFBA1A1A))
                                    }
                                }
                            }
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
                    text = if (existingDeck != null) "Manage your cards or add new ones manually." else "Save the deck first to add cards manually, or use AI generation to build vocabulary instantly.",
                    fontSize = 13.sp,
                    color = OutlineColor,
                    textAlign = TextAlign.Center
                )

                Button(
                    onClick = {
                        if (name.isNotEmpty()) {
                            if (existingDeck != null) {
                                Database.updateDeck(existingDeck.id, name, category)
                                onNavigate(CreateCard(existingDeck.id))
                            } else {
                                val newDeck = Database.addDeck(name, category)
                                onNavigate(CreateCard(newDeck.id))
                            }
                        }
                    },
                    enabled = name.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (existingDeck != null) "Add Card Manually" else "Save & Add Card Manually")
                }

                if (existingDeck != null) {
                    Button(
                        onClick = { showDeleteConfirm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFDAD6)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Delete Deck", color = Color(0xFFBA1A1A), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showDeleteConfirm && existingDeck != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Deck?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this deck? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        Database.deleteDeck(existingDeck.id)
                        showDeleteConfirm = false
                        onNavigate(Main)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA1A1A))
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Card Edit Dialog
    if (showCardEditDialog && editingCard != null && existingDeck != null) {
        AlertDialog(
            onDismissRequest = { showCardEditDialog = false },
            title = { Text("Edit Flashcard", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = cardFrontEdit,
                        onValueChange = { cardFrontEdit = it },
                        label = { Text("English Word") },
                        placeholder = { Text("e.g. Serendipity") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = cardPronunciationEdit,
                        onValueChange = { cardPronunciationEdit = it },
                        label = { Text("Pronunciation") },
                        placeholder = { Text("e.g. /ˌser.ənˈdɪp.ə.ti/") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Part of Speech Selector
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
                                        .background(if (cardPosEdit == category) Color.White else Color.Transparent)
                                        .clickable { cardPosEdit = category }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = category,
                                        fontWeight = FontWeight.Bold,
                                        color = if (cardPosEdit == category) PrimaryIndigo else OutlineColor,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = cardBackEdit,
                        onValueChange = { cardBackEdit = it },
                        label = { Text("Definition") },
                        placeholder = { Text("Enter word definition...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = cardExampleEdit,
                        onValueChange = { cardExampleEdit = it },
                        label = { Text("Example Sentence") },
                        placeholder = { Text("Write a sentence using this word...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = cardSynonymsEdit,
                        onValueChange = { cardSynonymsEdit = it },
                        label = { Text("Synonyms") },
                        placeholder = { Text("e.g. chance, accident") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (cardFrontEdit.isNotBlank() && cardBackEdit.isNotBlank()) {
                            Database.updateCardInDeckFull(
                                deckId = existingDeck.id,
                                cardId = editingCard!!.id,
                                englishWord = cardFrontEdit,
                                pronunciation = cardPronunciationEdit,
                                pos = cardPosEdit,
                                definition = cardBackEdit,
                                exampleSentence = cardExampleEdit,
                                synonyms = cardSynonymsEdit
                            )
                            showCardEditDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCardEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Card Delete Confirm Dialog
    if (showCardDeleteConfirm && cardToDelete != null && existingDeck != null) {
        AlertDialog(
            onDismissRequest = { showCardDeleteConfirm = false },
            title = { Text("Delete Card?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this card from the deck?") },
            confirmButton = {
                Button(
                    onClick = {
                        Database.deleteCardFromDeck(existingDeck.id, cardToDelete!!.id)
                        showCardDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA1A1A))
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCardDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

package com.example.mindcard.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.example.mindcard.CreateAI
import com.example.mindcard.CreateCard
import com.example.mindcard.Main
import com.example.mindcard.data.Card
import com.example.mindcard.data.Database
import com.example.mindcard.ui.main.BackgroundFrost
import com.example.mindcard.ui.main.OutlineColor
import com.example.mindcard.ui.main.OutlineVariantColor
import com.example.mindcard.ui.main.PrimaryIndigo
import com.example.mindcard.ui.main.SecondaryGreen
import com.example.mindcard.ui.viewmodel.CreateDeckViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDeckScreen(
    deckId: String? = null,
    onNavigate: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateDeckViewModel = viewModel()
) {
    val existingDeck = remember(deckId, Database.decks) {
        if (deckId != null) Database.decks.firstOrNull { it.id == deckId } else null
    }

    LaunchedEffect(existingDeck) {
        viewModel.initExistingDeck(existingDeck)
    }

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
                        viewModel.saveDeck(existingDeck?.id) {
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
                    value = viewModel.name,
                    onValueChange = { viewModel.name = it },
                    label = { Text("Set Name") },
                    placeholder = { Text("e.g. Spanish Verbs") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Category selector
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = viewModel.category,
                        onValueChange = {},
                        label = { Text("Category") },
                        readOnly = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { viewModel.showCategoryMenu = true }) {
                                Text("▼", fontSize = 12.sp)
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = viewModel.showCategoryMenu,
                        onDismissRequest = { viewModel.showCategoryMenu = false }
                    ) {
                        listOf("Languages", "Science", "History", "Math").forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    viewModel.category = cat
                                    viewModel.showCategoryMenu = false
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
                                        onClick = { viewModel.startEditingCard(card) }
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit Card", tint = PrimaryIndigo)
                                    }
                                    IconButton(
                                        onClick = { viewModel.confirmDeleteCard(card) }
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
                        viewModel.saveDeck(existingDeck?.id) {
                            val targetDeckId = existingDeck?.id ?: Database.decks.last().id
                            onNavigate(CreateCard(targetDeckId))
                        }
                    },
                    enabled = viewModel.name.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (existingDeck != null) "Add Card Manually" else "Save & Add Card Manually")
                }

                if (existingDeck != null) {
                    Button(
                        onClick = { viewModel.showDeleteConfirm = true },
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

    if (viewModel.showDeleteConfirm && existingDeck != null) {
        AlertDialog(
            onDismissRequest = { viewModel.showDeleteConfirm = false },
            title = { Text("Delete Deck?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this deck? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteDeck(existingDeck.id) {
                            onNavigate(Main)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA1A1A))
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Card Edit Dialog Form
    if (viewModel.showCardEditDialog && viewModel.editingCard != null && existingDeck != null) {
        AlertDialog(
            onDismissRequest = { viewModel.showCardEditDialog = false },
            title = { Text("Edit Card", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = viewModel.cardFrontEdit,
                        onValueChange = { viewModel.cardFrontEdit = it },
                        label = { Text("English Word/Phrase") },
                        placeholder = { Text("e.g. Serendipity") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = viewModel.cardPronunciationEdit,
                        onValueChange = { viewModel.cardPronunciationEdit = it },
                        label = { Text("Phonetic Pronunciation") },
                        placeholder = { Text("e.g. /ˌser.ənˈdɪp.ə.ti/") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Part of speech selector (Segmented Control style)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Part of Speech", fontSize = 12.sp, color = OutlineColor, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("Noun", "Verb", "Adjective").forEach { pos ->
                                val selected = viewModel.cardPosEdit == pos
                                Button(
                                    onClick = { viewModel.cardPosEdit = pos },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selected) PrimaryIndigo else Color(0xFFEFF1F8),
                                        contentColor = if (selected) Color.White else OutlineColor
                                    ),
                                    contentPadding = PaddingValues(0.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(pos, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = viewModel.cardBackEdit,
                        onValueChange = { viewModel.cardBackEdit = it },
                        label = { Text("Definition / Meaning") },
                        placeholder = { Text("e.g. Sự tình cờ may mắn") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = viewModel.cardExampleEdit,
                        onValueChange = { viewModel.cardExampleEdit = it },
                        label = { Text("Example Sentence") },
                        placeholder = { Text("e.g. We found the restaurant by serendipity.") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = viewModel.cardSynonymsEdit,
                        onValueChange = { viewModel.cardSynonymsEdit = it },
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
                        viewModel.saveCardEdit(existingDeck.id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showCardEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Card Delete Confirm Dialog
    if (viewModel.showCardDeleteConfirm && viewModel.cardToDelete != null && existingDeck != null) {
        AlertDialog(
            onDismissRequest = { viewModel.showCardDeleteConfirm = false },
            title = { Text("Delete Card?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this card from the deck?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCard(existingDeck.id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA1A1A))
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showCardDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

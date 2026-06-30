package com.example.mindcard.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.example.mindcard.Main
import com.example.mindcard.data.Database
import com.example.mindcard.ui.main.BackgroundFrost
import com.example.mindcard.ui.main.OutlineColor
import com.example.mindcard.ui.main.OutlineVariantColor
import com.example.mindcard.ui.main.PrimaryIndigo
import com.example.mindcard.ui.viewmodel.CreateCardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCardScreen(
    deckId: String,
    onNavigate: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateCardViewModel = viewModel()
) {
    val deck = remember(deckId, Database.decks) {
        Database.decks.firstOrNull { it.id == deckId }
    }
    if (deck == null) {
        onNavigate(Main)
        return
    }

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
            // Live Preview Card (Sử dụng card nổi bật trực quan)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(1.dp, OutlineVariantColor.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (viewModel.pos.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .background(PrimaryIndigo.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = viewModel.pos.uppercase(),
                                    color = PrimaryIndigo,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        Text(
                            text = viewModel.englishWord.ifEmpty { "Word / Phrase" },
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (viewModel.englishWord.isEmpty()) OutlineColor.copy(alpha = 0.6f) else Color(0xFF191C1E)
                        )
                        if (viewModel.pronunciation.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = viewModel.pronunciation,
                                fontSize = 14.sp,
                                color = OutlineColor,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                        if (viewModel.definition.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = viewModel.definition,
                                fontSize = 14.sp,
                                color = Color(0xFF555555),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Input Form
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .border(1.dp, OutlineVariantColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Card Details", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191C1E))

                OutlinedTextField(
                    value = viewModel.englishWord,
                    onValueChange = { viewModel.englishWord = it },
                    label = { Text("English Word/Phrase") },
                    placeholder = { Text("e.g. Serendipity") },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = OutlineColor) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = OutlineVariantColor.copy(alpha = 0.5f),
                        focusedBorderColor = PrimaryIndigo,
                        focusedLabelColor = PrimaryIndigo
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = viewModel.pronunciation,
                    onValueChange = { viewModel.pronunciation = it },
                    label = { Text("Phonetic Pronunciation") },
                    placeholder = { Text("e.g. /ˌser.ənˈdɪp.ə.ti/") },
                    leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null, tint = OutlineColor) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = OutlineVariantColor.copy(alpha = 0.5f),
                        focusedBorderColor = PrimaryIndigo,
                        focusedLabelColor = PrimaryIndigo
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Part of Speech Segmented Selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Part of Speech", fontSize = 12.sp, color = OutlineColor, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Noun", "Verb", "Adjective").forEach { pos ->
                            val selected = viewModel.pos == pos
                            Button(
                                onClick = { viewModel.pos = pos },
                                modifier = Modifier.weight(1f).height(40.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selected) PrimaryIndigo.copy(alpha = 0.1f) else Color(0xFFF4F6FA),
                                    contentColor = if (selected) PrimaryIndigo else OutlineColor
                                ),
                                border = BorderStroke(1.dp, if (selected) PrimaryIndigo else Color.Transparent),
                                contentPadding = PaddingValues(0.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(pos, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = viewModel.definition,
                    onValueChange = { viewModel.definition = it },
                    label = { Text("Definition / Meaning") },
                    placeholder = { Text("e.g. Sự tình cờ may mắn") },
                    leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, tint = OutlineColor) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = OutlineVariantColor.copy(alpha = 0.5f),
                        focusedBorderColor = PrimaryIndigo,
                        focusedLabelColor = PrimaryIndigo
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = viewModel.exampleSentence,
                    onValueChange = { viewModel.exampleSentence = it },
                    label = { Text("Example Sentence") },
                    placeholder = { Text("Use the word in a sentence...") },
                    leadingIcon = { Icon(Icons.Default.Star, contentDescription = null, tint = OutlineColor) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = OutlineVariantColor.copy(alpha = 0.5f),
                        focusedBorderColor = PrimaryIndigo,
                        focusedLabelColor = PrimaryIndigo
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = viewModel.synonyms,
                    onValueChange = { viewModel.synonyms = it },
                    label = { Text("Synonyms") },
                    placeholder = { Text("e.g. chance, luck") },
                    leadingIcon = { Icon(Icons.Default.List, contentDescription = null, tint = OutlineColor) },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = OutlineVariantColor.copy(alpha = 0.5f),
                        focusedBorderColor = PrimaryIndigo,
                        focusedLabelColor = PrimaryIndigo
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(
                onClick = {
                    viewModel.saveCard(deckId) {
                        onNavigate(Main)
                    }
                },
                enabled = viewModel.englishWord.isNotEmpty() && viewModel.definition.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
            ) {
                Text("Save Card", modifier = Modifier.padding(vertical = 8.dp), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

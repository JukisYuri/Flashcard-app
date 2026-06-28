package com.example.mindcard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Card details input form
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
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = viewModel.pronunciation,
                    onValueChange = { viewModel.pronunciation = it },
                    label = { Text("Phonetic Pronunciation") },
                    placeholder = { Text("e.g. /ˌser.ənˈdɪp.ə.ti/") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Part of speech selector (Segmented Control style)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Part of Speech", fontSize = 12.sp, color = OutlineColor, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Noun", "Verb", "Adjective").forEach { pos ->
                            val selected = viewModel.pos == pos
                            Button(
                                onClick = { viewModel.pos = pos },
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
                    value = viewModel.definition,
                    onValueChange = { viewModel.definition = it },
                    label = { Text("Definition / Meaning") },
                    placeholder = { Text("e.g. Sự tình cờ may mắn") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = viewModel.exampleSentence,
                    onValueChange = { viewModel.exampleSentence = it },
                    label = { Text("Example Sentence") },
                    placeholder = { Text("Use the word in a sentence...") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = viewModel.synonyms,
                    onValueChange = { viewModel.synonyms = it },
                    label = { Text("Synonyms") },
                    placeholder = { Text("e.g. chance, luck") },
                    shape = RoundedCornerShape(12.dp),
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

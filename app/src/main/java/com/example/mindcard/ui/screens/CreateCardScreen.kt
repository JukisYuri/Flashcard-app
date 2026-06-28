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
import androidx.navigation3.runtime.NavKey
import com.example.mindcard.Main
import com.example.mindcard.data.Card
import com.example.mindcard.data.Database

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

package com.example.mindcard.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mindcard.MainActivity

@Composable
fun QuickAddScreen(initialText: String, onClose: () -> Unit) {
    var frontText by remember { mutableStateOf(initialText) }
    var backText by remember { mutableStateOf("") }

    // Lấy context để có thể bắn Intent sang App chính
    val context = LocalContext.current

    val decks = remember { com.example.mindcard.data.Database.decks }
    var selectedDeck by remember { mutableStateOf(decks.firstOrNull()) }
    var showDeckMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)).clickable { onClose() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .padding(24.dp)
                .clickable(enabled = false) {}
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Quick Add", fontSize = 20.sp, fontWeight = FontWeight.Bold)

                // Hoàn thiện logic nút Full Edit
                TextButton(onClick = {
                    val intent = Intent(context, MainActivity::class.java).apply {
                        // Cờ này giúp đưa App chính lên trên cùng (nếu đang chạy ngầm)
                        // hoặc mở mới (nếu đang tắt)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP

                        // Đính kèm dữ liệu mồi
                        putExtra("QUICK_ADD_WORD", frontText)
                        putExtra("QUICK_ADD_DEF", backText)
                        selectedDeck?.let { putExtra("QUICK_ADD_DECK_ID", it.id) }
                    }
                    context.startActivity(intent)
                    onClose()
                }) {
                    Text("Full Edit ↗️")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Box {
                OutlinedTextField(
                    value = selectedDeck?.name ?: "No Deck Available",
                    onValueChange = {},
                    label = { Text("Save to Deck") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().clickable { showDeckMenu = true },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                    enabled = false
                )
                DropdownMenu(
                    expanded = showDeckMenu,
                    onDismissRequest = { showDeckMenu = false }
                ) {
                    decks.forEach { deck ->
                        DropdownMenuItem(
                            text = { Text(deck.name) },
                            onClick = {
                                selectedDeck = deck
                                showDeckMenu = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = frontText,
                onValueChange = { frontText = it },
                label = { Text("Word") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = backText,
                onValueChange = { backText = it },
                label = { Text("Meaning") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onClose) { Text("Cancel") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (selectedDeck != null && frontText.isNotBlank()) {
                            val newCard = com.example.mindcard.data.Card(
                                id = java.util.UUID.randomUUID().toString(),
                                englishWord = frontText.trim(),
                                definition = backText.trim(),
                                pronunciation = "",
                                pos = "Noun",
                                exampleSentence = "",
                                synonyms = ""
                            )
                            com.example.mindcard.data.Database.addCardToDeck(selectedDeck!!.id, newCard)
                        }
                        onClose()
                    },
                    enabled = selectedDeck != null && frontText.isNotBlank()
                ) {
                    Text("Save")
                }
            }
        }
    }
}
package com.example.mindcard.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import com.example.mindcard.CreateCard
import com.example.mindcard.CreateDeck
import com.example.mindcard.FlashcardStudy
import com.example.mindcard.data.Deck
import com.example.mindcard.data.FsrsAlgorithm

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mindcard.ui.viewmodel.HomeViewModel

@Composable
fun LessonScreen(
    onItemClick: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val decks = viewModel.decks

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Select Lesson", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)

        if (decks.isEmpty()) {
            EmptyStateCard(
                title = "No sets available to study",
                subtitle = "Create a set first to start learning vocabulary.",
                buttonText = "Create Set",
                onClick = { onItemClick(CreateDeck()) },
                onSeedClick = { viewModel.seedDemoData() }
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(decks) { deck ->
                    var showMenu by remember { mutableStateOf(false) }

                    Box {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {
                                        if (deck.cards.isNotEmpty()) {
                                            onItemClick(FlashcardStudy(deck.id))
                                        } else {
                                            onItemClick(CreateCard(deck.id))
                                        }
                                    },
                                    onLongClick = {
                                        showMenu = true
                                    }
                                )
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(SecondaryGreen.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = SecondaryGreen,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(deck.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("${deck.cards.size} Cards", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        val dueCount = FsrsAlgorithm.getDueCardsCount(deck.cards)
                                        if (dueCount > 0) {
                                            Text("$dueCount due", fontSize = 12.sp, color = Color(0xFFFF6B6B), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                if (deck.cards.isEmpty()) {
                                    Text("Add Cards", color = PrimaryIndigo, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                } else {
                                    Text("${deck.masteredPercentage}% Mastery", color = SecondaryGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Sửa đổi") },
                                onClick = {
                                    showMenu = false
                                    onItemClick(CreateDeck(deck.id))
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Xóa", color = Color.Red) },
                                onClick = {
                                    showMenu = false
                                    viewModel.deleteDeck(deck.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

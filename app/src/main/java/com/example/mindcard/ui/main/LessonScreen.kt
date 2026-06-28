package com.example.mindcard.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import com.example.mindcard.CreateCard
import com.example.mindcard.CreateDeck
import com.example.mindcard.FlashcardStudy
import com.example.mindcard.data.Database
import com.example.mindcard.data.Deck
import com.example.mindcard.data.FsrsAlgorithm
import com.example.mindcard.ui.screens.*

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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Select Lesson", fontSize = 20.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = Color(0xFF191C1E))

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
                    Card(
                        onClick = {
                            if (deck.cards.isNotEmpty()) {
                                onItemClick(FlashcardStudy(deck.id))
                            } else {
                                onItemClick(CreateCard(deck.id))
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, OutlineVariantColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                                    .background(SecondaryGreen.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
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
                                Text(deck.name, fontSize = 16.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = Color(0xFF191C1E))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("${deck.cards.size} Cards", fontSize = 12.sp, color = OutlineColor)
                                    val dueCount = FsrsAlgorithm.getDueCardsCount(deck.cards)
                                    if (dueCount > 0) {
                                        Text("$dueCount due", fontSize = 12.sp, color = Color(0xFFFF6B6B), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            if (deck.cards.isEmpty()) {
                                Text("Add Cards", color = PrimaryIndigo, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 13.sp)
                            } else {
                                Text("${deck.masteredPercentage}% Mastery", color = SecondaryGreen, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

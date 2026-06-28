package com.example.mindcard.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import com.example.mindcard.CreateCard
import com.example.mindcard.CreateDeck
import com.example.mindcard.FlashcardStudy
import com.example.mindcard.data.Database
import com.example.mindcard.data.Deck
import com.example.mindcard.ui.screens.*

@Composable
fun HomeScreen(
    decks: List<Deck>,
    onItemClick: (NavKey) -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Daily Goals Section
        Text("Daily Goals", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191C1E))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Words learned goal card
            GoalCard(
                title = "Words Learned",
                value = "${Database.userProfile.value.totalWordsLearned}",
                goal = "/ 30",
                progress = if (Database.userProfile.value.totalWordsLearned > 0) {
                    minOf(1f, Database.userProfile.value.totalWordsLearned / 30f)
                } else 0f,
                color = PrimaryIndigo,
                modifier = Modifier.weight(1f)
            )
            // Time spent goal card
            GoalCard(
                title = "Time Spent",
                value = "${Database.currentSessionTime}",
                goal = "min",
                progress = if (Database.currentSessionTime > 0) {
                    minOf(1f, Database.currentSessionTime / 15f)
                } else 0f,
                color = SecondaryGreen,
                modifier = Modifier.weight(1f)
            )
        }

        // Your Decks Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text("Your Flashcard Sets", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191C1E))
            if (decks.isNotEmpty()) {
                Text(
                    text = "Seed Demo",
                    color = PrimaryIndigo,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { Database.seedDemoData() }
                )
            }
        }

        if (decks.isEmpty()) {
            EmptyStateCard(
                title = "No flashcard sets yet",
                subtitle = "Start by creating a deck manually or using AI.",
                buttonText = "Create Deck",
                onClick = { onItemClick(CreateDeck) },
                onSeedClick = { Database.seedDemoData() }
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                decks.forEach { deck ->
                    DeckCard(deck = deck, onClick = {
                        if (deck.cards.isNotEmpty()) {
                            onItemClick(FlashcardStudy(deck.id))
                        } else {
                            onItemClick(CreateCard(deck.id))
                        }
                    })
                }
            }
        }
    }
}

@Composable
fun GoalCard(
    title: String,
    value: String,
    goal: String,
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.dp, OutlineVariantColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = OutlineColor)
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191C1E))
                Text(goal, fontSize = 14.sp, color = OutlineColor, modifier = Modifier.padding(bottom = 4.dp))
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = color,
                trackColor = OutlineVariantColor.copy(alpha = 0.3f),
                strokeCap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun DeckCard(
    deck: Deck,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
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
                    .size(48.dp)
                    .background(PrimaryIndigo.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = PrimaryIndigo,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(deck.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191C1E))
                Text("${deck.cards.size} Cards", fontSize = 13.sp, color = OutlineColor)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("${deck.masteredPercentage}% Mastered", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SecondaryGreen)
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { deck.masteredPercentage / 100f },
                    modifier = Modifier
                        .width(80.dp)
                        .height(6.dp)
                        .clip(CircleShape),
                    color = SecondaryGreen,
                    trackColor = OutlineVariantColor.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    title: String,
    subtitle: String,
    buttonText: String,
    onClick: () -> Unit,
    onSeedClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(20.dp))
            .border(2.dp, OutlineVariantColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("🗂️", fontSize = 48.sp)
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191C1E))
        Text(subtitle, fontSize = 14.sp, color = OutlineColor, textAlign = TextAlign.Center)

        Button(
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
        ) {
            Text(buttonText, fontWeight = FontWeight.Bold)
        }

        Text(
            text = "Seed Demo Decks",
            color = PrimaryIndigo,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clickable(onClick = onSeedClick)
                .padding(top = 8.dp)
        )
    }
}

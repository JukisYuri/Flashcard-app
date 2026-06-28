package com.example.mindcard.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.mindcard.data.Deck
import com.example.mindcard.ui.screens.*

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mindcard.ui.viewmodel.HomeViewModel

// Colors updated to match the provided screen14.png design
val PrimaryIndigo = Color(0xFF4648D4)
val SecondaryGreen = Color(0xFF2ECC71)
val DarkGreen = Color(0xFF007A33)
val BackgroundFrost = Color(0xFFF7F9FB)
val OutlineColor = Color(0xFF767586)
val OutlineVariantColor = Color(0xFFE5E7EB)

@Composable
fun HomeScreen(
    onItemClick: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val scrollState = rememberScrollState()
    val userProfile by viewModel.userProfile
    val decks = viewModel.decks

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundFrost)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        // --- Daily Goals Section ---
        Text(
            text = "Daily Goals",
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF191C1E)
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Words learned goal card
            val wordsLearned = userProfile.totalWordsLearned
            val wordsGoal = 30
            val progressWords = if (wordsGoal > 0) minOf(1f, wordsLearned.toFloat() / wordsGoal) else 0f

            GoalCardCircular(
                title = "WORDS LEARNED",
                value = "$wordsLearned",
                goal = "/ $wordsGoal",
                progress = progressWords,
                progressColor = PrimaryIndigo,
                isTimer = false
            )

            // Time spent goal card
            val timeSpent = viewModel.currentSessionTime
            val timeGoal = 15f
            val progressTime = if (timeSpent > 0) minOf(1f, timeSpent / timeGoal) else 0f

            GoalCardCircular(
                title = "TIME SPENT",
                value = "$timeSpent",
                goal = "min",
                progress = progressTime,
                progressColor = DarkGreen,
                isTimer = true
            )
        }

        // --- Your Flashcard Sets Section ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Your Flashcard Sets",
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF191C1E)
            )
            Text(
                text = "Show more",
                color = PrimaryIndigo,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.clickable { /* TODO: Navigate to library */ }
            )
        }

        if (decks.isEmpty()) {
            EmptyStateCard(
                title = "No flashcard sets yet",
                subtitle = "Start by creating a deck manually or using AI.",
                buttonText = "Create Deck",
                onClick = { onItemClick(CreateDeck()) },
                onSeedClick = { viewModel.seedDemoData() }
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                decks.forEachIndexed { index, deck ->
                    DeckCardVertical(
                        deck = deck,
                        isHot = index == 0, // Set the first one as "Hot" for demo purpose
                        onClick = {
                            if (deck.cards.isNotEmpty()) {
                                onItemClick(FlashcardStudy(deck.id))
                            } else {
                                onItemClick(CreateCard(deck.id))
                            }
                        }
                    )
                }
            }
        }

        // Spacer to prevent FAB from overlapping content
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun GoalCardCircular(
    title: String,
    value: String,
    goal: String,
    progress: Float,
    progressColor: Color,
    isTimer: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF374151),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = value,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF111827),
                        lineHeight = 44.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = goal,
                        fontSize = 20.sp,
                        color = Color(0xFF6B7280),
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(90.dp)
            ) {
                // Background Track
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = OutlineVariantColor,
                    strokeWidth = 8.dp,
                    strokeCap = StrokeCap.Round
                )
                // Active Progress
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    color = progressColor,
                    strokeWidth = 8.dp,
                    strokeCap = StrokeCap.Round
                )

                if (isTimer) {
                    Text(
                        text = "min",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = progressColor
                    )
                } else {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = progressColor
                    )
                }
            }
        }
    }
}

@Composable
fun DeckCardVertical(
    deck: Deck,
    isHot: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Row: Avatar & Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Avatar Badge (First letter of deck name)
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .background(Color(0xFFE5E4FF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = deck.name.take(1).uppercase(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryIndigo
                    )
                }

                if (isHot) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFFE082), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Hot",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF424242)
                        )
                    }
                }
            }

            // Middle Text Info
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = deck.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF191C1E)
                )
                Text(
                    text = "${deck.cards.size} Cards total",
                    fontSize = 15.sp,
                    color = OutlineColor
                )
            }

            // Bottom Progress Bar
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(top = 12.dp)
            ) {
                val progressValue = deck.masteredPercentage / 100f
                LinearProgressIndicator(
                    progress = { progressValue },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = SecondaryGreen,
                    trackColor = OutlineVariantColor,
                    strokeCap = StrokeCap.Round
                )
                Text(
                    text = "${deck.masteredPercentage}% Mastered",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkGreen,
                    modifier = Modifier.align(Alignment.End)
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
            .background(Color.White, RoundedCornerShape(24.dp))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191C1E))
        Text(subtitle, fontSize = 14.sp, color = OutlineColor, textAlign = TextAlign.Center)

        Button(
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
            modifier = Modifier.padding(top = 8.dp)
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
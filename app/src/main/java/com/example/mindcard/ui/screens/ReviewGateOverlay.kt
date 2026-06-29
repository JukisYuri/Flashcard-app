package com.example.mindcard.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mindcard.data.CardState
import com.example.mindcard.data.Database
import com.example.mindcard.data.FsrsAlgorithm
import com.example.mindcard.data.Rating
import com.example.mindcard.data.ReviewState
import com.example.mindcard.ui.main.PrimaryIndigo
import com.example.mindcard.ui.main.SecondaryGreen

@Composable
fun ReviewGateOverlay(
    onDismiss: () -> Unit,
    onFinish: (accuracy: Int, xp: Int, cardsReviewed: Int) -> Unit
) {
    val decks = Database.decks
    val allDueCards = decks.flatMap { deck ->
        deck.cards.filter { card ->
            val state = CardState(
                easeFactor = card.easeFactor,
                interval = card.interval,
                repetitions = card.repetitions,
                nextReview = card.nextReview,
                lastReview = card.lastReview,
                state = ReviewState.entries.find { it.name == card.reviewState } ?: ReviewState.New
            )
            FsrsAlgorithm.isDue(state)
        }.map { deck.id to it }
    }
    val dueCards = remember(allDueCards) { allDueCards.take(5) }

    val reviewTarget = minOf(3, dueCards.size)
    var currentIndex by remember { mutableIntStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }
    var totalAccuracy by remember { mutableIntStateOf(0) }
    var totalXp by remember { mutableIntStateOf(0) }

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f
    )

    if (dueCards.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .clickable(enabled = false) {},
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("\uD83C\uDF1F", fontSize = 48.sp)
                Text(
                    "No cards to review yet!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "All caught up. Come back later!",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Button(
                    onClick = { onFinish(100, 0, 0) },
                    modifier = Modifier.padding(top = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                ) {
                    Text("Continue", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 24.dp))
                }
            }
        }
        return
    }

    if (currentIndex >= reviewTarget) {
        LaunchedEffect(Unit) {
            onFinish(
                if (reviewTarget > 0) totalAccuracy / reviewTarget else 100,
                totalXp,
                currentIndex
            )
        }
        return
    }

    val currentPair = dueCards[currentIndex]
    val currentDeckId = currentPair.first
    val currentCard = currentPair.second
    val progress = currentIndex.toFloat() / reviewTarget

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable(enabled = false) {}
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Quick Review",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Text(
                    text = "Review $reviewTarget cards to continue",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = SecondaryGreen,
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
                Text(
                    text = "CARD ${currentIndex + 1} OF $reviewTarget",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 32.dp)
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 12f * density
                    }
                    .clickable { isFlipped = !isFlipped }
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                    .border(2.dp, PrimaryIndigo.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (rotation <= 90f) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        if (currentCard.pos.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .background(PrimaryIndigo.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(text = currentCard.pos, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryIndigo)
                            }
                        }
                        Text(
                            text = currentCard.englishWord,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = currentCard.pronunciation,
                            fontSize = 16.sp,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Tap to reveal",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .graphicsLayer { rotationY = 180f }
                            .padding(24.dp)
                    ) {
                        Text(
                            text = currentCard.definition,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryIndigo,
                            textAlign = TextAlign.Center
                        )
                        if (currentCard.exampleSentence.isNotEmpty()) {
                            Text(
                                text = "\"${currentCard.exampleSentence}\"",
                                fontSize = 14.sp,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            if (isFlipped) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GateFeedbackButton(
                        text = "Again",
                        color = Color(0xFFFF6B6B),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val newState = FsrsAlgorithm.nextCardState(
                                currentCard.toCardState(), Rating.Again
                            )
                            Database.updateCardState(currentDeckId, currentCard.updateFromCardState(newState))
                            totalAccuracy = (totalAccuracy * currentIndex + 0) / (currentIndex + 1)
                            totalXp += 5
                            currentIndex++
                            isFlipped = false
                        }
                    )
                    GateFeedbackButton(
                        text = "Hard",
                        color = Color(0xFFFFB800),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val newState = FsrsAlgorithm.nextCardState(
                                currentCard.toCardState(), Rating.Hard
                            )
                            Database.updateCardState(currentDeckId, currentCard.updateFromCardState(newState))
                            totalAccuracy = (totalAccuracy * currentIndex + 50) / (currentIndex + 1)
                            totalXp += 10
                            currentIndex++
                            isFlipped = false
                        }
                    )
                    GateFeedbackButton(
                        text = "Good",
                        color = PrimaryIndigo,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val newState = FsrsAlgorithm.nextCardState(
                                currentCard.toCardState(), Rating.Good
                            )
                            Database.updateCardState(currentDeckId, currentCard.updateFromCardState(newState))
                            totalAccuracy = (totalAccuracy * currentIndex + 80) / (currentIndex + 1)
                            totalXp += 15
                            currentIndex++
                            isFlipped = false
                        }
                    )
                    GateFeedbackButton(
                        text = "Easy",
                        color = SecondaryGreen,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val newState = FsrsAlgorithm.nextCardState(
                                currentCard.toCardState(), Rating.Easy
                            )
                            Database.updateCardState(currentDeckId, currentCard.updateFromCardState(newState))
                            totalAccuracy = (totalAccuracy * currentIndex + 100) / (currentIndex + 1)
                            totalXp += 20
                            currentIndex++
                            isFlipped = false
                        }
                    )
                }
            } else {
                Button(
                    onClick = { isFlipped = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                ) {
                    Text(
                        "FLIP CARD",
                        modifier = Modifier.padding(vertical = 8.dp),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun GateFeedbackButton(
    text: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .border(2.dp, color.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

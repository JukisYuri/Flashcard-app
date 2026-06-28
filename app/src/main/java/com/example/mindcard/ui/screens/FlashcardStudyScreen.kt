package com.example.mindcard.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
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
import androidx.navigation3.runtime.NavKey
import com.example.mindcard.Main
import com.example.mindcard.StudyResult
import com.example.mindcard.data.Database

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardStudyScreen(
    deckId: String,
    onNavigate: (NavKey) -> Unit,
    modifier: Modifier = Modifier
) {
    val deck = Database.decks.firstOrNull { it.id == deckId }
    if (deck == null) {
        onNavigate(Main)
        return
    }

    val cards = deck.cards
    if (cards.isEmpty()) {
        onNavigate(Main)
        return
    }

    var currentIndex by remember { mutableStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }
    var correctAnswers by remember { mutableStateOf(0) }
    val currentCard = cards[currentIndex]

    // Rotate transition for flip card
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 500)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(deck.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PrimaryIndigo)
                        Text("${currentIndex + 1} / ${cards.size}", fontSize = 12.sp, color = OutlineColor)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigate(Main) }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundFrost)
            )
        },
        containerColor = BackgroundFrost
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Progress Bar
            LinearProgressIndicator(
                progress = { (currentIndex + 1).toFloat() / cards.size.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = SecondaryGreen,
                trackColor = OutlineVariantColor.copy(alpha = 0.3f)
            )

            // Category Badge
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color(0xFFE1E0FF))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = currentCard.pos.uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryIndigo
                )
            }

            // Interactive Flipping Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.8f)
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 12 * density
                    }
                    .clickable { isFlipped = !isFlipped }
                    .background(
                        if (rotation > 90f) SuccessBg.copy(alpha = 0.1f) else Color.White,
                        RoundedCornerShape(24.dp)
                    )
                    .border(
                        width = 2.dp,
                        color = if (rotation > 90f) SecondaryGreen else PrimaryIndigo.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (rotation <= 90f) {
                    // Front side
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = currentCard.englishWord,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF191C1E),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = currentCard.pronunciation,
                            fontSize = 18.sp,
                            color = OutlineColor,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Tap to flip",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = OutlineVariantColor,
                            modifier = Modifier.padding(top = 32.dp)
                        )
                    }
                } else {
                    // Back side (needs Y flip back to prevent mirror image)
                    Column(
                        modifier = Modifier.graphicsLayer { rotationY = 180f },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = currentCard.englishWord,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryGreen,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = currentCard.definition,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF191C1E),
                            textAlign = TextAlign.Center
                        )
                        if (currentCard.exampleSentence.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(BackgroundFrost, RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "\"${currentCard.exampleSentence}\"",
                                    fontSize = 14.sp,
                                    fontStyle = FontStyle.Italic,
                                    color = OutlineColor,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            // Controls
            if (!isFlipped) {
                Button(
                    onClick = { isFlipped = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                ) {
                    Text("FLIP CARD", modifier = Modifier.padding(vertical = 8.dp), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Again button (Red)
                    FeedbackButton(
                        text = "Again",
                        containerColor = Color(0xFFFFDAD6),
                        textColor = Color(0xFFBA1A1A),
                        borderColor = Color(0xFFFFDAD6),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            isFlipped = false
                            if (currentIndex < cards.size - 1) {
                                currentIndex++
                            } else {
                                finishSession(deck.id, cards.size, correctAnswers, onNavigate)
                            }
                        }
                    )

                    // Hard button (Yellow)
                    FeedbackButton(
                        text = "Hard",
                        containerColor = Color(0xFFFFE083),
                        textColor = Color(0xFF735C00),
                        borderColor = Color(0xFFFFE083),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            isFlipped = false
                            correctAnswers++
                            if (currentIndex < cards.size - 1) {
                                currentIndex++
                            } else {
                                finishSession(deck.id, cards.size, correctAnswers, onNavigate)
                            }
                        }
                    )

                    // Easy button (Green)
                    FeedbackButton(
                        text = "Easy",
                        containerColor = SuccessBg.copy(alpha = 0.3f),
                        textColor = SecondaryGreen,
                        borderColor = SuccessBg,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            isFlipped = false
                            correctAnswers++
                            if (currentIndex < cards.size - 1) {
                                currentIndex++
                            } else {
                                finishSession(deck.id, cards.size, correctAnswers, onNavigate)
                            }
                        }
                    )
                }
            }
        }
    }
}

private fun finishSession(deckId: String, total: Int, correct: Int, onNavigate: (NavKey) -> Unit) {
    val accuracy = ((correct.toFloat() / total.toFloat()) * 100).toInt()
    val xp = correct * 30 + 100 // 30 XP per correct + 100 completion bonus
    val time = 1 // 1 minute simulation

    Database.recordStudySession(deckId, accuracy, xp, time)
    onNavigate(StudyResult(deckId, accuracy, xp, time))
}

@Composable
fun FeedbackButton(
    text: String,
    containerColor: Color,
    textColor: Color,
    borderColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .background(containerColor, RoundedCornerShape(12.dp))
            .border(2.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

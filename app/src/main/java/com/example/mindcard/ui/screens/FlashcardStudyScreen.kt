package com.example.mindcard.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.example.mindcard.Main
import com.example.mindcard.StudyResult
import com.example.mindcard.data.Database
import com.example.mindcard.ui.main.BackgroundFrost
import com.example.mindcard.ui.main.OutlineColor
import com.example.mindcard.ui.main.OutlineVariantColor
import com.example.mindcard.ui.main.PrimaryIndigo
import com.example.mindcard.ui.main.SecondaryGreen
import com.example.mindcard.ui.viewmodel.StudyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardStudyScreen(
    deckId: String,
    onNavigate: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StudyViewModel = viewModel()
) {
    val deck = remember(deckId, Database.decks) {
        Database.decks.firstOrNull { it.id == deckId }
    }
    if (deck == null) {
        onNavigate(Main)
        return
    }

    LaunchedEffect(deck) {
        viewModel.startStudySession(deck)
    }

    val currentDeck = viewModel.currentDeck
    if (currentDeck == null || currentDeck.cards.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = PrimaryIndigo)
        }
        return
    }

    val cards = currentDeck.cards
    val currentCard = cards[viewModel.currentCardIndex]

    // Rotate transition for flip card
    val rotation by animateFloatAsState(
        targetValue = if (viewModel.isCardFlipped) 180f else 0f
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentDeck.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { onNavigate(Main) }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
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
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Progress indicators
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LinearProgressIndicator(
                    progress = { (viewModel.currentCardIndex.toFloat() / cards.size) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = PrimaryIndigo,
                    trackColor = OutlineVariantColor
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "CARD ${viewModel.currentCardIndex + 1} OF ${cards.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = OutlineColor
                    )
                }
            }

            // Flashcard container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 32.dp)
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 12f * density
                    }
                    .clickable { viewModel.isCardFlipped = !viewModel.isCardFlipped }
                    .background(Color.White, RoundedCornerShape(24.dp))
                    .border(1.dp, OutlineVariantColor.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (rotation <= 90f) {
                    // FRONT SIDE
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(PrimaryIndigo.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = currentCard.pos,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryIndigo
                            )
                        }

                        Text(
                            text = currentCard.englishWord,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF191C1E),
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = currentCard.pronunciation,
                            fontSize = 16.sp,
                            fontStyle = FontStyle.Italic,
                            color = OutlineColor
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Tap to reveal translation",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = OutlineColor.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    // BACK SIDE
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

                        if (currentCard.synonyms.isNotEmpty()) {
                            Text(
                                text = "Synonyms: ${currentCard.synonyms}",
                                fontSize = 14.sp,
                                color = OutlineColor,
                                textAlign = TextAlign.Center
                            )
                        }

                        if (currentCard.exampleSentence.isNotEmpty()) {
                            HorizontalDivider(color = OutlineVariantColor.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 8.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Example:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OutlineColor.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
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
            if (!viewModel.isCardFlipped) {
                Button(
                    onClick = { viewModel.isCardFlipped = true },
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
                            viewModel.handleFeedback("Again") { accuracy, xp ->
                                onNavigate(StudyResult(deckId = deck.id, accuracy = accuracy, xpEarned = xp, timeMinutes = 1))
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
                            viewModel.handleFeedback("Hard") { accuracy, xp ->
                                onNavigate(StudyResult(deckId = deck.id, accuracy = accuracy, xpEarned = xp, timeMinutes = 1))
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
                            viewModel.handleFeedback("Easy") { accuracy, xp ->
                                onNavigate(StudyResult(deckId = deck.id, accuracy = accuracy, xpEarned = xp, timeMinutes = 1))
                            }
                        }
                    )
                }
            }
        }
    }
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

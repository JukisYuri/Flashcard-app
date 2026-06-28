package com.example.mindcard.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
    val dueCardsCount = viewModel.getDueCardsCount()
    val currentCard = cards[viewModel.currentCardIndex]

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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LinearProgressIndicator(
                    progress = { (viewModel.currentCardIndex.toFloat() / cards.size) },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = PrimaryIndigo,
                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        text = "CARD ${viewModel.currentCardIndex + 1} OF ${cards.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (dueCardsCount > 0) {
                        Text(text = "$dueCardsCount DUE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF6B6B))
                    }
                }
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
                    .clickable { viewModel.isCardFlipped = !viewModel.isCardFlipped }
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                    .border(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (rotation <= 90f) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(PrimaryIndigo.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(text = currentCard.pos, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryIndigo)
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

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Tap to reveal translation",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.graphicsLayer { rotationY = 180f }.padding(24.dp)
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
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }

                        if (currentCard.exampleSentence.isNotEmpty()) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Example:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "\"${currentCard.exampleSentence}\"",
                                    fontSize = 14.sp,
                                    fontStyle = FontStyle.Italic,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

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
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FeedbackButton(
                        text = "Again",
                        containerColor = Color(0xFFFF6B6B).copy(alpha = 0.15f),
                        textColor = Color(0xFFFF6B6B),
                        borderColor = Color(0xFFFF6B6B).copy(alpha = 0.3f),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.handleFeedback("Again") { accuracy, xp ->
                                onNavigate(StudyResult(deckId = deck.id, accuracy = accuracy, xpEarned = xp, timeMinutes = 1))
                            }
                        }
                    )
                    FeedbackButton(
                        text = "Hard",
                        containerColor = Color(0xFFFFD93D).copy(alpha = 0.15f),
                        textColor = Color(0xFFFFB800),
                        borderColor = Color(0xFFFFD93D).copy(alpha = 0.3f),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.handleFeedback("Hard") { accuracy, xp ->
                                onNavigate(StudyResult(deckId = deck.id, accuracy = accuracy, xpEarned = xp, timeMinutes = 1))
                            }
                        }
                    )
                    FeedbackButton(
                        text = "Easy",
                        containerColor = Color(0xFF00D68F).copy(alpha = 0.15f),
                        textColor = Color(0xFF00D68F),
                        borderColor = Color(0xFF00D68F).copy(alpha = 0.3f),
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

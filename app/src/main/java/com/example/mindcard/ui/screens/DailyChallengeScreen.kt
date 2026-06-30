package com.example.mindcard.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.mindcard.data.Card
import com.example.mindcard.data.Database
import com.example.mindcard.ui.main.PrimaryIndigo
import com.example.mindcard.ui.main.SecondaryGreen
import com.example.mindcard.ui.main.AccentYellow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyChallengeScreen(
    onNavigate: (NavKey) -> Unit,
    modifier: Modifier = Modifier
) {
    val allCards = remember { Database.decks.flatMap { it.cards }.shuffled().take(5) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }
    var isCompleted by remember { mutableStateOf(false) }
    var answeredCount by remember { mutableIntStateOf(0) }

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f
    )

    LaunchedEffect(isCompleted) {
        if (isCompleted) {
            val accuracy = if (allCards.isNotEmpty()) (score * 100) / allCards.size else 0
            val xp = score * 15
            Database.recordStudySession("daily_challenge", accuracy, xp, 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Challenge", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { onNavigate(Main) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Score badge
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SecondaryGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            "⭐ $score/${allCards.size}",
                            fontWeight = FontWeight.Bold,
                            color = SecondaryGreen,
                            fontSize = 14.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (allCards.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No cards available", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { onNavigate(Main) }) { Text("Go Home") }
                }
            }
            return@Scaffold
        }

        if (isCompleted) {
            // Results
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("🎉", fontSize = 72.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Challenge Complete!", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = PrimaryIndigo)
                Spacer(modifier = Modifier.height(8.dp))
                Text("You got $score out of ${allCards.size} correct!", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(24.dp))

                val accuracy = if (allCards.isNotEmpty()) (score * 100) / allCards.size else 0
                val xp = score * 15

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatBox("Accuracy", "$accuracy%", "🎯", SecondaryGreen, Modifier.weight(1f))
                    StatBox("XP Earned", "+$xp", "⚡", AccentYellow, Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            currentIndex = 0; isFlipped = false; score = 0; isCompleted = false; answeredCount = 0
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Retry", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onNavigate(Main) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                    ) {
                        Text("Home", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Challenge card
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Progress
                Column(modifier = Modifier.fillMaxWidth()) {
                    LinearProgressIndicator(
                        progress = { (currentIndex.toFloat() / allCards.size) },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = PrimaryIndigo,
                        trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Card ${currentIndex + 1} of ${allCards.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Card
                val card = allCards[currentIndex]
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 24.dp)
                        .graphicsLayer {
                            rotationY = rotation
                            cameraDistance = 12f * density
                        }
                        .clickable { isFlipped = !isFlipped }
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
                                modifier = Modifier.background(PrimaryIndigo.copy(alpha = 0.15f), RoundedCornerShape(8.dp)).padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(card.pos, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryIndigo)
                            }
                            Text(card.englishWord, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
                            Text(card.pronunciation, fontSize = 16.sp, fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Tap to reveal", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.graphicsLayer { rotationY = 180f }.padding(24.dp)
                        ) {
                            Text(card.definition, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = PrimaryIndigo, textAlign = TextAlign.Center)
                            if (card.exampleSentence.isNotEmpty()) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))
                                Text("\"${card.exampleSentence}\"", fontSize = 14.sp, fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }

                // Answer buttons
                if (!isFlipped) {
                    Button(
                        onClick = { isFlipped = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                    ) {
                        Text("REVEAL", modifier = Modifier.padding(vertical = 8.dp), fontSize = 16.sp, fontWeight = FontWeight.Bold)
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
                                answeredCount++
                                if (currentIndex < allCards.size - 1) { currentIndex++; isFlipped = false } else { isCompleted = true }
                            }
                        )
                        FeedbackButton(
                            text = "Hard",
                            containerColor = Color(0xFFFFD93D).copy(alpha = 0.15f),
                            textColor = Color(0xFFFFB800),
                            borderColor = Color(0xFFFFD93D).copy(alpha = 0.3f),
                            modifier = Modifier.weight(1f),
                            onClick = {
                                answeredCount++; score++
                                if (currentIndex < allCards.size - 1) { currentIndex++; isFlipped = false } else { isCompleted = true }
                            }
                        )
                        FeedbackButton(
                            text = "Easy",
                            containerColor = Color(0xFF00D68F).copy(alpha = 0.15f),
                            textColor = Color(0xFF00D68F),
                            borderColor = Color(0xFF00D68F).copy(alpha = 0.3f),
                            modifier = Modifier.weight(1f),
                            onClick = {
                                answeredCount++; score++
                                if (currentIndex < allCards.size - 1) { currentIndex++; isFlipped = false } else { isCompleted = true }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatBox(label: String, value: String, icon: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

package com.example.mindcard.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import com.example.mindcard.CreateCard
import com.example.mindcard.CreateDeck
import com.example.mindcard.DailyChallenge
import com.example.mindcard.FlashcardStudy
import com.example.mindcard.data.Deck
import com.example.mindcard.data.FsrsAlgorithm
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.platform.LocalContext

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mindcard.data.sync.SyncManager
import com.example.mindcard.ui.viewmodel.HomeViewModel
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

val PrimaryIndigo = Color(0xFF6C63FF)
val SecondaryGreen = Color(0xFF00D68F)
val DarkGreen = Color(0xFF00B377)
val AccentYellow = Color(0xFFFFD93D)
val BackgroundFrost = Color(0xFFF8F9FE)
val OutlineColor = Color(0xFF6B7280)
val OutlineVariantColor = Color(0xFFE5E7EB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onItemClick: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val userProfile by viewModel.userProfile
    val decks = viewModel.decks
    val totalDueCards = viewModel.getTotalDueCards()
    var deckToDelete by remember { mutableStateOf<Deck?>(null) }

    val coroutineScope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            coroutineScope.launch {
                com.example.mindcard.data.Database.syncNowSuspend()
                isRefreshing = false
            }
        },
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
        Text(
            text = "Daily Goals",
            fontSize = 22.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GoalCardCircular(
                title = "DUE CARDS",
                value = "$totalDueCards",
                goal = "cards",
                progress = if (totalDueCards > 0) minOf(1f, totalDueCards.toFloat() / 20) else 0f,
                progressColor = Color(0xFFFF6B6B),
                isTimer = false
            )

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

        // Word of the Day
        val allCards = decks.flatMap { it.cards }
        val wordOfTheDay = if (allCards.isNotEmpty()) allCards.random() else null
        if (wordOfTheDay != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryIndigo.copy(alpha = 0.1f)),
                border = ButtonDefaults.outlinedButtonBorder(enabled = true)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("✨", fontSize = 18.sp)
                        Text("Word of the Day", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PrimaryIndigo)
                    }
                    Text(
                        text = wordOfTheDay.englishWord,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = wordOfTheDay.pronunciation,
                        fontSize = 14.sp,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = wordOfTheDay.definition,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (wordOfTheDay.exampleSentence.isNotEmpty()) {
                        Text(
                            text = "\"${wordOfTheDay.exampleSentence}\"",
                            fontSize = 13.sp,
                            fontStyle = FontStyle.Italic,
                            color = PrimaryIndigo.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Daily Challenge Button
        Card(
            onClick = { onItemClick(DailyChallenge) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SecondaryGreen.copy(alpha = 0.15f)),
            border = ButtonDefaults.outlinedButtonBorder(enabled = true)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(SecondaryGreen.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⚡", fontSize = 24.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Daily Challenge", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("Test your vocabulary with 5 random words!", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SecondaryGreen)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Your Flashcard Sets",
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Show more",
                color = PrimaryIndigo,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.clickable { }
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
        }  else {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            decks.forEachIndexed { index, deck ->
                DeckCardVertical(
                    deck = deck,
                    isHot = index == 0,
                    onClick = {
                        if (deck.cards.isNotEmpty()) {
                            onItemClick(FlashcardStudy(deck.id))
                        } else {
                            onItemClick(CreateCard(deck.id))
                        }
                    },
                    onDeleteClick = { deckToDelete = deck }
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(80.dp))
    }
    }

    if (deckToDelete != null) {
        AlertDialog(
            onDismissRequest = { deckToDelete = null },
            title = { Text("Delete Deck?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${deckToDelete?.name}'? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        deckToDelete?.let { deck ->
                            val syncManager = SyncManager(context)
                            viewModel.deleteDeckPermanently(deck.id, syncManager)
                        }
                        deckToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA1A1A))
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { deckToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
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
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = value,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 44.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = goal,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(90.dp)
            ) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    strokeWidth = 8.dp,
                    strokeCap = StrokeCap.Round
                )
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
        onClick: () -> Unit,
        onDeleteClick: () -> Unit
    ) {
        Card(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(PrimaryIndigo.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = deck.name.take(1).uppercase(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryIndigo
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isHot) {
                            Box(
                                modifier = Modifier
                                    .background(AccentYellow.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Hot",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentYellow
                                )
                            }
                        }

                        // Nút X (Delete)
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Delete Deck",
                                tint = OutlineColor
                            )
                        }
                    }
                }

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = deck.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${deck.cards.size} Cards total",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val dueCount = FsrsAlgorithm.getDueCardsCount(deck.cards)
                    if (dueCount > 0) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFF6B6B).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "$dueCount due",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF6B6B)
                            )
                        }
                    }
                }
            }

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
                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    strokeCap = StrokeCap.Round
                )
                Text(
                    text = "${deck.masteredPercentage}% Mastered",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryGreen,
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
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text(subtitle, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)

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

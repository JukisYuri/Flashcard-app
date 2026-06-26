package com.example.mindcard.ui.main

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import com.example.mindcard.CreateAI
import com.example.mindcard.CreateDeck
import com.example.mindcard.CreateCard
import com.example.mindcard.FlashcardStudy
import com.example.mindcard.data.Card
import com.example.mindcard.data.Database
import com.example.mindcard.data.Deck
import com.example.mindcard.ui.screens.*


enum class ActiveTab { Home, Lesson, Library, Progress, Profile }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onItemClick: (NavKey) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(ActiveTab.Home) }
    val profile by remember { Database.userProfile }
    val decksList = Database.decks

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(PrimaryIndigo, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🚀", fontSize = 18.sp)
                        }
                        Text(
                            text = "Mind Card",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = PrimaryIndigo
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFFFFE083).copy(alpha = 0.2f))
                            .border(1.dp, Color(0xFFFFE083), CircleShape)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${profile.currentStreak} 🔥",
                            fontWeight = FontWeight.Bold,
                            color = TertiaryYellow,
                            fontSize = 14.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundFrost.copy(alpha = 0.8f)
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                val items = listOf(
                    Triple(ActiveTab.Home, Icons.Default.Home, "Home"),
                    Triple(ActiveTab.Lesson, Icons.Default.PlayArrow, "Lesson"),
                    Triple(ActiveTab.Library, Icons.Default.List, "Library"),
                    Triple(ActiveTab.Progress, Icons.Default.Star, "Progress"),
                    Triple(ActiveTab.Profile, Icons.Default.Person, "Profile")
                )
                items.forEach { (tab, icon, label) ->
                    NavigationBarItem(
                        selected = activeTab == tab,
                        onClick = { activeTab = tab },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryIndigo,
                            selectedTextColor = PrimaryIndigo,
                            unselectedIconColor = OutlineColor,
                            unselectedTextColor = OutlineColor,
                            indicatorColor = Color(0xFFE1E0FF)
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            if (activeTab == ActiveTab.Home || activeTab == ActiveTab.Lesson) {
                FloatingActionButton(
                    onClick = { onItemClick(CreateDeck) },
                    containerColor = PrimaryIndigo,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Deck", modifier = Modifier.size(32.dp))
                }
            }
        },
        containerColor = BackgroundFrost
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                ActiveTab.Home -> HomeScreen(decksList, onItemClick)
                ActiveTab.Lesson -> LessonScreen(decksList, onItemClick)
                ActiveTab.Library -> LibraryScreen()
                ActiveTab.Progress -> ProgressScreen()
                ActiveTab.Profile -> ProfileScreen()
            }
        }
    }
}

// ---------------------------------------------------------
// 1. HOME SCREEN
// ---------------------------------------------------------
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
            // Circular Indicator or Linear Progress
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

// ---------------------------------------------------------
// 2. LESSON SCREEN
// ---------------------------------------------------------
@Composable
fun LessonScreen(
    decks: List<Deck>,
    onItemClick: (NavKey) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Select Lesson", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191C1E))

        if (decks.isEmpty()) {
            EmptyStateCard(
                title = "No sets available to study",
                subtitle = "Create a set first to start learning vocabulary.",
                buttonText = "Create Set",
                onClick = { onItemClick(CreateDeck) },
                onSeedClick = { Database.seedDemoData() }
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
                                Text(deck.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191C1E))
                                Text("${deck.cards.size} Cards", fontSize = 12.sp, color = OutlineColor)
                            }
                            if (deck.cards.isEmpty()) {
                                Text("Add Cards", color = PrimaryIndigo, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            } else {
                                Text("${deck.masteredPercentage}% Mastery", color = SecondaryGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------
// 3. LIBRARY SCREEN
// ---------------------------------------------------------
@Composable
fun LibraryScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var selectedLetter by remember { mutableStateOf<Char?>(null) }

    // Aggregate all cards from all decks
    val allCards = remember { Database.decks.flatMap { deck -> deck.cards } }

    val filteredCards = allCards.filter { card ->
        val matchesQuery = card.englishWord.contains(searchQuery, ignoreCase = true) ||
                card.definition.contains(searchQuery, ignoreCase = true)
        val matchesLetter = selectedLetter == null ||
                card.englishWord.startsWith(selectedLetter.toString(), ignoreCase = true)
        matchesQuery && matchesLetter
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("My Library", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191C1E))
        Text("Review all ${allCards.size} words you've learned so far.", fontSize = 14.sp, color = OutlineColor)

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search words, definitions...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = OutlineVariantColor.copy(alpha = 0.5f),
                focusedBorderColor = PrimaryIndigo
            )
        )

        // Alphabet quick filter list
        val letters = ('A'..'Z').toList()
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (selectedLetter == null) PrimaryIndigo else OutlineVariantColor.copy(alpha = 0.2f))
                        .clickable { selectedLetter = null }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        "All",
                        color = if (selectedLetter == null) Color.White else OutlineColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            items(letters) { letter ->
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (selectedLetter == letter) PrimaryIndigo else OutlineVariantColor.copy(alpha = 0.2f))
                        .clickable { selectedLetter = if (selectedLetter == letter) null else letter }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        letter.toString(),
                        color = if (selectedLetter == letter) Color.White else OutlineColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (filteredCards.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (allCards.isEmpty()) "No words in your library yet.\nStart learning a deck to add words!" else "No search results matches.",
                    textAlign = TextAlign.Center,
                    color = OutlineColor,
                    fontSize = 15.sp
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredCards) { card ->
                    LibraryWordCard(card = card)
                }
            }
        }
    }
}

@Composable
fun LibraryWordCard(card: Card) {
    Box(
        modifier = Modifier
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.dp, OutlineVariantColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(PrimaryIndigo.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(card.pos, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PrimaryIndigo)
            }

            Text(card.englishWord, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191C1E))
            Text(card.pronunciation, fontSize = 12.sp, fontStyle = FontStyle.Italic, color = OutlineColor)

            Divider(color = OutlineVariantColor.copy(alpha = 0.3f))

            Text(card.definition, fontSize = 13.sp, color = OutlineColor, maxLines = 3, overflow = TextOverflow.Ellipsis)
            if (card.exampleSentence.isNotEmpty()) {
                Text(
                    "\"${card.exampleSentence}\"",
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic,
                    color = OutlineColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}

// ---------------------------------------------------------
// 4. PROGRESS SCREEN
// ---------------------------------------------------------
@Composable
fun ProgressScreen() {
    val profile = Database.userProfile.value
    var streakRestoreActive by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "My Progress",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF191C1E),
            modifier = Modifier.align(Alignment.Start)
        )

        // Streak badge container
        Box(
            modifier = Modifier
                .size(160.dp)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFFE083),
                            Color(0xFFFFAD33)
                        )
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🔥", fontSize = 56.sp)
                Text(
                    text = "${profile.currentStreak}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF231B00)
                )
                Text("Days Streak", fontSize = 12.sp, color = Color(0xFF231B00).copy(alpha = 0.7f))
            }
        }

        Text(
            text = "You're on fire!",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF191C1E)
        )

        Text(
            text = "${profile.currentStreak} days learning streak. Keep it up!",
            fontSize = 15.sp,
            color = OutlineColor,
            textAlign = TextAlign.Center
        )

        // Interactive Calendar Widget
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(24.dp))
                .border(1.dp, OutlineVariantColor.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Study Activity", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191C1E))
                Text("June 2026", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PrimaryIndigo)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Calendar days headers
            val daysHeaders = listOf("S", "M", "T", "W", "T", "F", "S")
            Row(modifier = Modifier.fillMaxWidth()) {
                daysHeaders.forEach {
                    Text(
                        it,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color = OutlineColor,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar days layout (Simulating 30 days of June 2026 starting on Monday)
            // Empty start offset: June 2026 starts on Monday. Offset is 1 (Sunday is empty)
            val totalDays = 30
            val startOffset = 1
            val cells = List(startOffset) { null } + (1..totalDays).toList()

            val chunked = cells.chunked(7)
            chunked.forEach { week ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    week.forEach { day ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (day == null) Color.Transparent
                                    else if (day % 4 == 0) SuccessBg.copy(alpha = 0.3f) // studied days
                                    else if (day == 26) PrimaryIndigo.copy(alpha = 0.2f) // today
                                    else Color(0xFFF2F4F6)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (day == 26) PrimaryIndigo else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (day != null) {
                                Text(
                                    day.toString(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (day % 4 == 0) SecondaryGreen else if (day == 26) PrimaryIndigo else Color(0xFF191C1E)
                                )
                            }
                        }
                    }
                    // pad out shorter rows at the end
                    if (week.size < 7) {
                        Spacer(modifier = Modifier.weight((7 - week.size).toFloat()))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        if (streakRestoreActive) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .border(1.dp, OutlineVariantColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text("🛡️", fontSize = 24.sp, modifier = Modifier.padding(end = 8.dp))
                    Text("Streak Shield Active! You missed yesterday.", fontSize = 13.sp, color = OutlineColor)
                }
                Button(
                    onClick = {
                        Database.userProfile.value = profile.copy(currentStreak = profile.currentStreak + 1)
                        streakRestoreActive = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Restore", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ---------------------------------------------------------
// 5. PROFILE SCREEN
// ---------------------------------------------------------
@Composable
fun ProfileScreen() {
    val profile = Database.userProfile.value
    var isEditingName by remember { mutableStateOf(false) }
    var editNameInput by remember { mutableStateOf(profile.name) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("My Profile", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191C1E))

        // Hero Info Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(24.dp))
                .border(1.dp, OutlineVariantColor.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(PrimaryIndigo.copy(alpha = 0.1f))
                        .border(2.dp, PrimaryIndigo, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🧑‍🎓", fontSize = 48.sp)
                }

                Text(profile.name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191C1E))
                Text(profile.title, fontSize = 14.sp, color = OutlineColor)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BackgroundFrost, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    ProfileStatItem("XP", "${profile.totalXp}")
                    Divider(modifier = Modifier.width(1.dp).height(24.dp).align(Alignment.CenterVertically))
                    ProfileStatItem("Words", "${profile.totalWordsLearned}")
                    Divider(modifier = Modifier.width(1.dp).height(24.dp).align(Alignment.CenterVertically))
                    ProfileStatItem("Streak", "${profile.currentStreak}")
                }

                Button(
                    onClick = { isEditingName = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Edit Profile", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Weekly activity bar chart
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(24.dp))
                .border(1.dp, OutlineVariantColor.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            Text("Weekly Activity", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191C1E))
            Text("Words learned this week", fontSize = 12.sp, color = OutlineColor)

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Bottom
            ) {
                // Mon to Sun bars (Tue is 45 xp today, other placeholder bars are small)
                val days = listOf("M" to 10, "T" to 45, "W" to 20, "T" to 30, "F" to 15, "S" to 5, "S" to 25)
                days.forEach { (name, heightVal) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Box(
                            modifier = Modifier
                                .width(16.dp)
                                .height((heightVal * 1.5).dp)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(if (name == "T") PrimaryIndigo else PrimaryIndigo.copy(alpha = 0.2f))
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = OutlineColor)
                    }
                }
            }
        }

        // Badges grid
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(24.dp))
                .border(1.dp, OutlineVariantColor.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            Text("Badges", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191C1E))
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                BadgeCircle("🔥", "Streak Lord", unlocked = profile.currentStreak > 0)
                BadgeCircle("🧠", "Vocab Master", unlocked = profile.totalWordsLearned > 0)
                BadgeCircle("⚡", "XP Earned", unlocked = profile.totalXp > 0)
                BadgeCircle("🔒", "Super Learner", unlocked = false)
            }
        }
    }

    if (isEditingName) {
        AlertDialog(
            onDismissRequest = { isEditingName = false },
            title = { Text("Edit Name") },
            text = {
                OutlinedTextField(
                    value = editNameInput,
                    onValueChange = { editNameInput = it },
                    label = { Text("Display Name") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        Database.updateProfileName(editNameInput)
                        isEditingName = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { isEditingName = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ProfileStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryIndigo)
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = OutlineColor)
    }
}

@Composable
fun BadgeCircle(emoji: String, name: String, unlocked: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(if (unlocked) Color(0xFFFFE083).copy(alpha = 0.4f) else Color(0xFFF2F4F6))
                .border(2.dp, if (unlocked) Color(0xFFFFE083) else Color.Transparent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(if (unlocked) emoji else "🔒", fontSize = 24.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(name, fontSize = 10.sp, color = OutlineColor, maxLines = 1, fontWeight = FontWeight.Bold)
    }
}

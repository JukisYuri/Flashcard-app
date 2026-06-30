package com.example.mindcard.ui.main

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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.example.mindcard.CreateAI
import com.example.mindcard.CreateDeck
import com.example.mindcard.CreateCard
import com.example.mindcard.DailyChallenge
import com.example.mindcard.FlashcardStudy
import com.example.mindcard.Leaderboard
import com.example.mindcard.Login
import com.example.mindcard.Settings
import com.example.mindcard.data.Database
import com.example.mindcard.ui.screens.*
import com.example.mindcard.ui.viewmodel.HomeViewModel
enum class ActiveTab { Home, Lesson, Library, Progress, Profile }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onItemClick: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    var activeTab by remember { mutableStateOf(ActiveTab.Home) }
    val profile by viewModel.userProfile
    var showSyncDialog by remember { mutableStateOf(false) }
    val isOnline = remember { mutableStateOf<Boolean>(Database.isOnline()) }

    val dueCount = viewModel.getTotalDueCards()

    val lifecycleOwner = LocalLifecycleOwner.current


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
                            Text("\uD83D\uDE80", fontSize = 18.sp)
                        }
                        Text(
                            text = "Mind Card",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    if (dueCount > 0 && !viewModel.showReviewGate) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(SecondaryGreen.copy(alpha = 0.15f))
                                .border(1.dp, SecondaryGreen, RoundedCornerShape(20.dp))
                                .clickable { viewModel.openReviewGate() }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("\u26A1", fontSize = 14.sp)
                                Text(
                                    text = "$dueCount",
                                    fontWeight = FontWeight.Bold,
                                    color = SecondaryGreen,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isOnline.value) Color(0xFF00D68F).copy(alpha = 0.2f) else Color(0xFFFF6B6B).copy(alpha = 0.2f))
                            .border(1.dp, if (isOnline.value) Color(0xFF00D68F) else Color(0xFFFF6B6B), CircleShape)
                            .clickable { showSyncDialog = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        if (isOnline.value) Color(0xFF00D68F) else Color(0xFFFF6B6B),
                                        CircleShape
                                    )
                            )
                            Text(
                                text = if (isOnline.value) "Online" else "Offline",
                                fontWeight = FontWeight.Bold,
                                color = if (isOnline.value) Color(0xFF00D68F) else Color(0xFFFF6B6B),
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(AccentYellow.copy(alpha = 0.2f))
                            .border(1.dp, AccentYellow, CircleShape)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${profile.currentStreak} \uD83D\uDD25",
                            fontWeight = FontWeight.Bold,
                            color = AccentYellow,
                            fontSize = 14.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
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
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = PrimaryIndigo.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            if (activeTab == ActiveTab.Home || activeTab == ActiveTab.Lesson) {
                FloatingActionButton(
                    onClick = { onItemClick(CreateDeck()) },
                    containerColor = PrimaryIndigo,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Deck", modifier = Modifier.size(32.dp))
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                ActiveTab.Home -> HomeScreen(onItemClick = onItemClick, viewModel = viewModel)
                ActiveTab.Lesson -> LessonScreen(onItemClick = onItemClick, viewModel = viewModel)
                ActiveTab.Library -> LibraryScreen()
                ActiveTab.Progress -> ProgressScreen()
                ActiveTab.Profile -> ProfileScreen(
                    onLogoutClick = { onItemClick(Login) },
                    onSettingsClick = { onItemClick(Settings) },
                    onLeaderboardClick = { onItemClick(Leaderboard) }
                )
            }
        }
    }

    if (showSyncDialog) {
        AlertDialog(
            onDismissRequest = { showSyncDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Sync, contentDescription = null, tint = PrimaryIndigo)
                    Text("Sync & Offline", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Status:", fontWeight = FontWeight.Bold)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(
                                        if (isOnline.value) Color(0xFF00D68F) else Color(0xFFFF6B6B),
                                        CircleShape
                                    )
                            )
                            Text(
                                if (isOnline.value) "Connected" else "Offline",
                                color = if (isOnline.value) Color(0xFF00D68F) else Color(0xFFFF6B6B),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    HorizontalDivider()
                    Text("Tap Sync to sync data between device and cloud.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Offline Mode", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Force local data only", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = !isOnline.value,
                            onCheckedChange = { offline ->
                                if (offline) Database.forceOfflineMode() else Database.forceOnlineMode()
                                isOnline.value = Database.isOnline()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFFFF6B6B),
                                checkedTrackColor = Color(0xFFFF6B6B).copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        Database.syncNow()
                        isOnline.value = Database.isOnline()
                        showSyncDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                ) {
                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sync Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSyncDialog = false }) { Text("Close") }
            }
        )
    }

    if (viewModel.showReviewGate) {
        ReviewGateOverlay(
            onDismiss = {
                viewModel.showReviewGate = false
            },
            onFinish = { accuracy, xp, cardsReviewed ->
                viewModel.onReviewGateFinished(accuracy, xp, cardsReviewed)
                Database.markTodayAsActive()
            }
        )
    }

    if (viewModel.showReviewGateResult && viewModel.reviewGateCards > 0) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissReviewGateResult() },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("\uD83C\uDF1F", fontSize = 24.sp)
                    Text("Quick Review Done!", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("You reviewed ${viewModel.reviewGateCards} cards and earned ${viewModel.reviewGateXp} XP!")
                    if (viewModel.reviewGateAccuracy >= 80) {
                        Text("Great accuracy! Keep it up! \uD83D\uDCAA", fontWeight = FontWeight.Bold, color = SecondaryGreen)
                    } else if (viewModel.reviewGateAccuracy >= 50) {
                        Text("Good effort! Stay consistent! \uD83D\uDD25", fontWeight = FontWeight.Bold)
                    } else {
                        Text("Keep practicing! You'll improve! \uD83D\uDCD6", fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissReviewGateResult() },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                ) {
                    Text("Continue")
                }
            }
        )
    }
}

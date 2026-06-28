package com.example.mindcard.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import com.example.mindcard.CreateAI
import com.example.mindcard.CreateDeck
import com.example.mindcard.CreateCard
import com.example.mindcard.FlashcardStudy
import com.example.mindcard.Login
import com.example.mindcard.data.Database
import com.example.mindcard.ui.screens.*

import androidx.lifecycle.viewmodel.compose.viewModel
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
    val decksList = viewModel.decks

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
                    onClick = { onItemClick(CreateDeck()) },
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
                ActiveTab.Home -> HomeScreen(onItemClick = onItemClick, viewModel = viewModel)
                ActiveTab.Lesson -> LessonScreen(onItemClick = onItemClick, viewModel = viewModel)
                ActiveTab.Library -> LibraryScreen()
                ActiveTab.Progress -> ProgressScreen()
                ActiveTab.Profile -> ProfileScreen(onLogoutClick = { onItemClick(Login) })
            }
        }
    }
}

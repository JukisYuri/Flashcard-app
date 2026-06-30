package com.example.mindcard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import com.example.mindcard.Main
import com.example.mindcard.data.Database
import com.example.mindcard.data.ApiClient
import com.example.mindcard.data.UserProfile
import com.example.mindcard.ui.main.PrimaryIndigo

data class LeaderboardEntry(
    val name: String,
    val xp: Int,
    val level: Int,
    val isCurrentUser: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    onNavigate: (NavKey) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentUser = Database.userProfile.value
    val currentUserName = currentUser.name

    var leaderboard by remember { mutableStateOf<List<LeaderboardEntry>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(currentUser) {
        isLoading = true
        try {
            val serverProfiles = ApiClient.get<List<UserProfile>>("/users/leaderboard")
            if (serverProfiles != null && serverProfiles.isNotEmpty()) {
                val entries = serverProfiles.map { profile ->
                    LeaderboardEntry(
                        name = profile.name,
                        xp = profile.totalXp,
                        level = profile.level,
                        isCurrentUser = profile.name == currentUserName
                    )
                }
                leaderboard = entries.sortedByDescending { it.xp }
            } else {
                leaderboard = listOf(LeaderboardEntry(currentUserName, currentUser.totalXp, currentUser.level, true))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            leaderboard = listOf(LeaderboardEntry(currentUserName, currentUser.totalXp, currentUser.level, true))
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Leaderboard", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { onNavigate(Main) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryIndigo)
            }
        } else {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top 3 podium
                if (leaderboard.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // 2nd place
                        if (leaderboard.size >= 2) {
                            PodiumItem(
                                entry = leaderboard[1],
                                medal = "🥈",
                                height = 100.dp,
                                gradient = listOf(Color(0xFFC0C0C0), Color(0xFFE8E8E8))
                            )
                        } else {
                            Spacer(modifier = Modifier.width(80.dp))
                        }

                        // 1st place
                        PodiumItem(
                            entry = leaderboard[0],
                            medal = "🥇",
                            height = 130.dp,
                            gradient = listOf(Color(0xFFFFD700), Color(0xFFFFE082))
                        )

                        // 3rd place
                        if (leaderboard.size >= 3) {
                            PodiumItem(
                                entry = leaderboard[2],
                                medal = "🥉",
                                height = 80.dp,
                                gradient = listOf(Color(0xFFCD7F32), Color(0xFFDEB887))
                            )
                        } else {
                            Spacer(modifier = Modifier.width(80.dp))
                        }
                    }
                }

                // Remaining entries
                if (leaderboard.size > 3) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(1),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(leaderboard.drop(3).size) { index ->
                            val entry = leaderboard.drop(3)[index]
                            LeaderboardRow(
                                rank = index + 4,
                                entry = entry
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PodiumItem(
    entry: LeaderboardEntry,
    medal: String,
    height: Dp,
    gradient: List<Color>
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (entry.isCurrentUser) PrimaryIndigo
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                entry.name.first().toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (entry.isCurrentUser) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            entry.name.split(" ").first(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            "${entry.xp} XP",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        // Podium
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(height)
                .background(
                    Brush.verticalGradient(gradient),
                    RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                medal,
                fontSize = 24.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun LeaderboardRow(
    rank: Int,
    entry: LeaderboardEntry
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (entry.isCurrentUser) PrimaryIndigo.copy(alpha = 0.1f)
            else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (entry.isCurrentUser) ButtonDefaults.outlinedButtonBorder(enabled = true) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Rank
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        if (entry.isCurrentUser) PrimaryIndigo
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "$rank",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (entry.isCurrentUser) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (entry.isCurrentUser) PrimaryIndigo.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    entry.name.first().toString(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (entry.isCurrentUser) PrimaryIndigo else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Name & Level
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    entry.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Level ${entry.level}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // XP
            Text(
                "${entry.xp} XP",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryIndigo
            )
        }
    }
}

package com.example.mindcard.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mindcard.data.Database
import com.example.mindcard.ui.screens.*

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
                        Database.updateUserProfile(profile.copy(currentStreak = profile.currentStreak + 1))
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

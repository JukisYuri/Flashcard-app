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
import java.util.Calendar
import java.util.Locale

@Composable
fun ProgressScreen() {
    val profile = Database.userProfile.value
    var streakRestoreActive by remember { mutableStateOf(true) }

    // Dynamic calendar calculation
    val calendar = remember { Calendar.getInstance() }
    val currentYear = remember { calendar.get(Calendar.YEAR) }
    val currentMonth = remember { calendar.get(Calendar.MONTH) } // 0-indexed
    val todayDay = remember { calendar.get(Calendar.DAY_OF_MONTH) }

    val monthNames = remember {
        listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")
    }
    val monthName = monthNames[currentMonth]

    val firstDayCal = remember {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }
    // Sunday is 1, Monday is 2, etc. Offset is (Day of Week - 1)
    val startOffset = remember { firstDayCal.get(Calendar.DAY_OF_WEEK) - 1 }
    val totalDays = remember { calendar.getActualMaximum(Calendar.DAY_OF_MONTH) }

    val cells = remember {
        List(startOffset) { null } + (1..totalDays).toList()
    }

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
            text = if (profile.currentStreak > 0) "You're on fire!" else "Start learning today!",
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
                Text("$monthName $currentYear", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PrimaryIndigo)
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

            // Calendar days layout
            val chunked = cells.chunked(7)
            chunked.forEach { week ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    week.forEach { day ->
                        val dateStr = if (day != null) {
                            String.format(Locale.getDefault(), "%04d-%02d-%02d", currentYear, currentMonth + 1, day)
                        } else ""
                        val studied = day != null && profile.studyHistory[dateStr] == true
                        val isToday = day != null && day == todayDay

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (day == null) Color.Transparent
                                    else if (studied) SuccessBg.copy(alpha = 0.3f)
                                    else if (isToday) PrimaryIndigo.copy(alpha = 0.2f)
                                    else Color(0xFFF2F4F6)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isToday) PrimaryIndigo else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (day != null) {
                                Text(
                                    day.toString(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (studied) SecondaryGreen else if (isToday) PrimaryIndigo else Color(0xFF191C1E)
                                )
                            }
                        }
                    }
                    // Pad out shorter rows at the end
                    if (week.size < 7) {
                        Spacer(modifier = Modifier.weight((7 - week.size).toFloat()))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        if (streakRestoreActive && profile.currentStreak == 0) {
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
                    Text("Streak Shield Active! Keep your streak going.", fontSize = 13.sp, color = OutlineColor)
                }
                Button(
                    onClick = {
                        Database.updateUserProfile(profile.copy(currentStreak = 1))
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

package com.example.mindcard.ui.main

import androidx.compose.foundation.BorderStroke
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mindcard.ui.screens.*
import com.example.mindcard.ui.viewmodel.ProgressViewModel
import java.util.Calendar

@Composable
fun ProgressScreen(
    modifier: Modifier = Modifier,
    viewModel: ProgressViewModel = viewModel()
) {
    val profile by viewModel.userProfile

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
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Your Progress", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191C1E), modifier = Modifier.align(Alignment.Start))

        // Month Year Header
        Text(
            text = "$monthName $currentYear",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryIndigo,
            modifier = Modifier.align(Alignment.Start)
        )

        // Calendar Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, OutlineVariantColor.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Days of week header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val daysOfWeek = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
                    daysOfWeek.forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = OutlineColor
                        )
                    }
                }

                HorizontalDivider(color = OutlineVariantColor.copy(alpha = 0.5f))

                // Days grid rows
                val rows = cells.chunked(7)
                rows.forEach { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        week.forEach { day ->
                            val key = if (day != null) String.format("%04d-%02d-%02d", currentYear, currentMonth + 1, day) else ""
                            val studied = day != null && profile.studyHistory.containsKey(key)
                            val isToday = day == todayDay

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (studied) SecondaryGreen.copy(alpha = 0.2f)
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
        }

        if (viewModel.streakRestoreActive && profile.currentStreak == 0) {
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
                        viewModel.restoreStreak()
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

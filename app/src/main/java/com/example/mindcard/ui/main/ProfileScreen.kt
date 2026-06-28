package com.example.mindcard.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mindcard.data.Database
import com.example.mindcard.ui.screens.*
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ProfileScreen(
    onLogoutClick: () -> Unit
) {
    val profile = Database.userProfile.value
    var isEditingName by remember { mutableStateOf(false) }
    var editNameInput by remember { mutableStateOf(profile.name) }

    // Dynamic weekly activity calculation
    val weekDaysInfo = remember(profile.studyHistory) {
        val calendar = Calendar.getInstance()
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val daysToSubtract = if (currentDayOfWeek == Calendar.SUNDAY) 6 else currentDayOfWeek - Calendar.MONDAY
        
        val mondayCal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -daysToSubtract)
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = dateFormat.format(Calendar.getInstance().time)

        (0..6).map { i ->
            val dayCal = mondayCal.clone() as Calendar
            dayCal.add(Calendar.DAY_OF_YEAR, i)
            val dateStr = dateFormat.format(dayCal.time)
            val dayLabel = when (dayCal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> "M"
                Calendar.TUESDAY -> "T"
                Calendar.WEDNESDAY -> "W"
                Calendar.THURSDAY -> "T"
                Calendar.FRIDAY -> "F"
                Calendar.SATURDAY -> "S"
                Calendar.SUNDAY -> "S"
                else -> ""
            }
            val studied = profile.studyHistory[dateStr] == true
            val isToday = todayStr == dateStr
            Triple(dayLabel, studied, isToday)
        }
    }
    val activeDaysCount = remember(weekDaysInfo) { weekDaysInfo.count { it.second } }

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
                    HorizontalDivider(modifier = Modifier.width(1.dp).height(24.dp).align(Alignment.CenterVertically))
                    ProfileStatItem("Words", "${profile.totalWordsLearned}")
                    HorizontalDivider(modifier = Modifier.width(1.dp).height(24.dp).align(Alignment.CenterVertically))
                    ProfileStatItem("Streak", "${profile.currentStreak}")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { isEditingName = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Edit Profile", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onLogoutClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFDAD6)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Log Out", fontWeight = FontWeight.Bold, color = Color(0xFFBA1A1A))
                    }
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
            Text("Days active this week: $activeDaysCount/7 days", fontSize = 12.sp, color = OutlineColor)

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Bottom
            ) {
                weekDaysInfo.forEach { (name, studied, isToday) ->
                    val barHeight = if (studied) 80.dp else 12.dp
                    val barColor = if (isToday) PrimaryIndigo else if (studied) PrimaryIndigo.copy(alpha = 0.6f) else Color(0xFFF2F4F6)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Box(
                            modifier = Modifier
                                .width(16.dp)
                                .height(barHeight)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(barColor)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = name,
                            fontSize = 11.sp,
                            fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Bold,
                            color = if (isToday) PrimaryIndigo else OutlineColor
                        )
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

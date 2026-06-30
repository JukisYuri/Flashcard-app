package com.example.mindcard.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mindcard.ui.screens.*
import com.example.mindcard.ui.viewmodel.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun ProfileScreen(
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel()
) {
    val profile by viewModel.userProfile

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
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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
                        onClick = { viewModel.startEditing() },
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
            Text("Badges & Achievements", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191C1E))
            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        BadgeItem(
                            emoji = "🌱",
                            title = "Day One",
                            desc = "Account created",
                            progress = 1f,
                            progressText = "Unlocked",
                            unlocked = true
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        val isStreakUnlocked = profile.currentStreak >= 3
                        val streakProgress = (profile.currentStreak.toFloat() / 3f).coerceIn(0f, 1f)
                        BadgeItem(
                            emoji = "🔥",
                            title = "Streak Lord",
                            desc = "3-day study streak",
                            progress = streakProgress,
                            progressText = "${profile.currentStreak}/3d",
                            unlocked = isStreakUnlocked
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        val wordGoal = 20
                        val isVocabUnlocked = profile.totalWordsLearned >= wordGoal
                        val vocabProgress = (profile.totalWordsLearned.toFloat() / wordGoal.toFloat()).coerceIn(0f, 1f)
                        BadgeItem(
                            emoji = "🧠",
                            title = "Vocab Master",
                            desc = "$wordGoal words learned",
                            progress = vocabProgress,
                            progressText = "${profile.totalWordsLearned}/$wordGoal",
                            unlocked = isVocabUnlocked
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        val xpGoal = 100
                        val isXpUnlocked = profile.totalXp >= xpGoal
                        val xpProgress = (profile.totalXp.toFloat() / xpGoal.toFloat()).coerceIn(0f, 1f)
                        BadgeItem(
                            emoji = "⚡",
                            title = "XP Champion",
                            desc = "$xpGoal XP points",
                            progress = xpProgress,
                            progressText = "${profile.totalXp}/$xpGoal XP",
                            unlocked = isXpUnlocked
                        )
                    }
                }
            }
        }

        // Settings / Reminders
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(24.dp))
                .border(1.dp, OutlineVariantColor.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            Text("Settings", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191C1E))
            Spacer(modifier = Modifier.height(12.dp))

            val context = androidx.compose.ui.platform.LocalContext.current
            var reminderEnabled by remember {
                mutableStateOf(com.example.mindcard.util.ReminderManager.isReminderEnabled(context))
            }

            // Android 13+ Notification Permission Launcher
            val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    com.example.mindcard.util.ReminderManager.scheduleDailyReminder(context)
                    reminderEnabled = true
                } else {
                    reminderEnabled = false
                    android.widget.Toast.makeText(
                        context,
                        "Notification permission denied. Cannot schedule reminder.",
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Daily Study Reminder", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191C1E))
                    Text("Get notified daily at 20:00 to keep up your streak", fontSize = 11.sp, color = OutlineColor)
                }
                Switch(
                    checked = reminderEnabled,
                    onCheckedChange = { checked ->
                        if (checked) {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                // Request permission on Android 13+
                                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                com.example.mindcard.util.ReminderManager.scheduleDailyReminder(context)
                                reminderEnabled = true
                            }
                        } else {
                            com.example.mindcard.util.ReminderManager.cancelDailyReminder(context)
                            reminderEnabled = false
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = PrimaryIndigo,
                        uncheckedThumbColor = OutlineColor,
                        uncheckedTrackColor = Color(0xFFF2F4F6)
                    )
                )
            }
        }
    }

    if (viewModel.isEditingName) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelEditing() },
            title = { Text("Edit Name") },
            text = {
                OutlinedTextField(
                    value = viewModel.editNameInput,
                    onValueChange = { viewModel.editNameInput = it },
                    label = { Text("Display Name") }
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.saveProfileName() },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelEditing() }) {
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
fun BadgeItem(
    emoji: String,
    title: String,
    desc: String,
    progress: Float,
    progressText: String,
    unlocked: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (unlocked) Color(0xFFF9FAFC) else Color(0xFFFAFAFA), RoundedCornerShape(14.dp))
            .border(
                1.dp,
                if (unlocked) PrimaryIndigo.copy(alpha = 0.15f) else OutlineVariantColor.copy(alpha = 0.3f),
                RoundedCornerShape(14.dp)
            )
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (unlocked) Color(0xFFFFE083).copy(alpha = 0.3f) else Color(0xFFEAEAEA)),
            contentAlignment = Alignment.Center
        ) {
            Text(if (unlocked) emoji else "🔒", fontSize = 18.sp)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (unlocked) Color(0xFF191C1E) else OutlineColor
            )
            Text(
                text = desc,
                fontSize = 9.sp,
                color = OutlineColor,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .clip(CircleShape),
                    color = if (unlocked) SecondaryGreen else PrimaryIndigo,
                    trackColor = Color(0xFFE2E8F0)
                )
                Text(
                    text = progressText,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (unlocked) SecondaryGreen else OutlineColor
                )
            }
        }
    }
}

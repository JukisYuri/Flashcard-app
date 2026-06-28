package com.example.mindcard.ui.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import com.example.mindcard.Main
import com.example.mindcard.notification.NotificationHelper
import com.example.mindcard.notification.ReminderWorker
import com.example.mindcard.ui.main.BackgroundFrost
import com.example.mindcard.ui.main.OutlineColor
import com.example.mindcard.ui.main.OutlineVariantColor
import com.example.mindcard.ui.main.PrimaryIndigo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigate: (NavKey) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var reminderEnabled by remember { mutableStateOf(NotificationHelper.isReminderEnabled(context)) }
    val (savedHour, savedMinute) = remember { NotificationHelper.getReminderTime(context) }
    var reminderHour by remember { mutableIntStateOf(savedHour) }
    var reminderMinute by remember { mutableIntStateOf(savedMinute) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { onNavigate(Main) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundFrost)
            )
        },
        containerColor = BackgroundFrost
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Notification Settings
            Text(
                "Notifications",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF191C1E)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Reminder toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = null,
                                tint = PrimaryIndigo,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    "Daily Reminder",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    "Get reminded to study every day",
                                    fontSize = 12.sp,
                                    color = OutlineColor
                                )
                            }
                        }
                        Switch(
                            checked = reminderEnabled,
                            onCheckedChange = { enabled ->
                                reminderEnabled = enabled
                                NotificationHelper.setReminderEnabled(context, enabled)
                                if (enabled) {
                                    ReminderWorker.schedule(context, reminderHour, reminderMinute)
                                } else {
                                    ReminderWorker.cancel(context)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PrimaryIndigo,
                                checkedTrackColor = PrimaryIndigo.copy(alpha = 0.3f)
                            )
                        )
                    }

                    HorizontalDivider(color = OutlineVariantColor.copy(alpha = 0.5f))

                    // Time picker
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = reminderEnabled) {
                                val dialog = TimePickerDialog(
                                    context,
                                    { _, hour, minute ->
                                        reminderHour = hour
                                        reminderMinute = minute
                                        NotificationHelper.setReminderTime(context, hour, minute)
                                        if (reminderEnabled) {
                                            ReminderWorker.schedule(context, hour, minute)
                                        }
                                    },
                                    reminderHour,
                                    reminderMinute,
                                    true
                                )
                                dialog.show()
                            },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Schedule,
                                contentDescription = null,
                                tint = if (reminderEnabled) PrimaryIndigo else OutlineColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    "Reminder Time",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = if (reminderEnabled) Color(0xFF191C1E) else OutlineColor
                                )
                                Text(
                                    "${reminderHour.toString().padStart(2, '0')}:${reminderMinute.toString().padStart(2, '0')}",
                                    fontSize = 14.sp,
                                    color = if (reminderEnabled) PrimaryIndigo else OutlineColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = OutlineColor
                        )
                    }
                }
            }

            // About section
            Text(
                "About",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF191C1E)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Version", fontSize = 14.sp, color = OutlineColor)
                        Text("1.0.0", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    HorizontalDivider(color = OutlineVariantColor.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Developer", fontSize = 14.sp, color = OutlineColor)
                        Text("MindCard Team", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

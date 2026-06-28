package com.example.mindcard.ui.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import com.example.mindcard.Main
import com.example.mindcard.notification.NotificationHelper
import com.example.mindcard.notification.ReminderWorker
import com.example.mindcard.theme.ThemeManager
import com.example.mindcard.ui.main.PrimaryIndigo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigate: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
    onThemeChanged: ((Boolean) -> Unit)? = null
) {
    val context = LocalContext.current
    var reminderEnabled by remember { mutableStateOf(NotificationHelper.isReminderEnabled(context)) }
    val (savedHour, savedMinute) = remember { NotificationHelper.getReminderTime(context) }
    var reminderHour by remember { mutableIntStateOf(savedHour) }
    var reminderMinute by remember { mutableIntStateOf(savedMinute) }
    var themeMode by remember { mutableIntStateOf(ThemeManager.getThemeMode(context)) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var dailyCardLimit by remember { mutableIntStateOf(ThemeManager.getDailyCardLimit(context)) }
    var showCardLimitDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("Appearance", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { showThemeDialog = true },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Default.DarkMode, contentDescription = null, tint = PrimaryIndigo, modifier = Modifier.size(24.dp))
                            Column {
                                Text("Theme", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(
                                    when (themeMode) { 0 -> "System default"; 1 -> "Light"; 2 -> "Dark"; else -> "System default" },
                                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Text("Notifications", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Default.Notifications, contentDescription = null, tint = PrimaryIndigo, modifier = Modifier.size(24.dp))
                            Column {
                                Text("Daily Reminder", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Get reminded to study every day", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = reminderEnabled,
                            onCheckedChange = { enabled ->
                                reminderEnabled = enabled
                                NotificationHelper.setReminderEnabled(context, enabled)
                                if (enabled) ReminderWorker.schedule(context, reminderHour, reminderMinute) else ReminderWorker.cancel(context)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = PrimaryIndigo, checkedTrackColor = PrimaryIndigo.copy(alpha = 0.3f))
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                    Row(
                        modifier = Modifier.fillMaxWidth().clickable(enabled = reminderEnabled) {
                            TimePickerDialog(context, { _, hour, minute ->
                                reminderHour = hour; reminderMinute = minute
                                NotificationHelper.setReminderTime(context, hour, minute)
                                if (reminderEnabled) ReminderWorker.schedule(context, hour, minute)
                            }, reminderHour, reminderMinute, true).show()
                        },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = if (reminderEnabled) PrimaryIndigo else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
                            Column {
                                Text("Reminder Time", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (reminderEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${reminderHour.toString().padStart(2, '0')}:${reminderMinute.toString().padStart(2, '0')}", fontSize = 14.sp, color = if (reminderEnabled) PrimaryIndigo else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Text("Learning", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { showCardLimitDialog = true },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Default.Style, contentDescription = null, tint = PrimaryIndigo, modifier = Modifier.size(24.dp))
                            Column {
                                Text("Daily Card Limit", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("$dailyCardLimit cards per day", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Text("About", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Version", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("1.0.0", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Developer", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("MindCard Team", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Select Theme", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeOption("System default", "Follow your device settings", themeMode == 0) { themeMode = 0; ThemeManager.setThemeMode(context, 0); onThemeChanged?.invoke(false); showThemeDialog = false }
                    ThemeOption("Light", "Always use light theme", themeMode == 1) { themeMode = 1; ThemeManager.setThemeMode(context, 1); onThemeChanged?.invoke(false); showThemeDialog = false }
                    ThemeOption("Dark", "Always use dark theme", themeMode == 2) { themeMode = 2; ThemeManager.setThemeMode(context, 2); onThemeChanged?.invoke(true); showThemeDialog = false }
                }
            },
            confirmButton = { TextButton(onClick = { showThemeDialog = false }) { Text("Cancel") } }
        )
    }

    if (showCardLimitDialog) {
        val options = listOf(10, 20, 30, 50, 100)
        AlertDialog(
            onDismissRequest = { showCardLimitDialog = false },
            title = { Text("Daily Card Limit", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    options.forEach { limit ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { dailyCardLimit = limit; ThemeManager.setDailyCardLimit(context, limit); showCardLimitDialog = false }.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("$limit cards", fontSize = 16.sp, color = if (dailyCardLimit == limit) PrimaryIndigo else MaterialTheme.colorScheme.onSurface)
                            if (dailyCardLimit == limit) Icon(Icons.Default.Check, contentDescription = null, tint = PrimaryIndigo)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showCardLimitDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun ThemeOption(title: String, subtitle: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (selected) PrimaryIndigo else MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (selected) Icon(Icons.Default.Check, contentDescription = null, tint = PrimaryIndigo)
    }
}

package com.example.mindcard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.mindcard.data.Database
import com.example.mindcard.notification.NotificationHelper
import com.example.mindcard.notification.ReminderWorker
import com.example.mindcard.theme.MindCardTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Initialize Room Database and SyncManager
    Database.initialize(applicationContext)

    // Initialize Notifications
    NotificationHelper.createNotificationChannel(this)
    ReminderWorker.reschedule(this)

    enableEdgeToEdge()
    setContent {
      MindCardTheme { Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) { MainNavigation() } }
    }
  }
}

package com.example.mindcard.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.mindcard.MainActivity
import com.example.mindcard.R
import kotlin.random.Random

object NotificationHelper {
    const val CHANNEL_ID = "mindcard_reminders"
    const val CHANNEL_NAME = "Study Reminders"
    const val NOTIFICATION_ID = 1001
    const val PREFS_NAME = "mindcard_prefs"
    const val KEY_REMINDER_ENABLED = "reminder_enabled"
    const val KEY_REMINDER_HOUR = "reminder_hour"
    const val KEY_REMINDER_MINUTE = "reminder_minute"

    fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Daily study reminders for MindCard"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 300, 200, 300)
        }

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    fun sendReminderNotification(context: Context, title: String, message: String) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("open_study", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            Random.nextInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 300, 200, 300))
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(Random.nextInt(), notification)
    }

    fun sendStreakReminder(context: Context, streakDays: Int) {
        val title = "Keep your streak going! 🔥"
        val message = "You have a $streakDays day streak. Don't break it!"
        sendReminderNotification(context, title, message)
    }

    fun sendDueCardsReminder(context: Context, dueCount: Int) {
        val title = "You have cards to review! 📚"
        val message = "You have $dueCount cards due for review. Study now!"
        sendReminderNotification(context, title, message)
    }

    fun isReminderEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_REMINDER_ENABLED, true)
    }

    fun getReminderTime(context: Context): Pair<Int, Int> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val hour = prefs.getInt(KEY_REMINDER_HOUR, 9)
        val minute = prefs.getInt(KEY_REMINDER_MINUTE, 0)
        return Pair(hour, minute)
    }

    fun setReminderTime(context: Context, hour: Int, minute: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putInt(KEY_REMINDER_HOUR, hour)
            .putInt(KEY_REMINDER_MINUTE, minute)
            .apply()
    }

    fun setReminderEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_REMINDER_ENABLED, enabled).apply()
    }
}

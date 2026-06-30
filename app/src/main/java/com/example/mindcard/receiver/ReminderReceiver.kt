package com.example.mindcard.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.mindcard.MainActivity
import com.example.mindcard.R
import com.example.mindcard.util.ReminderManager

class ReminderReceiver : BroadcastReceiver() {
    private val NOTIFICATION_ID = 2002

    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create intent to open MainActivity when notification is clicked
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build notification
        val builder = NotificationCompat.Builder(context, ReminderManager.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // Uses the app's launcher icon
            .setContentTitle("🧠 Giờ học từ vựng rồi!")
            .setContentText("Hãy dành ra 5 phút hôm nay để duy trì chuỗi học tập (Streak) của bạn nhé! 🔥")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Show notification
        try {
            notificationManager.notify(NOTIFICATION_ID, builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}

package com.example.mindcard.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.mindcard.util.ReminderManager

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Check if reminder was previously enabled, and reschedule it
            if (ReminderManager.isReminderEnabled(context)) {
                ReminderManager.scheduleDailyReminder(context)
            }
        }
    }
}

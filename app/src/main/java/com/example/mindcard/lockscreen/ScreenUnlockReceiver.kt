package com.example.mindcard.lockscreen

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager

class ScreenUnlockReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        if (action == Intent.ACTION_USER_PRESENT ||
            action == "android.intent.action.SCREEN_ON") {

            val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

            if (!keyguardManager.isKeyguardLocked && powerManager.isInteractive) {
                // Check if enough time passed since last show (min 30 minutes)
                val prefs = context.getSharedPreferences("lockscreen_prefs", Context.MODE_PRIVATE)
                val lastShow = prefs.getLong("last_show_time", 0)
                val now = System.currentTimeMillis()

                if (now - lastShow > 30 * 60 * 1000) { // 30 minutes
                    prefs.edit().putLong("last_show_time", now).apply()

                    val showIntent = Intent(context, LockScreenActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    }
                    context.startActivity(showIntent)
                }
            }
        }
    }
}

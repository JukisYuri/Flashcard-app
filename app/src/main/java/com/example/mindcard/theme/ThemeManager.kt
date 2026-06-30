package com.example.mindcard.theme

import android.content.Context
import android.content.SharedPreferences
import com.example.mindcard.notification.NotificationHelper

object ThemeManager {
    const val PREFS_NAME = "mindcard_theme_prefs"
    const val KEY_DARK_MODE = "dark_mode"
    const val KEY_THEME_MODE = "theme_mode" // 0 = System, 1 = Light, 2 = Dark

    fun getThemeMode(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_THEME_MODE, 0) // Default: System
    }

    fun setThemeMode(context: Context, mode: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_THEME_MODE, mode).apply()
    }

    fun isDarkMode(context: Context): Boolean {
        val mode = getThemeMode(context)
        return when (mode) {
            1 -> false // Light
            2 -> true  // Dark
            else -> false // System default (we'll handle this in Composable)
        }
    }

    fun getDailyCardLimit(context: Context): Int {
        val prefs = context.getSharedPreferences(NotificationHelper.PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt("daily_card_limit", 20)
    }

    fun setDailyCardLimit(context: Context, limit: Int) {
        val prefs = context.getSharedPreferences(NotificationHelper.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt("daily_card_limit", limit).apply()
    }
}

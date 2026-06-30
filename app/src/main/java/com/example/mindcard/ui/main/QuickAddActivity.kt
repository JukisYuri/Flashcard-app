// QuickAddActivity.kt
package com.example.mindcard.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.mindcard.ui.screens.QuickAddScreen

class QuickAddActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Bắt lấy đoạn text mà người dùng vừa bôi đen
        val selectedText = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString() ?: ""

        setContent {
            // Hiển thị UI Compose
            QuickAddScreen(
                initialText = selectedText,
                onClose = { finish() }
            )
        }
    }
}
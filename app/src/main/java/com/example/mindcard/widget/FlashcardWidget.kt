package com.example.mindcard.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.mindcard.R
import com.example.mindcard.data.local.AppDatabase
import com.example.mindcard.lockscreen.WidgetTtsService
import kotlinx.coroutines.*

class FlashcardWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(
            android.content.ComponentName(context, FlashcardWidget::class.java)
        )
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        private var lastIndex = -1

        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_flashcard)

            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                try {
                    val db = AppDatabase.getInstance(context)
                    val allCards = db.cardDao().getAllCards()

                    withContext(Dispatchers.Main) {
                        if (allCards.isNotEmpty()) {
                            lastIndex = (lastIndex + 1) % allCards.size
                            val card = allCards[lastIndex]

                            views.setTextViewText(R.id.widget_word, card.englishWord)
                            views.setTextViewText(R.id.widget_pronunciation, card.pronunciation)
                            views.setTextViewText(R.id.widget_definition, card.definition)
                            views.setTextViewText(R.id.widget_pos, card.pos)

                            // Click = speak word + cycle to next
                            val ttsIntent = Intent(context, WidgetTtsService::class.java).apply {
                                putExtra("word", card.englishWord)
                            }
                            val ttsPendingIntent = android.app.PendingIntent.getService(
                                context, appWidgetId, ttsIntent,
                                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                            )
                            views.setOnClickPendingIntent(R.id.widget_card, ttsPendingIntent)
                        } else {
                            views.setTextViewText(R.id.widget_word, "MindCard")
                            views.setTextViewText(R.id.widget_pronunciation, "")
                            views.setTextViewText(R.id.widget_definition, "Add vocabulary to start learning!")
                            views.setTextViewText(R.id.widget_pos, "✨")

                            val openIntent = Intent(context, com.example.mindcard.MainActivity::class.java)
                            val openPendingIntent = android.app.PendingIntent.getActivity(
                                context, 0, openIntent,
                                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                            )
                            views.setOnClickPendingIntent(R.id.widget_card, openPendingIntent)
                        }

                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        views.setTextViewText(R.id.widget_word, "MindCard")
                        views.setTextViewText(R.id.widget_pronunciation, "")
                        views.setTextViewText(R.id.widget_definition, "Tap to open and learn!")
                        views.setTextViewText(R.id.widget_pos, "✨")

                        val intent = android.content.Intent(context, com.example.mindcard.MainActivity::class.java)
                        val pendingIntent = android.app.PendingIntent.getActivity(
                            context, 0, intent,
                            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                        )
                        views.setOnClickPendingIntent(R.id.widget_card, pendingIntent)
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                }
            }
        }
    }
}

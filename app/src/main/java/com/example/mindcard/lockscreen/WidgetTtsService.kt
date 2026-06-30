package com.example.mindcard.lockscreen

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.speech.tts.TextToSpeech
import java.util.Locale

class WidgetTtsService : Service() {
    private var tts: TextToSpeech? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val word = intent?.getStringExtra("word") ?: return START_NOT_STICKY

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                tts?.speak(word, TextToSpeech.QUEUE_FLUSH, null, null)
                tts?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                    override fun onDone(utteranceId: String?) {
                        stopSelf()
                    }
                    override fun onError(utteranceId: String?) {
                        stopSelf()
                    }
                    override fun onStart(utteranceId: String?) {}
                })
            } else {
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}

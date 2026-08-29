package com.example.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class AudioSplitAnnouncer(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = TextToSpeech(context, this)
    private var isReady = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.getDefault()
            isReady = true
        }
    }

    fun announceSplit(kilometer: Int, paceMinutes: Int, paceSeconds: Int) {
        if (!isReady) return
        val message = "Kilometer $kilometer completed. Split pace: $paceMinutes minutes $paceSeconds seconds."
        tts?.speak(message, TextToSpeech.QUEUE_ADD, null, "split_$kilometer")
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}

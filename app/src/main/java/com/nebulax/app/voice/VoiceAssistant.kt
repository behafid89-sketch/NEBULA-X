package com.nebulax.app.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale

/**
 * Assistante vocale « Nebula ».
 *  - Reconnaissance vocale : service Android SpeechRecognizer (Google / Samsung), en fr-FR.
 *  - Réponse parlée : moteur Text-to-Speech du téléphone, en français.
 *  - Compréhension : règles par mots-clés (voir [Brain]). Ce n'est pas un grand modèle de langage.
 */
class VoiceAssistant(private val context: Context) {

    var listening by mutableStateOf(false); private set
    var heard by mutableStateOf(""); private set
    var reply by mutableStateOf("Bonjour, je suis Nebula. Touchez le micro et parlez-moi."); private set
    var level by mutableFloatStateOf(0f); private set
    var ttsReady by mutableStateOf(false); private set

    /** Appelé quand une commande demande d'ouvrir un écran. */
    var onNavigate: ((Destination) -> Unit)? = null

    private val brain = Brain(context)
    private var recognizer: SpeechRecognizer? = null

    private val tts: TextToSpeech = TextToSpeech(context.applicationContext) { status ->
        if (status == TextToSpeech.SUCCESS) {
            ttsReady = true
            setFrench()
        }
    }

    private fun setFrench() {
        val r = tts.setLanguage(Locale.FRANCE)
        if (r == TextToSpeech.LANG_MISSING_DATA || r == TextToSpeech.LANG_NOT_SUPPORTED) {
            reply = "La voix française n'est pas installée. Installez-la dans les réglages de synthèse vocale du téléphone."
        }
        tts.setSpeechRate(1.0f)
        tts.setPitch(1.05f)
    }

    fun speak(text: String) {
        reply = text
        if (ttsReady) tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "nebula-" + System.currentTimeMillis())
    }

    fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            speak("La reconnaissance vocale n'est pas disponible. Vérifiez que l'application Google ou Samsung Voice est installée.")
            return
        }
        tts.stop()
        if (recognizer == null) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(context).also { it.setRecognitionListener(listener) }
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fr-FR")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "fr-FR")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }
        heard = ""
        listening = true
        recognizer?.startListening(intent)
    }

    fun stopListening() {
        recognizer?.stopListening()
        listening = false
        level = 0f
    }

    /** Traite une phrase tapée ou reconnue. */
    fun handle(text: String) {
        heard = text
        val answer = brain.answer(text)
        speak(answer.text)
        answer.destination?.let { d -> onNavigate?.invoke(d) }
    }

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) { listening = true }
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) { level = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f) }
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() { level = 0f }
        override fun onError(error: Int) {
            listening = false
            level = 0f
            reply = when (error) {
                SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Je n'ai rien entendu. Touchez le micro et réessayez."
                SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Problème de connexion pour la reconnaissance vocale."
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "J'ai besoin de l'autorisation du micro."
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Le micro est occupé, réessayez dans un instant."
                else -> "Je n'ai pas compris. Réessayez."
            }
        }
        override fun onResults(results: Bundle?) {
            listening = false
            level = 0f
            val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
            if (!text.isNullOrBlank()) handle(text) else reply = "Je n'ai pas compris. Réessayez."
        }
        override fun onPartialResults(partialResults: Bundle?) {
            partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.let { heard = it }
        }
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    fun release() {
        recognizer?.destroy()
        recognizer = null
        tts.stop()
        tts.shutdown()
        brain.torchOff()
    }
}

// Translate.kt
package com.example.hearwell

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
private const val LIBRETRANSLATE_URL = "http://127.0.0.1:5000/translate" // Change if needed

class Translate(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var recognitionListener: RecognitionListener? = null
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val okHttpClient = OkHttpClient()

    private var ttsInitialized = false

    private var onTranslationResult: (String) -> Unit = {}
    private var onSpeechError: (String) -> Unit = {}
    private var onTtsError: (String) -> Unit = {}
    private var onTtsStarted: () -> Unit = {}
    private var onTtsDone: () -> Unit = {}

    private var sourceLanguageCode: String = "en"
    private var targetLanguageCode: String = "es"

    fun setSourceLanguage(languageCode: String) {
        sourceLanguageCode = languageCode
    }

    fun setTargetLanguage(languageCode: String) {
        targetLanguageCode = languageCode
    }

    /**
     * Initializes the TextToSpeech engine.  Must be called before using TTS functions.
     */
    suspend fun initTextToSpeech(): Boolean = suspendCoroutine { continuation ->
        if (ttsInitialized) {
            continuation.resume(true)
            return@suspendCoroutine
        }

        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.getDefault())
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Language is not supported")
                    ttsInitialized = false
                    continuation.resume(false)
                } else {
                    Log.i("TTS", "TTS engine initialized successfully.")
                    ttsInitialized = true
                    continuation.resume(true)
                }
            } else {
                Log.e("TTS", "Initialization failed: $status")
                ttsInitialized = false
                continuation.resume(false)
            }
        }
    }

    private fun setUtteranceListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String) {
                Log.d("TTS", "onStart: $utteranceId")
                onTtsStarted()
            }

            override fun onDone(utteranceId: String) {
                Log.d("TTS", "onDone: $utteranceId")
                onTtsDone()
            }

            override fun onError(utteranceId: String) {
                Log.e("TTS", "onError: $utteranceId")
                onTtsError("TTS Error occurred.")
            }
        })
    }


    /**
     * Starts speech recognition.
     */
    fun startSpeechRecognition() {
        // Check for permission
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                context as? MainActivity ?: throw IllegalArgumentException("Context must be MainActivity"),
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_RECORD_AUDIO_PERMISSION
            )
            onSpeechError("Audio permission not granted")
            return
        }

        // Ensure TTS is initialized
        if (!ttsInitialized) {
            coroutineScope.launch {
                if (!initTextToSpeech()) {
                    onSpeechError("TTS Initialization failed")
                    return@launch
                }
            }
        }

        // Create the SpeechRecognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(createRecognitionListener())
        }

        // Create the recognition intent
        val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, sourceLanguageCode)
        }

        // Start listening
        speechRecognizer?.startListening(recognizerIntent)
    }

    /**
     * Stops speech recognition.
     */
    fun stopSpeechRecognition() {
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    /**
     * Releases the TTS engine.
     */
    fun destroyTextToSpeech() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        ttsInitialized = false
    }

    fun setOnTranslationResultListener(listener: (String) -> Unit) {
        onTranslationResult = listener
    }

    fun setOnSpeechErrorListener(listener: (String) -> Unit) {
        onSpeechError = listener
    }

    fun setOnTtsErrorListener(listener: (String) -> Unit) {
        onTtsError = listener
    }

    fun setOnTtsStartedListener(listener: () -> Unit) {
        onTtsStarted = listener
    }

    fun setOnTtsDoneListener(listener: () -> Unit) {
        onTtsDone = listener
    }

    /**
     * Speech recognition listener.
     */
    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d("SpeechRecognizer", "onReadyForSpeech")
            }

            override fun onBeginningOfSpeech() {
                Log.d("SpeechRecognizer", "onBeginningOfSpeech")
            }

            override fun onRmsChanged(rmsdB: Float) {
                Log.d("SpeechRecognizer", "onRmsChanged: $rmsdB")
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                Log.d("SpeechRecognizer", "onBufferReceived: ${buffer?.size}")
            }

            override fun onEndOfSpeech() {
                Log.d("SpeechRecognizer", "onEndOfSpeech")
            }

            override fun onError(error: Int) {
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No match"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
                    SpeechRecognizer.ERROR_SERVER -> "Server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                    else -> "Unknown error: $error"
                }
                Log.e("SpeechRecognizer", "onError: $errorMessage")
                onSpeechError(errorMessage)
                stopSpeechRecognition()
            }

            override fun onResults(results: Bundle?) {
                Log.d("SpeechRecognizer", "onResults")
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches != null && matches.isNotEmpty()) {
                    val spokenText = matches[0]
                    Log.d("SpeechRecognizer", "Recognized text: $spokenText")
                    coroutineScope.launch {
                        val translatedText = translateText(spokenText)
                        if (translatedText.isNotBlank()) {
                            onTranslationResult(translatedText)
                            speak(translatedText)
                        }
                        stopSpeechRecognition()
                    }
                } else {
                    onSpeechError("No recognition results")
                    stopSpeechRecognition()
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                Log.d("SpeechRecognizer", "onPartialResults")
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches != null && matches.isNotEmpty()) {
                    val partialText = matches[0]
                    Log.d("SpeechRecognizer", "Partial text: $partialText")
                    coroutineScope.launch {
                        val translatedText = translateText(partialText)
                        if (translatedText.isNotBlank()) {
                            onTranslationResult(translatedText)
                        }
                    }
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                Log.d("SpeechRecognizer", "onEvent: $eventType")
            }

            override fun onLanguageDetection(bundle: Bundle) {
                val detectedLanguage = bundle.getString(SpeechRecognizer.DETECTED_LANGUAGE)
                Log.d("SpeechRecognizer", "onLanguageDetection: $detectedLanguage")
            }
        }
    }

    /**
     * Uses the LibreTranslate API to translate text (Coroutine version).
     * @param text The text to translate.
     * @return The translated text, or an empty string on error.
     */
    private suspend fun translateText(text: String): String = suspendCoroutine { continuation ->
        val mediaType = "application/json".toMediaTypeOrNull()
        val json = JSONObject().apply {
            put("q", text)
            put("source", sourceLanguageCode)
            put("target", targetLanguageCode)
        }.toString()
        val body = json.toRequestBody(mediaType)
        val request = Request.Builder()
            .url(LIBRETRANSLATE_URL)
            .post(body)
            .build()

        okHttpClient.newCall(request).enqueue(object : Callback {  // Use the interface
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Translation", "Failed to translate text: ${e.message}", e)
                continuation.resume("")
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseBody = response.body?.string() ?: ""
                    if (response.isSuccessful) {
                        val jsonResponse = JSONObject(responseBody)
                        val translatedText = jsonResponse.optString("translatedText", "")
                        Log.d("Translation", "Translated text: $translatedText")
                        continuation.resume(translatedText)
                    } else {
                        Log.e("Translation", "Error: ${response.code}, Response Body: $responseBody")
                        continuation.resume("")
                    }
                } catch (e: Exception) {
                    Log.e("Translation", "Error parsing JSON: ${e.message}", e)
                    continuation.resume("")
                } finally {
                    response.close()
                }
            }
        })
    }

    /**
     * Uses the TextToSpeech engine to speak text.
     * @param text The text to speak.
     */
    private fun speak(text: String) {
        if (!ttsInitialized) {
            Log.e("TTS", "TTS engine not initialized")
            onTtsError("TTS Engine not initialized")
            return
        }
        setUtteranceListener()
        val utteranceId = UUID.randomUUID().toString()
        val result = tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        if (result == TextToSpeech.ERROR) {
            Log.e("TTS", "Failed to speak text")
            onTtsError("Failed to speak the translated text.")
        }
    }

    // Cleanup resources when the associated activity/fragment is destroyed.
    fun cleanup() {
        stopSpeechRecognition()
        destroyTextToSpeech()
        coroutineScope.cancel()
    }
}

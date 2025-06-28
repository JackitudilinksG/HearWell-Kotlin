package com.example.hearwell

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.hearwell.ui.theme.HearWellTheme

private const val REQUEST_RECORD_AUDIO_PERMISSION = 200

class TranslateActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HearWellTheme {
                TranslateScreen()
            }
        }
    }
}

@Composable
fun TranslateScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val activity = context as? Activity
    var hasRecordPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    val recognizedText = remember { mutableStateOf("") }
    val isListening = remember { mutableStateOf(false) }

    // Handle permission request
    LaunchedEffect(Unit) {
        if (!hasRecordPermission) {
            ActivityCompat.requestPermissions(
                activity!!,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_RECORD_AUDIO_PERMISSION
            )
            hasRecordPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    // Proper lifecycle management
    DisposableEffect(speechRecognizer) {
        onDispose {
            speechRecognizer.destroy()
        }
    }

    // Check if recognition is available
    if (!SpeechRecognizer.isRecognitionAvailable(context)) {
        Text("Speech recognition not available on this device")
        return
    }

    val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() { isListening.value = false }

        override fun onError(error: Int) {
            isListening.value = false
            recognizedText.value = when (error) {
                SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Please try again."
                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                SpeechRecognizer.ERROR_NETWORK -> "Network error"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                SpeechRecognizer.ERROR_SERVER -> "Server error"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                else -> "Error code: $error"
            }
        }

        override fun onResults(results: Bundle) {
            results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.let {
                recognizedText.value = it.firstOrNull() ?: "No results"
            }
            isListening.value = false
        }

        override fun onPartialResults(partialResults: Bundle) {
            partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.let {
                recognizedText.value = it.firstOrNull() ?: ""
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    speechRecognizer.setRecognitionListener(listener)

    val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
        putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...")
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
    }

    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        Text(
            text = if (hasRecordPermission) "Tap to speak" else "Microphone permission required",
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (hasRecordPermission && !isListening.value) {
                    isListening.value = true
                    speechRecognizer.startListening(recognizerIntent)
                }
            },
            enabled = hasRecordPermission && !isListening.value
        ) {
            Text(if (isListening.value) "Listening..." else "Start Listening")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                speechRecognizer.stopListening()
                isListening.value = false
            },
            enabled = isListening.value
        ) {
            Text("Stop Listening")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Recognized Text:",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = recognizedText.value,
            fontSize = 14.sp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
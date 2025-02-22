package com.example.hearwell_06

import android.app.Activity
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.audiofx.LoudnessEnhancer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hearwell_06.ui.theme.Hearwell_06Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * TestingMain is the main activity for testing frequencies.
 * We now call setVolumeControlStream(AudioManager.STREAM_MUSIC) in onCreate
 * so the hardware volume buttons control the music stream.
 */
class TestingMain : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Allow user to fine-tune volume using the hardware volume buttons.
        setVolumeControlStream(AudioManager.STREAM_MUSIC)
        setContent {
            Hearwell_06Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TestingMainScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

/**
 * TestingMainScreen now includes an EarSelectionRow at the top, so the user can choose
 * between "Left Ear" and "Right Ear". The test then follows with frequency selection and adjustment.
 */
@Composable
fun TestingMainScreen(modifier: Modifier = Modifier) {
    // List of frequencies to test.
    val frequencies = listOf(250.0, 500.0, 1000.0, 2000.0, 4000.0, 8000.0)
    val context = LocalContext.current
    val activity = context as? Activity

    // Lists to store the measured volume (in dB) for each frequency.
    var leftResults by remember { mutableStateOf(List(frequencies.size) { null as Float? }) }
    var rightResults by remember { mutableStateOf(List(frequencies.size) { null as Float? }) }

    // State variables to control the test flow.
    var currentEar by remember { mutableStateOf("left") } // "left" or "right"
    var phase by remember { mutableStateOf("select") }      // "select" or "adjust"
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var currentVolume by remember { mutableStateOf(0.5f) }    // Volume slider value (0f to 1f)

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Ear selection row is available during the frequency selection phase.
            if (phase == "select") {
                EarSelectionRow(currentEar = currentEar, onEarSelected = { ear ->
                    currentEar = ear
                })
            }
            Spacer(modifier = Modifier.height(16.dp))
            when (phase) {
                "select" -> {
                    FrequencySelectionScreen(
                        frequencies = frequencies,
                        ear = currentEar,
                        results = if (currentEar == "left") leftResults else rightResults,
                        onFrequencySelected = { index ->
                            selectedIndex = index
                            currentVolume = 0.5f // Reset slider for new frequency
                            phase = "adjust"
                        },
                        onTestComplete = {
                            if (currentEar == "left") {
                                // If left ear test is complete, switch to right ear.
                                currentEar = "right"
                                phase = "select"
                            } else {
                                // All tests complete: pass results to Audiogram activity.
                                val leftData = leftResults.joinToString(separator = ",") { it.toString() }
                                val rightData = rightResults.joinToString(separator = ",") { it.toString() }
                                val freqData = frequencies.joinToString(separator = ",")
                                val intent = Intent(context, Audiogram::class.java).apply {
                                    putExtra("leftVolumes", leftData)
                                    putExtra("rightVolumes", rightData)
                                    putExtra("frequencies", freqData)
                                }
                                context.startActivity(intent)
                            }
                        }
                    )
                }
                "adjust" -> {
                    // Display the volume adjustment UI for the selected frequency.
                    val freq = frequencies[selectedIndex!!]
                    VolumeAdjustmentScreen(
                        frequency = freq,
                        ear = currentEar,
                        currentVolume = currentVolume,
                        onVolumeChange = { newVolume ->
                            currentVolume = newVolume
                            // Automatically play tone at new volume.
                            CoroutineScope(Dispatchers.IO).launch {
                                playTone(
                                    frequency = freq,
                                    durationMs = 3000,
                                    volume = newVolume,
                                    stereoSide = currentEar.uppercase()
                                )
                            }
                        },
                        onIncrease = {
                            val newValue = (currentVolume + 0.05f).coerceAtMost(1f)
                            currentVolume = newValue
                            CoroutineScope(Dispatchers.IO).launch {
                                playTone(freq, 3000, volume = newValue, stereoSide = currentEar.uppercase())
                            }
                        },
                        onDecrease = {
                            val newValue = (currentVolume - 0.05f).coerceAtLeast(0f)
                            currentVolume = newValue
                            CoroutineScope(Dispatchers.IO).launch {
                                playTone(freq, 3000, volume = newValue, stereoSide = currentEar.uppercase())
                            }
                        },
                        onSetVolume = {
                            // Convert the slider value to decibels and record it.
                            val volumeDb = sliderToDb(currentVolume)
                            if (currentEar == "left") {
                                leftResults = leftResults.toMutableList().also { it[selectedIndex!!] = volumeDb }
                            } else {
                                rightResults = rightResults.toMutableList().also { it[selectedIndex!!] = volumeDb }
                            }
                            selectedIndex = null
                            phase = "select"
                        }
                    )
                }
            }
        }
        // Common Back button to exit the activity.
        Button(
            onClick = { activity?.finish() },
            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
        ) {
            Text("Back")
        }
    }
}

/**
 * EarSelectionRow provides two buttons for choosing the ear to test.
 * The currently selected ear is disabled.
 */
@Composable
fun EarSelectionRow(currentEar: String, onEarSelected: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = { onEarSelected("left") },
            enabled = currentEar != "left"
        ) {
            Text("Left Ear")
        }
        Spacer(modifier = Modifier.width(16.dp))
        Button(
            onClick = { onEarSelected("right") },
            enabled = currentEar != "right"
        ) {
            Text("Right Ear")
        }
    }
}

/**
 * FrequencySelectionScreen displays a grid of frequency buttons.
 * Already tested frequencies are disabled.
 */
@Composable
fun FrequencySelectionScreen(
    frequencies: List<Double>,
    ear: String,
    results: List<Float?>,
    onFrequencySelected: (Int) -> Unit,
    onTestComplete: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "${ear.capitalize()} Ear Test", fontSize = 30.sp)
        Spacer(modifier = Modifier.height(16.dp))
        // Arrange frequency buttons in a grid (2 rows x 3 columns).
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.height(200.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(frequencies) { index, freq ->
                // Disable button if frequency has already been tested.
                val tested = results[index] != null
                Button(
                    onClick = { onFrequencySelected(index) },
                    enabled = !tested,
                    modifier = Modifier.height(60.dp)
                ) {
                    Text("$freq Hz")
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        // When all frequencies are tested, show the Next/Finish button.
        if (results.all { it != null }) {
            Button(onClick = { onTestComplete() }) {
                Text(if (ear == "left") "Next: Right Ear" else "Finish Test")
            }
        }
    }
}

/**
 * VolumeAdjustmentScreen provides UI to adjust the volume for a selected frequency.
 */
@Composable
fun VolumeAdjustmentScreen(
    frequency: Double,
    ear: String,
    currentVolume: Float,
    onVolumeChange: (Float) -> Unit,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onSetVolume: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Adjust volume for $frequency Hz", fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Volume: ${(currentVolume * 100).toInt()}%")
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            Button(onClick = onDecrease) { Text("-") }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = onIncrease) { Text("+") }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onSetVolume) { Text("Set Volume") }
    }
}

/**
 * sliderToDb converts a slider value (0.0 to 1.0) into a decibel level.
 */
fun sliderToDb(value: Float): Float {
    return value * 80f - 80f
}

/**
 * playTone generates and plays a sine wave tone for a given frequency and duration.
 * It directs the output to the specified stereo channel.
 */
fun playTone(
    frequency: Double,
    durationMs: Int,
    sampleRate: Int = 44100,
    volume: Float = 0.5F,
    volumeBoost: Float = 0.0F,
    stereoSide: String = "MIDDLE"
) {
    val minBuffSize = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )
    val player = AudioTrack.Builder()
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        .setAudioFormat(
            AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(sampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()
        )
        .setBufferSizeInBytes(minBuffSize * 4)
        .setTransferMode(AudioTrack.MODE_STREAM)
        .build()
    player.setVolume(volume)
    val enhancer = LoudnessEnhancer(player.audioSessionId)
    enhancer.setTargetGain(volumeBoost.toInt())
    enhancer.enabled = true
    when (stereoSide) {
        "MIDDLE" -> player.setStereoVolume(1.0F, 1.0F)
        "LEFT" -> player.setStereoVolume(1.0F, 0.0F)
        "RIGHT" -> player.setStereoVolume(0.0F, 1.0F)
    }
    player.play()
    val numSamples = (durationMs * sampleRate / 1000)
    val audioData = ShortArray(minBuffSize)
    var sampleIndex = 0
    while (sampleIndex < numSamples) {
        for (i in audioData.indices) {
            if (sampleIndex >= numSamples) break
            audioData[i] = (Short.MAX_VALUE * sin(2.0 * PI * sampleIndex * frequency / sampleRate)).toInt().toShort()
            sampleIndex++
        }
        player.write(audioData, 0, audioData.size)
    }
    player.stop()
    player.release()
}

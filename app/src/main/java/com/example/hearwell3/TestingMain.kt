package com.example.hearwell

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
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hearwell.R
import com.example.hearwell.Audiogram
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import com.example.hearwell.ui.theme.HearWellTheme

class TestingMain : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setVolumeControlStream(AudioManager.STREAM_MUSIC)
        setContent {
            HearWellTheme {
                TestingMainScreen()
            }
        }
    }
}

@Composable
fun TestingMainScreen(modifier: Modifier = Modifier) {
    val frequencies = listOf(250.0, 500.0, 1000.0, 2000.0, 4000.0, 8000.0)
    val context = LocalContext.current
    val activity = context as? Activity

    var leftResults by remember { mutableStateOf(List(frequencies.size) { null as Float? }) }
    var rightResults by remember { mutableStateOf(List(frequencies.size) { null as Float? }) }

    var currentEar by remember { mutableStateOf("left") }
    var phase by remember { mutableStateOf("select") }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var currentVolume by remember { mutableStateOf(0.5f) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

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
                            currentVolume = 0.5f
                            phase = "adjust"
                        },
                        onTestComplete = {
                            if (currentEar == "left") {
                                currentEar = "right"
                                phase = "select"
                            } else {

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
                    val freq = frequencies[selectedIndex!!]
                    VolumeAdjustmentScreen(
                        frequency = freq,
                        ear = currentEar,
                        currentVolume = currentVolume,
                        onVolumeChange = { newVolume ->
                            currentVolume = newVolume
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
        Button(
            onClick = { activity?.finish() },
            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
        ) {
            Text("Back")
        }
    }
}

@Composable
fun EarSelectionRow(currentEar: String, onEarSelected: (String) -> Unit) {
    val painterLeft = painterResource(id = R.drawable.ear_left)
    val painterRight = painterResource(id = R.drawable.ear_right)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 50.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterLeft,
            contentDescription = "Left Ear",
            modifier = Modifier.size(150.dp).clickable { onEarSelected("left") }
        )
        Image(
            painter = painterRight,
            contentDescription = "Right Ear",
            modifier = Modifier.size(150.dp).clickable { onEarSelected("right"); }
        )
    }
}

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
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.height(200.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(frequencies) { index, freq ->
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
        if (results.all { it != null }) {
            Button(onClick = { onTestComplete() }) {
                Text(if (ear == "left") "Next: Right Ear" else "Finish Test")
            }
        }
    }
}

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

fun sliderToDb(value: Float): Float {
    return value * 80f - 80f
}

fun playTone(
    frequency: Double,
    durationMs: Int,
    sampleRate: Int = 44100,
    volume: Float = 0.5f,
    volumeBoost: Float = 0.0f,
    stereoSide: String = "MIDDLE"
) {
    val numSamples = (durationMs * sampleRate / 1000).toInt()
    val fadeSamples = 100 // Samples for fade in/out
    val audioData = ShortArray(numSamples * 2) // Stereo (2 channels)

    for (i in 0 until numSamples) {
        // Apply fade-in and fade-out to prevent clicks
        val fadeFactor = when {
            i < fadeSamples -> i.toDouble() / fadeSamples
            i >= numSamples - fadeSamples -> (numSamples - i).toDouble() / fadeSamples
            else -> 1.0
        }
        val angle = 2.0 * PI * i * frequency / sampleRate
        val sample = (sin(angle) * Short.MAX_VALUE * fadeFactor * volume).toInt().toShort()

        when (stereoSide.uppercase()) {
            "LEFT" -> {
                audioData[i * 2] = sample     // Left channel
                audioData[i * 2 + 1] = 0      // Right channel (silent)
            }
            "RIGHT" -> {
                audioData[i * 2] = 0          // Left channel (silent)
                audioData[i * 2 + 1] = sample // Right channel
            }
            else -> { // MIDDLE
                audioData[i * 2] = sample     // Left channel
                audioData[i * 2 + 1] = sample // Right channel
            }
        }
    }

    val bufferSize = audioData.size * 2 // 2 bytes per Short

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
                .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                .build()
        )
        .setBufferSizeInBytes(bufferSize)
        .setTransferMode(AudioTrack.MODE_STATIC)
        .build()

    player.write(audioData, 0, audioData.size, AudioTrack.WRITE_BLOCKING)

    val enhancer = LoudnessEnhancer(player.audioSessionId).apply {
        setTargetGain(volumeBoost.toInt())
        enabled = true
    }

    player.play()

    // Keep the coroutine alive until playback finishes
    Thread.sleep(durationMs.toLong())

    player.stop()
    player.release()
    enhancer.release()
}


package com.example.newtesting1

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
import com.example.hearwell_06newtesting1.Audiogram
import com.example.newtesting1.ui.theme.NewTesting1Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class TestingMain : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Allow user to fine-tune volume using the hardware volume buttons.
        setVolumeControlStream(AudioManager.STREAM_MUSIC)
        setContent {
            NewTesting1Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TestingMainScreen(modifier = Modifier.padding(innerPadding))
                }
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


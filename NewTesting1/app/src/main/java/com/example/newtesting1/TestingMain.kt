package com.example.newtesting1

import android.app.Activity
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.audiofx.LoudnessEnhancer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.newtesting1.ui.theme.NewTesting1Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class TestingMain : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NewTesting1Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    TestingScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun TestingScreen(modifier: Modifier = Modifier) {
    var side: String = "MIDDLE"
    var vol: Int = 0
    val displayVol = remember { mutableStateOf(50) }
    val context = LocalContext.current
    val activity = context as? Activity

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("HearWell", fontSize = 50.sp, fontWeight = FontWeight.Bold)
            Text("Do not change device volume while testing", fontSize = 10.sp)
            Row {
                Button(onClick = { side = "LEFT" }) { Text("LEFT") }
                Button(onClick = { side = "MIDDLE" }, modifier = Modifier.padding(start = 10.dp, end = 10.dp)) { Text("MIDDLE") }
                Button(onClick = { side = "RIGHT" }) { Text("RIGHT") }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = displayVol.value.toString(), fontSize = 100.sp)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(onClick = { displayVol.value += 5; vol += 500 }) { Text("UP") }
                    Button(onClick = { displayVol.value -= 5; vol -= 500 }) { Text("DOWN") }
                }
            }
            Column {
                Row(
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            playTone(250.0, 3000, volume = 1.0F, volumeBoost = vol.toFloat(), stereoSide = side)
                        }
                    }) { Text("250Hz") }
                    Button(onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            playTone(500.0, 3000, volume = 1.0F, volumeBoost = vol.toFloat(), stereoSide = side)
                        }
                    }, modifier = Modifier.padding(start = 10.dp, end = 10.dp)) { Text("500Hz") }
                    Button(onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            playTone(1000.0, 3000, volume = 1.0F, volumeBoost = vol.toFloat(), stereoSide = side)
                        }
                    }) { Text("1kHz") }
                }
                Row {
                    Button(onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            playTone(2000.0, 3000, volume = 1.0F, volumeBoost = vol.toFloat(), stereoSide = side)
                        }
                    }) { Text("2kHz") }
                    Button(onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            playTone(4000.0, 3000, volume = 1.0F, volumeBoost = vol.toFloat(), stereoSide = side)
                        }
                    }, modifier = Modifier.padding(start = 10.dp, end = 10.dp)) { Text("4kHz") }
                    Button(onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            playTone(8000.0, 3000, volume = 1.0F, volumeBoost = vol.toFloat(), stereoSide = side)
                        }
                    }) { Text("8kHz") }
                }
                Button( onClick = {
                    val intent = Intent(context, TestingRealTime::class.java)
                    context.startActivity(intent)
                }) {
                    Text("RealTime")
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

    // Set volume and boost
    player.setVolume(volume)
    val enhancer = LoudnessEnhancer(player.audioSessionId)
    enhancer.setTargetGain(volumeBoost.toInt())
    enhancer.enabled = true

    // Set stereo output based on selection
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
            audioData[i] = (Short.MAX_VALUE * sin(2.0 * PI * sampleIndex * frequency / sampleRate))
                .toInt().toShort()
            sampleIndex++
        }
        player.write(audioData, 0, audioData.size)
    }
    player.stop()
    player.release()
}

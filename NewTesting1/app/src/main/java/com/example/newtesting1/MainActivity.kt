package com.example.newtesting1

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.audiofx.LoudnessEnhancer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.newtesting1.ui.theme.NewTesting1Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            NewTesting1Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "HearWell Technologies",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    var side : String = "MIDDLE"
    var vol by Remember { mutableStateOf(50)}
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = name,
            modifier = modifier.padding(top = 32.dp),
            style = TextStyle(
                color = Color.Red
            )
        )
        Row {
            Button(onClick = { side = "LEFT" }) { Text("LEFT")}
            Button(onClick = {side = "MIDDLE"}) { Text("MIDDLE")}
            Button(onClick = { side = "RIGHT" }) { Text("RIGHT")}
        }
        Row (
            verticalAlignment = Alignment.CenterVertically
        ){
            Text(
                text = vol.toString(),
                fontSize = 100.sp
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(onClick = { vol += 5 }) { Text("UP") }
                Button(onClick = { vol -= 5 }) { Text("DOWN") }
            }
        }
        Column {
            Row {
                Button(onClick = { // Button 1
                    CoroutineScope(Dispatchers.IO).launch { // Runs in a background thread
                        playTone(250.0, 3000, volume = 1.0F, volumeBoost = 1000F, stereoSide = side)
                    }
                }) {
                    Text("250Hz")
                }
                Button(onClick = { // Button 2
                    CoroutineScope(Dispatchers.IO).launch {
                        playTone(500.0, 3000)
                    }
                }) {
                    Text("500Hz")
                }
                Button(onClick = { // Button 3
                    CoroutineScope(Dispatchers.IO).launch {
                        playTone(1000.0, 3000)
                    }
                }) {
                    Text("1kHz")
                }
            }
            Row {
                Button(onClick = { // Button 4
                    CoroutineScope(Dispatchers.IO).launch {
                        playTone(2000.0, 3000)
                    }
                }) {
                    Text("2kHz")
                }
                Button(onClick = { // Button 5
                    CoroutineScope(Dispatchers.IO).launch {
                        playTone(4000.0, 3000)
                    }
                }) {
                    Text("4kHz")
                }
                Button(onClick = { // Button 6
                    CoroutineScope(Dispatchers.IO).launch {
                        playTone(8000.0, 3000)
                    }
                }) {
                    Text("8kHz")
                }
            }
        }
    }
}

fun playTone(frequency: Double, durationMs: Int, sampleRate: Int = 44100, volume: Float = 0.5F, volumeBoost : Float = 0.0F, stereoSide : String = "MIDDLE") {
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
        .setBufferSizeInBytes(minBuffSize * 4) // Increase buffer size
        .setTransferMode(AudioTrack.MODE_STREAM) // Use streaming mode
        .build()

    // volume
    player.setVolume(volume)

    //volume boost
    val enhancer = LoudnessEnhancer(player.audioSessionId)
    enhancer.setTargetGain(volumeBoost.toInt())
    enhancer.enabled = true

    // change side
    if(stereoSide == "MIDDLE") {
        player.setStereoVolume(1.0F, 1.0F)
    } else if(stereoSide == "LEFT") {
        player.setStereoVolume(1.0F, 0.0F)
    } else if(stereoSide == "RIGHT") {
        player.setStereoVolume(0.0F, 1.0F)
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


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NewTesting1Theme {
        Greeting("Android")
    }
}

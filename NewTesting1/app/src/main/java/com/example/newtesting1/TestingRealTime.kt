package com.example.newtesting1

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.example.newtesting1.ui.theme.NewTesting1Theme
import kotlinx.coroutines.*
import org.jtransforms.fft.DoubleFFT_1D

class TestingRealTime : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NewTesting1Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    RealTime(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@SuppressLint("CoroutineCreationDuringComposition")
@Composable
fun RealTime(modifier: Modifier = Modifier) {
    var audioRecord: AudioRecord? = null
    lateinit var audioTrack: AudioTrack
    var isProcessing = false

    val sampleRate = 44100 // Standard sample rate
    val bufferSize = AudioRecord.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT
    )

    // Request microphone permission if not granted
    if (ActivityCompat.checkSelfPermission(
            LocalContext.current,
            Manifest.permission.RECORD_AUDIO
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        ActivityCompat.requestPermissions(
            LocalContext.current as Activity,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            100
        )
        return
    }

    // Set up AudioRecord and AudioTrack
    audioRecord = AudioRecord(
        MediaRecorder.AudioSource.MIC,
        sampleRate,
        AudioFormat.CHANNEL_IN_MONO,
        AudioFormat.ENCODING_PCM_16BIT,
        bufferSize
    )
    audioTrack = AudioTrack(
        AudioManager.STREAM_MUSIC,
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT,
        bufferSize,
        AudioTrack.MODE_STREAM
    )

    isProcessing = true
    CoroutineScope(Dispatchers.Default).launch {
        val buffer = ShortArray(bufferSize / 2)
        val fftBuffer = DoubleArray(buffer.size * 2) // FFT needs complex numbers

        val fft = DoubleFFT_1D(buffer.size.toLong())

        audioRecord.startRecording()
        audioTrack.play()

        while (isProcessing) {
            val readSize = audioRecord.read(buffer, 0, buffer.size)

            // Convert short array to double array for FFT
            for (i in buffer.indices) {
                fftBuffer[2 * i] = buffer[i].toDouble()  // Real part
                fftBuffer[2 * i + 1] = 0.0              // Imaginary part
            }

            // Apply FFT
            fft.realForward(fftBuffer)

            // 🔹 Modify Frequency Amplitudes
            adjustFrequencies(fftBuffer, buffer.size, sampleRate)

            // Apply Inverse FFT
            fft.realInverse(fftBuffer, true)

            // Convert back to short array
            for (i in buffer.indices) {
                buffer[i] = fftBuffer[2 * i].toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }

            // Play modified sound
            audioTrack.write(buffer, 0, readSize)
        }

        audioRecord.stop()
        audioTrack.stop()
    }
}

private fun adjustFrequencies(fftBuffer: DoubleArray, size: Int, sampleRate: Int) {
    val numFrequencies = size / 2

    for (i in 0 until numFrequencies) {
        val frequency = i * sampleRate / size

        // 🔹 Example: Reduce volume of frequencies between 1000Hz and 3000Hz
        if (frequency in 1000..3000) {
            fftBuffer[2 * i] *= 0.5  // Reduce amplitude by 50%
            fftBuffer[2 * i + 1] *= 0.5
        }

        // 🔹 Example: Boost bass (frequencies < 500Hz)
        if (frequency < 500) {
            fftBuffer[2 * i] *= 1.5  // Increase amplitude by 50%
            fftBuffer[2 * i + 1] *= 1.5
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    NewTesting1Theme {
        RealTime()
    }
}

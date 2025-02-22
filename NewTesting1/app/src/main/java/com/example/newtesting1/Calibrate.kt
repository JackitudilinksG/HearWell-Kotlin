package com.example.newtesting1

import android.app.Activity
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.newtesting1.ui.theme.NewTesting1Theme

class Calibrate : ComponentActivity() {
    private lateinit var mySound: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ensure volume buttons control media volume
        setVolumeControlStream(AudioManager.STREAM_MUSIC)

        // Initialize the media player with the calibration audio resource
        mySound = MediaPlayer.create(this, R.raw.calibrate)

        enableEdgeToEdge()
        setContent {
            NewTesting1Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CalibrationScreen(
                        modifier = Modifier.padding(innerPadding),
                        onPlaySound = { mySound.start() }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mySound.release()
    }
}

@Composable
fun CalibrationScreen(modifier: Modifier = Modifier, onPlaySound: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Let’s get your volume set to the correct levels.", fontSize = 20.sp)
            Button(onClick = { onPlaySound() }) {
                Text("Play Calibration Sound")
            }
            Button(onClick = {
                val intent = Intent(context, TestingMain::class.java)
                context.startActivity(intent)
            }) {
                Text("Continue to Testing", fontSize = 18.sp)
            }
        }
        // Back button at bottom left
        Button(
            onClick = { activity?.finish() },
            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
        ) {
            Text("Back")
        }
    }
}


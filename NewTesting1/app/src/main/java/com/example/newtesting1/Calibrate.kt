package com.example.hearwell_06

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.hearwell_06.ui.theme.Hearwell_06Theme

class Calibrate : ComponentActivity() {
    private lateinit var mySound: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize the media player with the audio resource
        mySound = MediaPlayer.create(this, R.raw.calibrate)

        enableEdgeToEdge()
        setContent {
            Hearwell_06Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    UserCalibrate(
                        modifier = Modifier.padding(innerPadding),
                        onPlaySound = { mySound.start() } // Lambda to play sound
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mySound.release() // Release media player resources
    }
}

@Composable
fun UserCalibrate(modifier: Modifier = Modifier, onPlaySound: () -> Unit) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Let’s get your volume set to the correct levels.",
            fontSize = 20.sp
        )
        Button(onClick = { onPlaySound() }) {
            Text("Play Sound")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CalibratePreview() {
    Hearwell_06Theme {
        UserCalibrate(onPlaySound = {})
    }
}

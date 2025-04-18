package com.example.hearwell

import android.app.Activity
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.hearwell.ui.theme.HearWellTheme

class AudioTest : ComponentActivity() {
    private lateinit var myTestMusic: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setVolumeControlStream(AudioManager.STREAM_MUSIC)

        myTestMusic = MediaPlayer.create(this, R.raw.audio_testing)
        
        setContent {
            HearWellTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AudioTestScreen(
                        modifier = Modifier.padding(innerPadding),
                        onPlaySound = { myTestMusic.start() }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        myTestMusic.release()
    }
}

@Composable
fun AudioTestScreen(modifier: Modifier = Modifier, onPlaySound: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button(onClick = { onPlaySound() }) {
            Icon(imageVector = Icons.Filled.VolumeUp, contentDescription = "Play Sound", modifier = Modifier.padding(end = 8.dp))
            Text("Test Audio Test")
        }
    }
}
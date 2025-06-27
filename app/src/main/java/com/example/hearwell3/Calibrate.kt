package com.example.hearwell

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.example.hearwell.ui.theme.HearWellTheme
import com.example.hearwell.LeftTesting

class Calibrate : ComponentActivity() {
    private lateinit var mySound: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setVolumeControlStream(AudioManager.STREAM_MUSIC)

        mySound = MediaPlayer.create(this, R.raw.calibrate)

        enableEdgeToEdge()
        setContent {
            HearWellTheme {
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
fun OpenYouTubeThumbnail() {
    val context = LocalContext.current
    val videoId = "dQw4w9WgXcQ" // replace with your own video ID
    val youtubeAppUri = Uri.parse("vnd.youtube:$videoId")
    val youtubeWebUri = Uri.parse("https://www.youtube.com/watch?v=$videoId")
    Image(
        painter = painterResource(id = R.drawable.youtube_video_clickable),
        contentDescription = "Play Video",
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(height = 231.dp, width = 410.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                // Try to open YouTube app
                val appIntent = Intent(Intent.ACTION_VIEW, youtubeAppUri)
                val webIntent = Intent(Intent.ACTION_VIEW, youtubeWebUri)

                try {
                    context.startActivity(appIntent)
                } catch (e: ActivityNotFoundException) {
                    // Fallback to browser
                    context.startActivity(webIntent)
                }
            }
    )
}


@Composable
fun CalibrationScreen(modifier: Modifier = Modifier, onPlaySound: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFDADDF2))
    ) {
        Image(
            painter = painterResource(id = R.drawable.background_image),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
        )
        Column(
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(start = 16.dp, end = 16.dp, top = 80.dp)
                .fillMaxSize()
        ) {
            Text(
                text = "Calibration Phase",
                fontSize = 48.sp,
                style = TextStyle(
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFDADDF1),
                    lineHeight = 42.sp
                )
            )
            Text(
                text = "Perform the Calibration phase before testing your hearing capacity. The calibration allows us to accurately gauge your hearing capacity.",
                fontSize = 20.sp,
                style = TextStyle(
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFFDADDF1),
                    lineHeight = 22.sp,
                )
            )
            Text(
                text = "Watch this tutorial on how to perform the calibration before you begin",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 15.dp, top = 20.dp),
                style = TextStyle(
                    fontWeight = FontWeight.Light,
                    color = Color(0xFFDADDF1),
                    lineHeight = 26.sp,
                    textAlign = TextAlign.Right,
                    fontSize = 32.sp
                )
            )

            OpenYouTubeThumbnail()

            Text(
                text = "Wear your earphones and play the calibration audio according to the video",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                style = TextStyle(
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF161A36),
                    lineHeight = 26.sp,
                    textAlign = TextAlign.Left,
                    fontSize = 32.sp
                )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ){
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Button(
                        onClick = { onPlaySound() },
                        modifier = Modifier
                            .background(color = Color.Transparent),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF161A36))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.VolumeUp,
                            contentDescription = "Play Sound",
                            tint = Color(0xFFDADDF1),
                            modifier = Modifier
                                .padding(end = 8.dp)
                        )
                        Text(
                            "Play Calibration Sound",
                            style = TextStyle(
                                fontSize = 18.sp,
                                color = Color(0xFFDADDF1)
                            )
                        )
                    }
                    Button(
                        onClick = {
                            val intent = Intent(context, LeftTesting::class.java)
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF161A36))
                    ) {
                        Text("Continue to Testing", fontSize = 18.sp)
                    }
                }
            }
        }
        Button(
            onClick = { activity?.finish() },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF161A36))
        ) {
            Text("Back")
        }
    }
}

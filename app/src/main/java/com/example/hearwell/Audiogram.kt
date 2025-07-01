package com.example.hearwell

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hearwell.ui.theme.HearWellTheme
import androidx.core.graphics.createBitmap

class Audiogram : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rightVolumes = intent.getIntArrayExtra("rightVolumes")
        val leftVolumes = intent.getIntArrayExtra("leftVolumes")

        setContent {
            HearWellTheme {
                AudiogramScreen(leftVolumes, rightVolumes)
            }
        }
    }
}

@Composable
fun AudiogramScreen(leftVolumes: IntArray?, rightVolumes: IntArray?) {
    var frequencies = intArrayOf(125, 250, 500, 1000, 2000, 4000, 8000)
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
            modifier = Modifier
                .align(Alignment.Center)
                .padding(start = 16.dp, end = 16.dp, top = 80.dp)
                .fillMaxSize()
        ) {
            Text(
                text = "Audiogram Result",
                fontSize = 46.sp,
                style = TextStyle(
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFDADDF1),
                    lineHeight = 42.sp
                )
            )
            Text(
                text = "The audiogram represents your hearing ability with respect to frequency and volume(in decibels)",
                fontSize = 20.sp,
                style = TextStyle(
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFFDADDF1),
                    lineHeight = 22.sp,
                )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f) // or set fixed height to match the background image
                    .padding(top = 16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.graph_bg),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier.fillMaxSize()
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val frequencies = intArrayOf(125, 250, 500, 1000, 2000, 4000, 8000)
                    val left = leftVolumes
                    val right = rightVolumes

                    if (left != null && right != null && left.size == frequencies.size && right.size == frequencies.size) {
                        val maxDb = 120f
                        val dbHeight = size.height / maxDb

                        fun mapY(db: Float): Float = db * dbHeight
                        val yOffset = 10f

                        for (i in frequencies.indices) {
                            val leftPadding = 180f  // adjust this to your liking
                            val rightPadding = 40f
                            val usableWidth = size.width - leftPadding - rightPadding
                            val widthPerPoint = usableWidth / (frequencies.size - 1)
                            val x = leftPadding + (i * widthPerPoint)


                            // Draw left (X)
                            val y = mapY(leftVolumes?.get(i)!!.toFloat()) + yOffset
                            drawLine(
                                color = Color.Green,
                                start = Offset(x - 10f, y - 10f),
                                end = Offset(x + 10f, y + 10f),
                                strokeWidth = 4f
                            )
                            drawLine(
                                color = Color.Green,
                                start = Offset(x + 10f, y - 10f),
                                end = Offset(x - 10f, y + 10f),
                                strokeWidth = 4f
                            )

                            // Draw right (O)
                            val yRight = mapY(right[i].toFloat())
                            drawCircle(
                                color = Color.Red,
                                radius = 10f,
                                center = Offset(x, yRight),
                                style = androidx.compose.ui.graphics.drawscope.Fill
                            )
                        }
                    }
                }
            }

        }
}
}
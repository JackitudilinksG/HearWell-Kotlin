package com.example.hearwell

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hearwell.ui.theme.HearWellTheme
import androidx.core.graphics.createBitmap

/**
 * Audiogram Activity receives the measured volume (in dB) for left and right ears
 * along with the frequency list. It displays a graph of frequency versus volume.
 */

class Audiogram : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retrieve the data from Intent extras.
        val leftData = intent.getStringExtra("leftVolumes") ?: ""
        val rightData = intent.getStringExtra("rightVolumes") ?: ""
        val freqData = intent.getStringExtra("frequencies") ?: ""
        val leftVolumes = leftData.split(",").mapNotNull { it.toFloatOrNull() }
        val rightVolumes = rightData.split(",").mapNotNull { it.toFloatOrNull() }
        val frequencies = freqData.split(",").mapNotNull { it.toDoubleOrNull() }
        setContent {
            HearWellTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AudiogramScreen(
                        modifier = Modifier.padding(innerPadding),
                        frequencies = frequencies,
                        leftVolumes = leftVolumes,
                        rightVolumes = rightVolumes
                    )
                }
            }
        }
    }
}

/**
 * AudiogramScreen draws the frequency versus volume graph on a Canvas.
 * It also includes a button to save the graph as an image and a Back button.
 */
@Composable
fun AudiogramScreen(
    modifier: Modifier = Modifier,
    frequencies: List<Double>,
    leftVolumes: List<Float>,
    rightVolumes: List<Float>
) {
    val context = LocalContext.current
    val activity = remember { context as? Activity }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Audiogram",
            fontSize = 30.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(16.dp))
        GraphView(
            frequencies = frequencies,
            leftVolumes = leftVolumes,
            rightVolumes = rightVolumes,
            modifier = Modifier.fillMaxWidth().height(300.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                val width = 600
                val height = 400
                val bitmap = createBitmap(width, height)
                if (saveBitmapToGallery(bitmap, context)) {
                    Toast.makeText(context, "Graph saved as image", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Error saving image", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Save Graph as Image")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { activity?.finish() },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Back")
        }
    }
}

/**
 * GraphView draws the frequency vs. volume graph.
 * It maps the frequencies to the x-axis and normalizes the dB values (assumed to be between -80 and 0 dB)
 * to the y-axis.
 */
@Composable
fun GraphView(
    frequencies: List<Double>,
    leftVolumes: List<Float>,
    rightVolumes: List<Float>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        // Draw axes.
        drawLine(
            color = Color.Black,
            start = Offset(40f, canvasHeight - 40f),
            end = Offset(canvasWidth - 20f, canvasHeight - 40f),
            strokeWidth = 4f
        )
        drawLine(
            color = Color.Black,
            start = Offset(40f, canvasHeight - 40f),
            end = Offset(40f, 20f),
            strokeWidth = 4f
        )
        // Map frequencies to x-axis.
        val minFreq = frequencies.minOrNull() ?: 250.0
        val maxFreq = frequencies.maxOrNull() ?: 8000.0
        val xScale = (canvasWidth - 60f) / (maxFreq - minFreq).toFloat()
        // Normalize dB values (assumed between -80 and 0 dB) to [0,1].
        fun normalizeDb(db: Float): Float = (db + 80f) / 80f
        val yScale = (canvasHeight - 60f)
        // Draw left ear graph in red.
        var prevPoint: Offset? = null
        frequencies.forEachIndexed { index, freq ->
            val x = 40f + ((freq - minFreq).toFloat() * xScale)
            val norm = normalizeDb(leftVolumes.getOrElse(index) { -80f })
            val y = canvasHeight - 40f - norm * yScale
            val currentPoint = Offset(x, y)
            drawCircle(color = Color.Red, radius = 6f, center = currentPoint)
            if (prevPoint != null) {
                drawLine(color = Color.Red, start = prevPoint!!, end = currentPoint, strokeWidth = 4f)
            }
            prevPoint = currentPoint
        }
        // Draw right ear graph in blue.
        prevPoint = null
        frequencies.forEachIndexed { index, freq ->
            val x = 40f + ((freq - minFreq).toFloat() * xScale)
            val norm = normalizeDb(rightVolumes.getOrElse(index) { -80f })
            val y = canvasHeight - 40f - norm * yScale
            val currentPoint = Offset(x, y)
            drawCircle(color = Color.Blue, radius = 6f, center = currentPoint)
            if (prevPoint != null) {
                drawLine(color = Color.Blue, start = prevPoint!!, end = currentPoint, strokeWidth = 4f)
            }
            prevPoint = currentPoint
        }
    }
}

/**
 * saveBitmapToGallery saves a bitmap image to the device's gallery.
 * For Android Q and above, it uses MediaStore; for older versions, it writes to an external files directory.
 */
fun saveBitmapToGallery(bitmap: Bitmap, context: Context): Boolean {
    val filename = "audiogram_${System.currentTimeMillis()}.png"
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/HearWell")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { outStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
                }
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                context.contentResolver.update(uri, values, null, null)
                true
            } ?: false
        } else {
            val imagesDir = context.getExternalFilesDir("Pictures")
            val imageFile = java.io.File(imagesDir, filename)
            imageFile.outputStream().use { outStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)
            }
            true
        }
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}


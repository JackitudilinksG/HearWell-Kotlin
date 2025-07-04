package com.example.hearwell

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import com.example.hearwell.ui.theme.HearWellTheme
import kotlinx.coroutines.launch

class RightTesting : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Calling leftVolumes
        val leftVolumes = intent.getIntArrayExtra("leftVolumes")

        enableEdgeToEdge()
        setContent {
            HearWellTheme {
                RightTestingPage(leftVolumes = leftVolumes)
            }
        }
    }
    override fun onDestroy() {
        super.onDestroy()
    }
}

// Data class to represent a frequency item
data class RightFrequencyItem(val hz: Int, val isSelected: Boolean = false)

@Composable
fun RightFrequencyItemCard(frequency: RightFrequencyItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .background(Color.Transparent),
        colors = CardDefaults.cardColors(Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${frequency.hz}Hz",
                fontSize = if (frequency.isSelected) 32.sp else 20.sp,
                lineHeight = 40.sp,
                fontWeight = if (frequency.isSelected) FontWeight.ExtraBold else FontWeight.Light,
                color = Color(0xFF161A36)
            )
            Spacer(modifier = Modifier.height(4.dp))
            // This is a simplified representation of the vertical line
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(if (frequency.isSelected) 300.dp else 150.dp) // Taller if selected
                    .background(Color(0xFF161A36))
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
fun RightFrequencySelectorLazyRow(): Map<Int, Int> {
    val volumeMap = remember {
        mutableStateMapOf(
            125 to 40,
            250 to 45,
            500 to 50,
            1000 to 55,
            2000 to 60,
            4000 to 65,
            8000 to 70
        )
    }

    val frequencies = remember {
        mutableStateListOf(
            RightFrequencyItem(125),
            RightFrequencyItem(250),
            RightFrequencyItem(500),
            RightFrequencyItem(1000),
            RightFrequencyItem(2000),
            RightFrequencyItem(4000),
            RightFrequencyItem(8000),
        )
    }

    // State to hold the currently selected frequency index
    var selectedFrequencyIndex by remember { mutableIntStateOf(0) }
    val currentFreq = frequencies[selectedFrequencyIndex].hz

    // Update the `isSelected` state for the list based on `selectedFrequencyIndex`
    LaunchedEffect(selectedFrequencyIndex) {
        val newList = frequencies.mapIndexed { index, item ->
            item.copy(isSelected = index == selectedFrequencyIndex)
        }
        frequencies.clear()
        frequencies.addAll(newList)
    }

    // 3. Create and remember the LazyListState
    val lazyListState = rememberLazyListState()

    // Used to measure the width of the LazyRow container and individual items
    var lazyRowWidthPx by remember { mutableIntStateOf(0) }
    var itemWidthPx by remember { mutableIntStateOf(0) } // Assuming all items have similar width

    // Coroutine scope for launching scroll actions
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyRow(
            state = lazyListState,
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(450.dp)
                .padding(top = 16.dp, bottom = 16.dp)
                .background(Color.Transparent)
                .onGloballyPositioned { coordinates ->
                    lazyRowWidthPx = coordinates.size.width
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            itemsIndexed(frequencies) { index, item ->
                RightFrequencyItemCard(
                    frequency = item,
                    onClick = {
                        selectedFrequencyIndex = index
                        coroutineScope.launch {
                            if (itemWidthPx > 0 && lazyRowWidthPx > 0) {
                                val centerOffsetPx = (lazyRowWidthPx - itemWidthPx) / 2
                                lazyListState.animateScrollToItem(index, scrollOffset = -centerOffsetPx)
                            }
                        }
                    },
                    modifier = Modifier
                        .width(120.dp)
                        .onGloballyPositioned { coordinates ->
                            // Capture the width of an item (assuming all items are similar)
                            if (itemWidthPx == 0) {
                                itemWidthPx = coordinates.size.width
                            }
                        }
                )
            }
        }
        // Decibel Adjustment Section (simplified)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16))
                .background(Color(0xFF161A36)),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    volumeMap[currentFreq] = maxOf((volumeMap[currentFreq] ?: 0) - 5, 0)
                },
                shape = RoundedCornerShape(6),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF2F2F2)),
                contentPadding = PaddingValues(horizontal = 15.dp, vertical = 4.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = "-5dB",
                    style = TextStyle(
                        color = Color(0xFF161A36),
                        textAlign = TextAlign.Center,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    modifier = Modifier
                        .padding(top = 2.dp, bottom = 2.dp, start = 10.dp, end = 10.dp)
                )
            }
            Text(
                text = volumeMap[currentFreq].toString()+"dB",
                style = TextStyle(
                    color = Color(0xFFDADDF1),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.padding(top = 10.dp, bottom = 10.dp)
            )
            Button(
                onClick = {
                    volumeMap[currentFreq] = maxOf((volumeMap[currentFreq] ?: 0) + 5, 0)
                    Log.d("volumeMap", volumeMap.toMap().toString())
                },
                shape = RoundedCornerShape(6),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF2F2F2)),
                contentPadding = PaddingValues(horizontal = 15.dp, vertical = 4.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = "+5dB",
                    style = TextStyle(
                        color = Color(0xFF161A36),
                        textAlign = TextAlign.Center,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    modifier = Modifier
                        .padding(top = 2.dp, bottom = 2.dp, start = 10.dp, end = 10.dp)
                )
            }
        }
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(color = Color(0xFF161A36))
        ) {
            val currentVolume = volumeMap[currentFreq] ?: 0
            val statusText = when {
                currentVolume in 0..20 -> Pair("0–20dB is considered ", "normal" to Color(0xFF31E157))
                currentVolume in 21..40 -> Pair("20–40dB is considered ", "moderate" to Color(0xFFDEE131))
                currentVolume > 40 -> Pair("40+dB is considered ", "severe" to Color(0xFFFF0000))
                else -> Pair("", "Error has occurred" to Color.Gray)
            }
            Text(
                text = buildAnnotatedString {
                    append(statusText.first)
                    withStyle(style = SpanStyle(color = statusText.second.second, fontWeight = FontWeight.SemiBold)) {
                        append(statusText.second.first)
                    }
                },
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.sp,
                color = Color(0xFFF2F2F2),
                modifier = Modifier.padding(start = 30.dp, end = 30.dp, top = 8.dp, bottom = 8.dp)
            )
        }
    }
    return volumeMap
}

@Composable
fun RightTestingPage(leftVolumes: IntArray?) {
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

        val orderedFrequencies = listOf(125, 250, 500, 1000, 2000, 4000, 8000)
        var rightVolumeMap: Map<Int, Int> by remember { mutableStateOf(emptyMap()) }

        Column(
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(start = 16.dp, end = 16.dp, top = 80.dp)
                .fillMaxSize()
        ) {
            Text(
                text = "Right Ear Side Testing",
                fontSize = 48.sp,
                style = TextStyle(
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFDADDF1),
                    lineHeight = 42.sp
                )
            )
            Text(
                text = "Increase or decrease the decibel value of the specific frequency till it is barely audible.",
                fontSize = 20.sp,
                style = TextStyle(
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFFDADDF1),
                    lineHeight = 22.sp,
                )
            )
            rightVolumeMap = RightFrequencySelectorLazyRow()
        }

        val rightVolumes = orderedFrequencies.map{rightVolumeMap[it]?:0}

        Button(
            onClick = { activity?.finish() },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF161A36))
        ) {
            Text("Back")
        }
        Button(
            onClick = {
                val intent = Intent(context, Audiogram::class.java)
                intent.putExtra("rightVolumes", rightVolumes.toIntArray())
                intent.putExtra("leftVolumes", leftVolumes)
                Log.d("volumes", "RIGHT: ${rightVolumes.joinToString()} \nLEFT: ${leftVolumes?.joinToString()}")
                context.startActivity(intent)
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF161A36))
        ) {
            Text("Continue")
        }
    }
}
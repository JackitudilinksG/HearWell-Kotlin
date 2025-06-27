package com.example.hearwell

import android.os.Bundle
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.hearwell.ui.theme.HearWellTheme
import kotlinx.coroutines.launch

class RightTesting : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HearWellTheme {
                RightTestingPage()
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
fun RightFrequencySelectorLazyRow() {
    val volumeMap = remember {
        mutableStateMapOf(
            125 to 10,
            250 to 10,
            500 to 10,
            1000 to 10,
            2000 to 10,
            4000 to 10,
            8000 to 10
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

    val density = LocalDensity.current

    // State to hold the currently selected frequency index
    var selectedFrequencyIndex by remember { mutableStateOf(0) }
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
    var lazyRowWidthPx by remember { mutableStateOf(0) }
    var itemWidthPx by remember { mutableStateOf(0) } // Assuming all items have similar width

    // Coroutine scope for launching scroll actions
    val coroutineScope = rememberCoroutineScope()
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

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
                .fillMaxWidth().height(450.dp)
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
    }
}

@Composable
fun RightTestingPage() {
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
                text = "Left Ear Side Testing",
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
            RightFrequencySelectorLazyRow()
        }
    }
}
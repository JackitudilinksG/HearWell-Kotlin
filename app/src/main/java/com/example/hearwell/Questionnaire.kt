package com.example.hearwell

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.hearwell.ui.theme.HearWellTheme

class Questionnaire : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HearWellTheme {
                QuestionnaireScreen()
            }
        }
    }
}

@Composable
fun QuestionnaireScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
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
    }
    Column(
        modifier = Modifier.padding(top = 90.dp, start = 16.dp, end = 16.dp)
    ) {
        Text(
            text = "Some Questions before we begin.",
            fontSize = 48.sp,
            style = TextStyle(
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFDADDF1)
            )
        )
        Text(
            text = "Have you recently had a significant change in hearing loss?",
            modifier = Modifier.padding(top = 80.dp),
            fontSize = 25.sp,
            style = TextStyle(
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF161A36)
            )
        )
        Row(
            modifier = Modifier
                .padding(top = 20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { print("CLICKED YES")},
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF303263)),
            ) {
                Text(text = "Yes", color = Color.White)
            }
            Spacer(modifier = Modifier.width(32.dp))
            Button(
                onClick = { print("CLICKED YES")},
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF303263)),
            ) {
                Text(text = "No", color = Color.White)
            }
        }

        Text(
            text = "Are you experiencing pain in your ears?",
            modifier = Modifier.padding(top = 30.dp),
            fontSize = 25.sp,
            style = TextStyle(
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF161A36)
            )
        )
        Row(
            modifier = Modifier
                .padding(top = 20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { print("CLICKED YES")},
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF303263)),
            ) {
                Text(text = "Yes", color = Color.White)
            }
            Spacer(modifier = Modifier.width(32.dp))
            Button(
                onClick = { print("CLICKED YES")},
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF303263)),
            ) {
                Text(text = "No", color = Color.White)
            }
        }

        Text(
            text = "Are you suffering from dizziness or vertigo?",
            modifier = Modifier.padding(top = 30.dp),
            fontSize = 25.sp,
            style = TextStyle(
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF161A36)
            )
        )
        Row(
            modifier = Modifier
                .padding(top = 20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { print("CLICKED YES")},
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF303263)),
            ) {
                Text(text = "Yes", color = Color.White)
            }
            Spacer(modifier = Modifier.width(32.dp))
            Button(
                onClick = { print("CLICKED YES")},
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF303263)),
            ) {
                Text(text = "No", color = Color.White)
            }
        }

        Box(
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
            contentAlignment = Alignment.Center,
        ) {
            Button(
                onClick = {
                    val intent = Intent(context, Calibrate::class.java)
                    context.startActivity(intent)
                },
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF303263)),
            ) {
                Text(
                    text = "Continue ->",
                    color = Color.White,
                    style = TextStyle(
                        fontSize = 20.sp
                    )
                )
            }
        }
    }
}

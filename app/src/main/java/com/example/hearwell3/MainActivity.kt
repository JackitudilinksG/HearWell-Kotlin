package com.example.hearwell

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hearwell.R
import com.example.hearwell.ui.theme.HearWellTheme
import com.example.hearwell.Calibrate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HearWellTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WelcomeScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun WelcomeScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val activity = context as? Activity
    
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Atlas", fontSize = 70.sp, style = TextStyle(
                fontWeight = FontWeight.Normal
            ))
            Text("By HearWell", modifier= Modifier.padding(bottom = 8.dp), fontSize = 23.sp, style = TextStyle(
                fontWeight = FontWeight.Light
            ))
            val painter = painterResource(id = R.drawable.logo)
            Image(
                painter = painter,
                contentDescription = "Logo",
                modifier = Modifier.size(150.dp)
            )
            Text(
                modifier = Modifier.padding(top = 20.dp),
                text = "Hear Well, Live Better",
                fontSize = 20.sp,
                color = Color(0xFF787677),
                style = TextStyle(
                    fontWeight = FontWeight.ExtraLight,
                ))
            Button(onClick = {
                val intent = Intent(context, HomePage::class.java)
                context.startActivity(intent)
            }, modifier = Modifier.padding(top = 350.dp, bottom = 25.dp)
                .size(250.dp, 35.dp)
                .border(
                    BorderStroke(2.dp, Color.Black),
                    shape = RoundedCornerShape(15.dp)
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFFDFE),
                    contentColor = Color(0xFF475199)
                )) {
                Text("I ALREADY HAVE AN ACCOUNT")
            }
            Button(onClick = {
                val intent = Intent(context, Calibrate::class.java)
                context.startActivity(intent)
            }, modifier = Modifier
                .size(250.dp, 35.dp)
                .border(
                    BorderStroke(0.dp, Color.Black),
                    shape = RoundedCornerShape(15.dp)
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF475199),
                    contentColor = Color(0xFFFFFDFE)
                )) {
                Text("GET STARTED")
            }
        }
    }
}
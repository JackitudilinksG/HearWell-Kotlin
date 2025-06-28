package com.example.hearwell

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.*
import com.example.hearwell.ui.theme.HearWellTheme
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.remember

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
fun LoginSignUpToggle(
    selected: String,
    onSelectedChange: (String) -> Unit
) {
    val shape = RoundedCornerShape(20.dp)

    Row(
        modifier = Modifier
            .padding(top = 16.dp, start = 32.dp, end = 32.dp)
            .background(color = Color(0xFFEFEFFF), shape = shape)
            .clip(shape)
    ) {
            // Login Button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (selected == "Login") Color.White else Color(0xFFEFEFFF),
                        shape = shape
                    )
                    .clickable { onSelectedChange("Login") }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Login",
                    color = if (selected == "Login") Color.Black else Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }

            // Sign Up Button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        if (selected == "Sign Up") Color.White else Color(0xFFEFEFFF),
                        shape = shape
                    )
                    .clickable { onSelectedChange("Sign Up") }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Sign Up",
                    color = if (selected == "Sign Up") Color.Black else Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
        }
}

@Composable
fun WelcomeScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var selectedTab = remember { mutableStateOf("Login") }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.hearwell_logo_dark),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(450.dp)
                .align(Alignment.TopEnd)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Atlas",
                modifier = Modifier.padding(top = 100.dp),
                fontSize = 120.sp,
                style = TextStyle(
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF262B54)
                )
            )
            Text(
                text = "By HearWell",
                modifier = Modifier.padding(bottom = 8.dp),
                fontSize = 23.sp,
                style = TextStyle(
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF262B54)
                )
            )
            Text(
                text = "Hear Well, Live Better",
                modifier = Modifier.padding(bottom = 20.dp),
                fontSize = 20.sp,
                style = TextStyle(
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF262B54)
                )
            )

            LoginSignUpToggle(
                selected = selectedTab.value,
                onSelectedChange = { selectedTab.value = it }
            )

            Button(
                onClick = {
                    val intent = Intent(context, HomePage::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .padding(top = 50.dp, bottom = 25.dp)
                    .size(250.dp, 35.dp)
                    .border(
                        BorderStroke(2.dp, Color.Black),
                        shape = RoundedCornerShape(15.dp)
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFFDFE),
                    contentColor = Color(0xFF475199)
                )
            ) {
                Text("I ALREADY HAVE AN ACCOUNT")
            }

            Button(
                onClick = {
                    val intent = Intent(context, Questionnaire::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .size(250.dp, 35.dp)
                    .border(
                        BorderStroke(0.dp, Color.Black),
                        shape = RoundedCornerShape(15.dp)
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF475199),
                    contentColor = Color(0xFFFFFDFE)
                )
            ) {
                Text("GET STARTED")
            }
        }
    }
}

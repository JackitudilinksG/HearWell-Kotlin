package com.example.hearwell

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hearwell.ui.theme.HearWellTheme
import com.google.firebase.firestore.FirebaseFirestore


class UserActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HearWellTheme {
                    UserActivityFun()
                }
            }
        }
}
@Preview


@Composable
fun UserActivityFun() {
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }

    // Get context for Toast
    val context = LocalContext.current

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Name TextField
            TextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Enter your name") },
                modifier = Modifier
                    .width(250.dp)
                    .padding(bottom = 16.dp) // Space between fields
            )

            // Age TextField
            TextField(
                value = age,
                onValueChange = { age = it },
                placeholder = { Text("Enter your age") },
                modifier = Modifier
                    .width(250.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))
            val db = FirebaseFirestore.getInstance() // Firestore instance

            // Save Button
            Button(onClick = {
                val ageInt = age.toIntOrNull() // Convert age to integer
                if (name.isNotEmpty() && ageInt != null && ageInt > 0) {
                    val user = hashMapOf(
                        "name" to name,
                        "age" to ageInt,
                        "mall" to listOf("50","50","50"),
                        "createdAt" to System.currentTimeMillis()
                    )
                    db.collection("users")
                        .add(user)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Saved to Firestore!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(context, "Please enter a valid name and age", Toast.LENGTH_SHORT).show()
                }
            }) {
                Text("Save")
            }



        }
    }
}
package com.example.hearwell

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hearwell.ui.theme.HearWellTheme
import com.google.firebase.firestore.FirebaseFirestore

data class EarData(
    val Left: List<String> = listOf(),
    val Right: List<String> = listOf()
)

class UserProfile : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HearWellTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    UserProfileScreen()
                }
            }
        }
    }
}

@Preview
@Composable
fun UserProfileScreen() {
    val db = FirebaseFirestore.getInstance()
    val documentId = "Fp3EZY5gJsp41gOj7R5k"

    var name by remember { mutableStateOf("Loading...") }
    var age by remember { mutableStateOf(0) }
    var earDataList by remember { mutableStateOf(listOf<EarData>()) }

    fun fetchData() {
        db.collection("users").document(documentId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    name = document.getString("name") ?: "No Name"
                    age = document.getLong("age")?.toInt() ?: 0

                    val rawList = document.get("earData") as? List<Map<String, List<String>>>
                    val parsedList = rawList?.map {
                        EarData(
                            Left = it["Left"] ?: emptyList(),
                            Right = it["Right"] ?: emptyList()
                        )
                    } ?: emptyList()

                    earDataList = parsedList
                }
            }
    }

    LaunchedEffect(Unit) {
        fetchData()
    }

    fun updatePreset4() {
        db.collection("users").document(documentId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val rawList = document.get("earData") as? List<Map<String, List<String>>>
                    val existingList = rawList?.map {
                        EarData(
                            Left = it["Left"] ?: emptyList(),
                            Right = it["Right"] ?: emptyList()
                        )
                    }?.toMutableList() ?: mutableListOf()

                    val globalLeft = leftResultsGlobal.map { it?.toString() ?: "" }
                    val globalRight = rightResultsGlobal.map { it?.toString() ?: "" }

                    if (existingList.size >= 4) {
                        existingList[3] = EarData(globalLeft, globalRight)
                    } else {
                        while (existingList.size < 4) {
                            existingList.add(EarData(emptyList(), emptyList()))
                        }
                        existingList[3] = EarData(globalLeft, globalRight)
                    }

                    val updatedList = existingList.map {
                        mapOf("Left" to it.Left, "Right" to it.Right)
                    }

                    db.collection("users").document(documentId)
                        .update("earData", updatedList)
                        .addOnSuccessListener {
                            println("Preset 4 successfully updated.")
                            fetchData() // Refresh UI
                        }
                        .addOnFailureListener {
                            println("Failed to update Preset 4: $it")
                        }
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text("Name: $name", fontSize = 24.sp, color = Color.Black, modifier = Modifier.padding(top = 40.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text("Age: $age", fontSize = 24.sp, color = Color.Black)
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Presets:",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (earDataList.isNotEmpty()) {
            earDataList.forEachIndexed { index, earData ->
                PresetBox(
                    title = "Preset ${index + 1}",
                    data = "Left: ${earData.Left.joinToString(", ")}\nRight: ${earData.Right.joinToString(", ")}"
                )
            }
        } else {
            PresetBox(title = "No Presets", data = "No data found.")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { updatePreset4() },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Update Preset 4 from Global")
        }
    }
}

@Composable
fun PresetBox(title: String, data: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(Color(0xFF1E2244), shape = RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(text = title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = data, color = Color.White, fontSize = 14.sp)
    }
}

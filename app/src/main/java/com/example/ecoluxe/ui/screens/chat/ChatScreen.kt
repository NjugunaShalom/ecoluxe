package com.example.ecoluxe.ui.screens.chat

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.*

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val senderId: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun ChatScreen(navController: NavController, chatId: String) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: ""

    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var messageText by remember { mutableStateOf(TextFieldValue("")) }

    // Listen for new messages
    LaunchedEffect(chatId) {
        db.collection("chats").document(chatId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    messages = snapshot.documents.mapNotNull { doc ->
                        val data = doc.data ?: return@mapNotNull null
                        ChatMessage(
                            id = doc.id,
                            senderId = data["senderId"] as? String ?: "",
                            text = data["text"] as? String ?: "",
                            timestamp = data["timestamp"] as? Long ?: 0L
                        )
                    }
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F6FF))
            .padding(16.dp)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
                val isMine = message.senderId == userId
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = if (isMine) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isMine) Color(0xFFB57EDC) else Color(0xFFE0E0E0)
                    ) {
                        Text(
                            text = message.text,
                            modifier = Modifier.padding(12.dp),
                            color = if (isMine) Color.White else Color.Black
                        )
                    }
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { Text("Type a message") },
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = {
                    val text = messageText.text.trim()
                    if (text.isNotEmpty()) {
                        val message = hashMapOf(
                            "senderId" to userId,
                            "text" to text,
                            "timestamp" to System.currentTimeMillis()
                        )
                        db.collection("chats").document(chatId)
                            .collection("messages")
                            .add(message)
                            .addOnSuccessListener { messageText = TextFieldValue("") }
                            .addOnFailureListener {
                                Toast.makeText(context, "Failed to send", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }
    }
}

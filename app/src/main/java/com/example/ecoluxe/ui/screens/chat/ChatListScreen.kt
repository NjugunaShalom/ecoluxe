package com.example.ecoluxe.ui.screens.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ecoluxe.R
import com.example.ecoluxe.navigation.ROUTE_CHAT
import java.text.SimpleDateFormat
import java.util.*

data class ChatPreview(
    val chatId: String,
    val name: String,
    val lastMessage: String,
    val timestamp: Long,
    val unreadCount: Int,
    val profilePic: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(navController: NavController) {
    val chats = remember {
        listOf(
            ChatPreview("1", "Sharon Kimani", "Hey! Are the heels still available?", System.currentTimeMillis(), 2, R.drawable.ecoluxe_logo),
            ChatPreview("2", "Liam Mugo", "Great, thanks! Will swap it soon.", System.currentTimeMillis() - 3600000, 0, R.drawable.ecoluxe_logo),
            ChatPreview("3", "EcoStylish", "Your upcycled top is ready!", System.currentTimeMillis() - 7200000, 5, R.drawable.ecoluxe_logo)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Messages", fontWeight = FontWeight.Bold, color = Color.White)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFB57EDC))
            )
        },
        containerColor = Color(0xFFF8F6FF)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(chats) { chat ->
                ChatItem(chat = chat) {
                    navController.navigate(ROUTE_CHAT + "/${chat.chatId}")
                }
            }
        }
    }
}

@Composable
fun ChatItem(chat: ChatPreview, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = chat.profilePic),
            contentDescription = null,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(chat.name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(chat.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    chat.lastMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
                if (chat.unreadCount > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Badge { Text(chat.unreadCount.toString()) }
                }
            }
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = Color.Gray,
            modifier = Modifier.size(20.dp)
        )
    }
}

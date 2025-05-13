package com.example.ecoluxe.data.model

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val profileName: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
data class ChatPreview(
    val chatId: String,
    val name: String,
    val lastMessage: String,
    val timestamp: Long,
    val unreadCount: Int = 0,
    val profilePicUrl: String = ""
)

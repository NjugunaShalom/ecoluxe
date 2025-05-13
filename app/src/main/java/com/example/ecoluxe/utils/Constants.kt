package com.example.ecoluxe.utils

fun generateChatId(userId1: String, userId2: String): String {
    val sortedIds = listOf(userId1, userId2).sorted()
    return "${sortedIds[0]}_${sortedIds[1]}"
}


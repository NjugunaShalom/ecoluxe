package com.example.ecoluxe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecoluxe.data.model.ChatMessage
import com.example.ecoluxe.data.model.ChatPreview
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class ChatViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _chatPreviews = MutableStateFlow<List<ChatPreview>>(emptyList())
    val chatPreviews = _chatPreviews.asStateFlow()

    private var chatListener: ListenerRegistration? = null

    fun loadMessages(chatId: String) {
        chatListener?.remove() // Detach previous listener

        // First, ensure the chat document exists
        db.collection("chats").document(chatId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (!documentSnapshot.exists()) {
                    // Create the chat document if it doesn't exist
                    db.collection("chats").document(chatId)
                        .set(mapOf("created" to System.currentTimeMillis()))
                        .addOnSuccessListener {
                            // Now set up the listener after ensuring document exists
                            setupChatListener(chatId)
                        }
                } else {
                    // Document exists, set up the listener
                    setupChatListener(chatId)
                }
            }
    }

    private fun setupChatListener(chatId: String) {
        chatListener = db.collection("chats").document(chatId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Handle error
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val msgList = snapshot.documents.mapNotNull { doc ->
                        val data = doc.data ?: return@mapNotNull null
                        ChatMessage(
                            id = doc.id,
                            senderId = data["senderId"] as? String ?: "",
                            profileName = data["profileName"] as? String ?: "",
                            text = data["text"] as? String ?: "",
                            timestamp = data["timestamp"] as? Long ?: 0L
                        )
                    }
                    _messages.value = msgList
                }
            }
    }

    fun sendMessage(chatId: String, text: String, onSuccess: () -> Unit, onError: () -> Unit) {
        val user = auth.currentUser ?: return
        val userId = user.uid


        viewModelScope.launch {
            try {
                // Get the current user's profile name
                val userDoc = db.collection("users").document(userId).get().await()
                val profileName = userDoc.getString("name") ?: "You"

                val message = mapOf(
                    "senderId" to userId,
                    "profileName" to profileName,
                    "text" to text,
                    "timestamp" to System.currentTimeMillis()
                )

                // Create the chat document if it doesn't exist
                val chatDocRef = db.collection("chats").document(chatId)
                val chatDoc = chatDocRef.get().await()

                if (!chatDoc.exists()) {
                    chatDocRef.set(mapOf("created" to System.currentTimeMillis())).await()
                }

                // Add the message
                db.collection("chats").document(chatId)
                    .collection("messages")
                    .add(message)
                    .addOnSuccessListener {
                        // Update last message info in the chat document
                        chatDocRef.update(
                            mapOf(
                                "lastMessage" to text,
                                "lastMessageTime" to System.currentTimeMillis()
                            )
                        )
                        onSuccess()
                    }
                    .addOnFailureListener { onError() }

            } catch (e: Exception) {
                e.printStackTrace()
                onError()
            }
        }
    }

    // Method to get the other user's name
    fun getOtherUserName(userId: String, onNameFetched: (String) -> Unit) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val name = document.getString("name") ?: "User"
                    onNameFetched(name)
                } else {
                    onNameFetched("User")
                }
            }
            .addOnFailureListener {
                onNameFetched("User")
            }
    }

    fun loadChatPreviews() {
        val currentUserId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                // Get all chats where current user is a participant
                val chatSnapshots = db.collection("chats")
                    .get()
                    .await()

                val previews = mutableListOf<ChatPreview>()

                for (chatDoc in chatSnapshots.documents) {
                    val chatId = chatDoc.id

                    // Only include chats where the current user is a participant
                    if (!chatId.contains(currentUserId)) continue

                    // Get last message
                    val messageSnapshot = db.collection("chats").document(chatId)
                        .collection("messages")
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .limit(1)
                        .get()
                        .await()
                        .documents
                        .firstOrNull()

                    val lastMessage = messageSnapshot?.getString("text") ?: ""
                    val timestamp = messageSnapshot?.getLong("timestamp") ?: 0L

                    // Get other user's ID
                    val otherUserId = chatId.split("_").firstOrNull { it != currentUserId } ?: continue

                    // Fetch other user's info
                    val userDoc = db.collection("users").document(otherUserId).get().await()
                    val name = userDoc.getString("name") ?: "User"
                    val imageUrl = userDoc.getString("imageUrl") ?: ""

                    previews.add(
                        ChatPreview(
                            chatId = chatId,
                            name = name,
                            lastMessage = lastMessage,
                            timestamp = timestamp,
                            unreadCount = 0,
                            profilePicUrl = imageUrl
                        )
                    )
                }

                _chatPreviews.value = previews.sortedByDescending { it.timestamp }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        chatListener?.remove()
    }
}
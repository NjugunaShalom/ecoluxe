package com.example.ecoluxe.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecoluxe.data.model.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EventViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events = _events.asStateFlow()

    init {
        loadEvents()
    }

    fun loadEvents() {
        db.collection("events")
            .orderBy("date")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val eventList = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    Event(
                        id = doc.id,
                        title = data["title"] as? String ?: "",
                        description = data["description"] as? String ?: "",
                        date = data["date"] as? String ?: "",
                        imageUrl = data["imageUrl"] as? String ?: "",
                        interested = (data["interestedCount"] as? Long)?.toInt() ?: 0
                    )
                }
                _events.value = eventList
            }
    }

    fun uploadEvent(title: String, desc: String, date: String, imageUrl: String, onSuccess: () -> Unit) {
        val event = mapOf(
            "title" to title,
            "description" to desc,
            "date" to date,
            "imageUrl" to imageUrl,
            "interestedCount" to 0
        )

        db.collection("events").add(event)
            .addOnSuccessListener { onSuccess() }
    }

    fun registerInterest(eventId: String, context: Context) {
        val user = auth.currentUser ?: return
        val name = user.displayName ?: ""
        val email = user.email ?: ""
        val uid = user.uid

        val data = mapOf(
            "uid" to uid,
            "name" to name,
            "email" to email
        )

        db.collection("events").document(eventId)
            .collection("interested")
            .add(data)
            .addOnSuccessListener {
                // Increment interestedCount
                db.collection("events").document(eventId)
                    .update("interestedCount", com.google.firebase.firestore.FieldValue.increment(1))
            }
            .addOnFailureListener {
                Toast.makeText(context, "Already registered or failed to register", Toast.LENGTH_SHORT).show()
            }
    }
    fun registerInterestManual(eventId: String, name: String, email: String, context: Context) {
        val uid = auth.currentUser?.uid ?: return
        val data = mapOf(
            "uid" to uid,
            "name" to name,
            "email" to email
        )

        db.collection("events").document(eventId)
            .collection("interested")
            .add(data)
            .addOnSuccessListener {
                db.collection("events").document(eventId)
                    .update("interestedCount", com.google.firebase.firestore.FieldValue.increment(1))
            }
            .addOnFailureListener {
                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
            }
    }

}



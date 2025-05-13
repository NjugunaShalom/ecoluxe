package com.example.ecoluxe.viewmodel

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EcoTrackerViewModel @Inject constructor() : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _points = MutableStateFlow(0)
    val points = _points.asStateFlow()

    private val _badges = MutableStateFlow<List<String>>(emptyList())
    val badges = _badges.asStateFlow()

    init {
        loadTracker()
    }

    fun logActivity(activity: String, earnedPoints: Int) {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            val newTotal = _points.value + earnedPoints

            // Save to Firestore
            val data = mapOf(
                "points" to newTotal,
                "lastActivity" to activity,
                "timestamp" to System.currentTimeMillis()
            )
            db.collection("ecoTracker").document(uid)
                .set(data)
                .addOnSuccessListener {
                    _points.value = newTotal
                    checkBadges(newTotal)
                }
                .addOnFailureListener {
                    // Optionally log
                }
        }
    }

    private fun checkBadges(totalPoints: Int) {
        val earned = mutableListOf<String>()
        if (totalPoints >= 50) earned.add("Eco Rookie")
        if (totalPoints >= 100) earned.add("Sustainable Star")
        if (totalPoints >= 200) earned.add("Eco Warrior")

        _badges.value = earned
    }

    fun loadTracker() {
        val uid = auth.currentUser?.uid ?: return

        db.collection("ecoTracker").document(uid).get()
            .addOnSuccessListener { doc ->
                val savedPoints = doc.getLong("points")?.toInt() ?: 0
                _points.value = savedPoints
                checkBadges(savedPoints)
            }
    }
}

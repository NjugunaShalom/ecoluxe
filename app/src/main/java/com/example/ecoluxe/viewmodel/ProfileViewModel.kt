package com.example.ecoluxe.viewmodel

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecoluxe.data.network.uploadImageToImgur
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor() : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    val name = mutableStateOf("")
    val bio = mutableStateOf("")
    val role = mutableStateOf("")
    val imageUrl = mutableStateOf("")

    fun loadProfile() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).get()
            .addOnSuccessListener { doc ->
                doc?.let {
                    name.value = it.getString("name") ?: ""
                    bio.value = it.getString("bio") ?: ""
                    role.value = it.getString("role") ?: ""
                    imageUrl.value = it.getString("imageUrl") ?: ""
                }
            }
    }

    fun updateProfile(newName: String, newBio: String, newRole: String, newImageUri: Uri?, context: Context, onSuccess: () -> Unit) {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            val uploadedUrl = newImageUri?.let { uploadImageToImgur(context, it) } ?: imageUrl.value

            val profileData = hashMapOf(
                "name" to newName,
                "bio" to newBio,
                "role" to newRole,
                "imageUrl" to uploadedUrl
            )

            db.collection("users")
                .document(userId)
                .set(profileData)
                .addOnSuccessListener {
                    name.value = newName
                    bio.value = newBio
                    role.value = newRole
                    imageUrl.value = uploadedUrl
                    Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
                    onSuccess()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error updating profile", Toast.LENGTH_SHORT).show()
                }
        }
    }
}

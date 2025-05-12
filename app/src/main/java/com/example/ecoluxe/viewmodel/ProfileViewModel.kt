package com.example.ecoluxe.viewmodel

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ecoluxe.data.model.UserProfile
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
                doc?.toObject(UserProfile::class.java)?.let { profile ->
                    name.value = profile.name
                    bio.value = profile.bio
                    role.value = profile.role
                    imageUrl.value = profile.imageUrl
                }
            }
            .addOnFailureListener {
                // Optional: log error
            }
    }

    fun updateProfile(
        newName: String,
        newBio: String,
        newRole: String,
        newImageUri: Uri?,
        context: Context,
        onSuccess: () -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            val uploadedUrl = if (newImageUri == null) {
                imageUrl.value
            } else {
                val result = uploadImageToImgur(context, newImageUri)
                if (result.isNullOrBlank()) {
                    Toast.makeText(context, "Image upload failed", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                result
            }


            val updatedProfile = UserProfile(
                name = newName,
                bio = newBio,
                role = newRole,
                imageUrl = uploadedUrl
            )

            db.collection("users").document(userId)
                .set(updatedProfile)
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

package com.example.ecoluxe.viewmodel

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ecoluxe.ui.screens.swap.SwapListScreen
import com.example.ecoluxe.data.model.SwapItem
import com.example.ecoluxe.data.network.uploadImageToImgur
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class SwapViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _swapItems = MutableStateFlow<List<SwapItem>>(emptyList())
    val swapItems = _swapItems.asStateFlow()

    fun loadItems(onlyMine: Boolean = false) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("swapItems")
            .whereEqualTo("status", "Available")
            .get()
            .addOnSuccessListener { snapshot ->
                val items = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    SwapItem(
                        id = doc.id,
                        name = data["name"] as? String ?: "",
                        size = data["size"] as? String ?: "",
                        color = data["color"] as? String ?: "",
                        imageUrl = data["imageUrl"] as? String ?: "",
                        extraImages = data["extraImages"] as? List<String> ?: emptyList(),
                        uploader = data["uploader"] as? String ?: "",
                        status = data["status"] as? String ?: "Available"
                    )
                }.filter { if (onlyMine) it.uploader == userId else true }

                _swapItems.value = items
            }
    }

    fun uploadItem(
        name: String,
        size: String,
        color: String,
        images: List<Uri>,
        context: Context,
        onSuccess: () -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return
        if (images.isEmpty()) {
            Toast.makeText(context, "Please select at least one image", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            val imageUrls = mutableListOf<String>()

            for (uri in images) {
                val imgurUrl = uploadImageToImgur(context, uri)
                if (!imgurUrl.isNullOrBlank()) {
                    imageUrls.add(imgurUrl)
                }
            }

            if (imageUrls.isEmpty()) {
                Toast.makeText(context, "Image upload failed", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val item = hashMapOf(
                "name" to name,
                "size" to size,
                "color" to color,
                "imageUrl" to imageUrls.first(),
                "extraImages" to imageUrls.drop(1),
                "uploader" to userId,
                "status" to "Available"
            )

            db.collection("swapItems").add(item)
                .addOnSuccessListener {
                    Toast.makeText(context, "Swap item uploaded!", Toast.LENGTH_SHORT).show()
                    loadItems() // 👈 Refresh UI
                    onSuccess()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to save item", Toast.LENGTH_SHORT).show()
                }
        }
    }

    fun editItem(
        itemId: String,
        name: String,
        size: String,
        color: String,
        images: List<Uri>,
        context: Context,
        onSuccess: () -> Unit
    ) {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            val imageUrls = mutableListOf<String>()

            for (uri in images) {
                val imgurUrl = uploadImageToImgur(context, uri)
                if (!imgurUrl.isNullOrBlank()) {
                    imageUrls.add(imgurUrl)
                }
            }

            if (imageUrls.isEmpty()) {
                Toast.makeText(context, "Image upload failed", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val update = mapOf(
                "name" to name,
                "size" to size,
                "color" to color,
                "imageUrl" to imageUrls.first(),
                "extraImages" to imageUrls.drop(1)
            )

            db.collection("swapItems").document(itemId)
                .update(update)
                .addOnSuccessListener {
                    Toast.makeText(context, "Swap item updated!", Toast.LENGTH_SHORT).show()
                    loadItems() // 👈 Refresh
                    onSuccess()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to update item", Toast.LENGTH_SHORT).show()
                }
        }
    }
    fun deleteItem(itemId: String) {
        db.collection("swapItems").document(itemId)
            .delete()
    }
    fun markAsSwapped(itemId: String) {
        db.collection("swapItems").document(itemId)
            .update("status", "Swapped")
    }



    fun isCurrentUser(uid: String): Boolean {
        return auth.currentUser?.uid == uid
    }

    fun loadItemsFiltered(color: String, size: String) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("swapItems")
            .whereEqualTo("status", "Available")
            .get()
            .addOnSuccessListener { snapshot ->
                var items = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    SwapItem(
                        id = doc.id,
                        name = data["name"] as? String ?: "",
                        size = data["size"] as? String ?: "",
                        color = data["color"] as? String ?: "",
                        imageUrl = data["imageUrl"] as? String ?: "",
                        extraImages = data["extraImages"] as? List<String> ?: emptyList(),
                        uploader = data["uploader"] as? String ?: "",
                        status = data["status"] as? String ?: "Available"
                    )
                }

                if (color != "All") items = items.filter { it.color.equals(color, ignoreCase = true) }
                if (size != "All") items = items.filter { it.size.equals(size, ignoreCase = true) }

                _swapItems.value = items
            }
    }


}

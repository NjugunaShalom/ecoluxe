package com.example.ecoluxe.ui.screens.swap

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.ecoluxe.R
import com.example.ecoluxe.data.network.uploadImageToImgur
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwapListScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: ""
    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()

    var swapItems by remember { mutableStateOf(listOf<SwapItem>()) }
    var selectedItem by remember { mutableStateOf<SwapItem?>(null) }
    var showUploadDialog by remember { mutableStateOf(false) }
    var showSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }

    var selectedFilter by remember { mutableStateOf("Most Recent") }
    var expandedFilter by remember { mutableStateOf(false) }

    // Fetch swap items
    LaunchedEffect(selectedFilter) {
        val baseQuery = db.collection("swap_items")
            .whereEqualTo("status", "Available")

        val query = when (selectedFilter) {
            "My Uploads" -> baseQuery.whereEqualTo("uploaderId", userId)
            "By Size" -> baseQuery.orderBy("size", Query.Direction.ASCENDING)
            "By Color" -> baseQuery.orderBy("color", Query.Direction.ASCENDING)
            else -> baseQuery.orderBy("timestamp", Query.Direction.DESCENDING)
        }

        query.addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                swapItems = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data
                    if (data != null) {
                        SwapItem(
                            id = doc.id,
                            imageUrl = data["imageUrl"] as? String ?: "",
                            name = data["itemName"] as? String ?: "",
                            size = data["size"] as? String ?: "",
                            color = data["color"] as? String ?: "",
                            uploader = data["uploaderId"] as? String ?: "",
                            extraImages = (data["extraImages"] as? List<String>).orEmpty(),
                            status = data["status"] as? String ?: "Available"
                        )
                    } else null
                }
            }
        }
    }

    if (showSnackbar) {
        LaunchedEffect(showSnackbar) {
            delay(2000)
            showSnackbar = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ecoluxe_logo),
                            contentDescription = "EcoLuxe Logo",
                            modifier = Modifier.size(32.dp).clip(CircleShape)
                        )
                        Text("EcoLuxe Swap", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { expandedFilter = true }) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = Color.White)
                        }
                        DropdownMenu(expanded = expandedFilter, onDismissRequest = { expandedFilter = false }) {
                            listOf("Most Recent", "By Size", "By Color", "My Uploads").forEach {
                                DropdownMenuItem(
                                    text = { Text(it) },
                                    onClick = {
                                        selectedFilter = it
                                        expandedFilter = false
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFB57EDC))
            )
        },
        floatingActionButton = {
            val pulse = rememberInfiniteTransition().animateFloat(
                initialValue = 1f,
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(tween(1000), repeatMode = RepeatMode.Reverse)
            )
            FloatingActionButton(
                onClick = { showUploadDialog = true },
                modifier = Modifier.scale(pulse.value),
                containerColor = Color(0xFFB57EDC)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item", tint = Color.White)
            }
        },
        snackbarHost = {
            if (showSnackbar) {
                Snackbar(modifier = Modifier.padding(16.dp)) {
                    Text(snackbarMessage)
                }
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFF8F6FF), Color(0xFFF4FFF4))
                    )
                )
                .padding(padding)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(swapItems) { item ->
                    SwapItemCard(
                        item = item,
                        onEdit = { /* TODO */ },
                        onDelete = {
                            db.collection("swap_items").document(item.id)
                                .delete()
                        },
                        onMarkSwapped = {
                            db.collection("swap_items").document(item.id)
                                .update("status", "Swapped")
                            snackbarMessage = "Marked as swapped"
                            showSnackbar = true
                        },
                        onTap = {
                            selectedItem = item
                        },
                        isMine = item.uploader == userId
                    )
                }
            }
        }

        if (showUploadDialog) {
            AlertDialog(
                onDismissRequest = { showUploadDialog = false },
                confirmButton = {},
                text = {
                    UploadSwapItemPopup { showUploadDialog = false }
                }
            )
        }

        if (selectedItem != null) {
            AlertDialog(
                onDismissRequest = { selectedItem = null },
                confirmButton = {},
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        AsyncImage(
                            model = selectedItem!!.imageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Name: ${selectedItem!!.name}")
                        Text("Size: ${selectedItem!!.size}")
                        Text("Color: ${selectedItem!!.color}")
                        Text("Uploader: ${selectedItem!!.uploader}")
                    }
                }
            )
        }
    }
}

@Composable
fun UploadSwapItemPopup(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val imageUri = remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
            uri: Uri? -> uri?.let { imageUri.value = it }
    }

    var itemName by remember { mutableStateOf("") }
    var itemDesc by remember { mutableStateOf("") }
    var itemSize by remember { mutableStateOf("") }
    var itemColor by remember { mutableStateOf("") }

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Upload Item", style = MaterialTheme.typography.titleMedium)

        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add Image",
            modifier = Modifier
                .size(100.dp)
                .clickable { launcher.launch("image/*") },
            tint = Color(0xFFB57EDC)
        )

        OutlinedTextField(
            value = itemName,
            onValueChange = { itemName = it },
            label = { Text("Item Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = itemDesc,
            onValueChange = { itemDesc = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = itemSize,
            onValueChange = { itemSize = it },
            label = { Text("Size (e.g. Size 10 UK)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = itemColor,
            onValueChange = { itemColor = it },
            label = { Text("Color") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (imageUri.value != null && itemName.isNotBlank()) {
                    scope.launch {
                        val imageUrl = uploadImageToImgur(context, imageUri.value!!)
                        if (imageUrl != null) {
                            val userId = auth.currentUser?.uid ?: "unknown"
                            val itemData = hashMapOf(
                                "itemName" to itemName,
                                "description" to itemDesc,
                                "imageUrl" to imageUrl,
                                "size" to itemSize,
                                "color" to itemColor,
                                "uploaderId" to userId,
                                "status" to "Available",
                                "timestamp" to com.google.firebase.Timestamp.now()
                            )

                            db.collection("swap_items")
                                .add(itemData)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Item uploaded!", Toast.LENGTH_SHORT).show()
                                    onDismiss()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "Failed to upload", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(context, "Image upload failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "Add image and name", Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB57EDC)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Upload", color = Color.White)
        }

        TextButton(onClick = onDismiss) {
            Text("Cancel")
        }
    }
}

@Composable
fun SwapItemCard(
    item: SwapItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMarkSwapped: () -> Unit,
    onTap: () -> Unit,
    isMine: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTap() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.name,
                    modifier = Modifier
                        .height(120.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isMine) {
                        Text(
                            text = "My Upload",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier
                                .background(Color(0xFFB57EDC), RoundedCornerShape(8.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Box {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color.White)
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                onClick = {
                                    expanded = false
                                    onEdit()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete") },
                                onClick = {
                                    expanded = false
                                    onDelete()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Mark as Swapped") },
                                onClick = {
                                    expanded = false
                                    onMarkSwapped()
                                }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(item.name, style = MaterialTheme.typography.titleSmall)
            Text(item.size, style = MaterialTheme.typography.bodySmall)
            Text(item.color, style = MaterialTheme.typography.bodySmall)
        }
    }
}

data class SwapItem(
    val id: String = "",
    val imageUrl: String = "",
    val name: String = "",
    val size: String = "",
    val color: String = "",
    val uploader: String = "",
    val extraImages: List<String> = emptyList(),
    val status: String = "Available"
)


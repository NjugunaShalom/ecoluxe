package com.example.ecoluxe.ui.screens.swap

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.ecoluxe.data.model.SwapItem
import com.example.ecoluxe.viewmodel.SwapViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwapListScreen(navController: NavController,viewModel: SwapViewModel = hiltViewModel()) {
    val items by viewModel.swapItems.collectAsState()
    val showUploadDialog = remember { mutableStateOf(false) }
    val showDetailPopup = remember { mutableStateOf(false) }
    val selectedItem = remember { mutableStateOf<SwapItem?>(null) }
    val context = LocalContext.current

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showUploadDialog.value = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
        containerColor = Color(0xFFF8F6FF)
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(items) { item ->
                    SwapItemCard(
                        item = item,
                        onEdit = {
                            // Future implementation
                        },
                        onDelete = { viewModel.deleteItem(item.id) },
                        onMarkSwapped = { viewModel.markAsSwapped(item.id) },
                        onChat = {
                            Toast.makeText(context, "Chat with ${item.uploaderName}", Toast.LENGTH_SHORT).show()
                        },
                        onTap = {
                            selectedItem.value = item
                            showDetailPopup.value = true
                        },
                        isMine = viewModel.isCurrentUser(item.uploader)
                    )
                }
            }

            if (showUploadDialog.value) {
                UploadSwapItemPopup(
                    onDismiss = { showUploadDialog.value = false },
                    onUploadSuccess = {
                        showUploadDialog.value = false
                        viewModel.loadItems()
                    },
                    viewModel = viewModel
                )
            }
            if (showDetailPopup.value && selectedItem.value != null) {
                SwapItemDetailPopup(
                    item = selectedItem.value!!,
                    onDismiss = { showDetailPopup.value = false },
                    onChat = {
                        Toast.makeText(context, "Chat with ${selectedItem.value!!.uploaderName}", Toast.LENGTH_SHORT).show()
                    }
                )
            }

        }
    }
}
@Composable
fun UploadSwapItemPopup(
    onDismiss: () -> Unit,
    onUploadSuccess: () -> Unit,
    viewModel: SwapViewModel
) {
    val context = LocalContext.current
    val purple = Color(0xFFB57EDC)
    val name = remember { mutableStateOf("") }
    val size = remember { mutableStateOf("") }
    val color = remember { mutableStateOf("") }
    val imageUris = remember { mutableStateListOf<Uri>() }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        imageUris.clear()
        imageUris.addAll(uris)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp),
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Add Swap Item", style = MaterialTheme.typography.titleMedium.copy(color = purple))

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(value = name.value, onValueChange = { name.value = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = size.value, onValueChange = { size.value = it }, label = { Text("Size") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = color.value, onValueChange = { color.value = it }, label = { Text("Color") }, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(12.dp))

                Button(onClick = { imagePicker.launch("image/*") }, colors = ButtonDefaults.buttonColors(containerColor = purple)) {
                    Text("Select Images", color = Color.White)
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(imageUris.size) { i ->
                        AsyncImage(
                            model = imageUris[i],
                            contentDescription = null,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        viewModel.uploadItem(
                            name = name.value,
                            size = size.value,
                            color = color.value,
                            images = imageUris,
                            context = context,
                            onSuccess = {
                                name.value = ""
                                size.value = ""
                                color.value = ""
                                imageUris.clear()
                                onUploadSuccess()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = purple),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Upload", color = Color.White)
                }

                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

@Composable
fun SwapItemDetailPopup(
    item: SwapItem,
    onDismiss: () -> Unit,
    onChat: () -> Unit
) {
    val purple = Color(0xFFB57EDC)
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp),
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (item.extraImages.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(item.extraImages.size) { i ->
                            AsyncImage(
                                model = item.extraImages[i],
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                Text("Name: ${item.name}", style = MaterialTheme.typography.titleSmall)
                Text("Size: ${item.size}", style = MaterialTheme.typography.bodySmall)
                Text("Color: ${item.color}", style = MaterialTheme.typography.bodySmall)
                if (item.uploaderName.isNotBlank()) {
                    Text("Uploaded by: ${item.uploaderName}", style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onChat,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = purple)
                ) {
                    Text("Chat with Uploader", color = Color.White)
                }

                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    )
}

@Composable
fun SwapItemCard(
    item: SwapItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMarkSwapped: () -> Unit,
    onChat: () -> Unit,
    onTap: () -> Unit,
    isMine: Boolean
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTap() },
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .height(140.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                ) {
                    if (isMine) {
                        Text(
                            text = "Mine",
                            color = Color.White,
                            modifier = Modifier
                                .background(Color(0xFFB57EDC), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.White)
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(text = { Text("Edit") }, onClick = {
                            menuExpanded = false
                            onEdit()
                        })
                        DropdownMenuItem(text = { Text("Delete") }, onClick = {
                            menuExpanded = false
                            onDelete()
                        })
                        DropdownMenuItem(text = { Text("Mark Swapped") }, onClick = {
                            menuExpanded = false
                            onMarkSwapped()
                        })
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = item.name, style = MaterialTheme.typography.titleSmall)
            Text(text = "Size: ${item.size}", style = MaterialTheme.typography.bodySmall)
            Text(text = "Color: ${item.color}", style = MaterialTheme.typography.bodySmall)
            if (item.uploaderName.isNotBlank()) {
                Text(text = "By: ${item.uploaderName}", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (!isMine) {
                OutlinedButton(
                    onClick = onChat,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Chat with Uploader")
                }
            }
        }
    }
}


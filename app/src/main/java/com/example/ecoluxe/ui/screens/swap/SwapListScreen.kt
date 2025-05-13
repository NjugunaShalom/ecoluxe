package com.example.ecoluxe.ui.screens.swap

import android.net.Uri
import android.widget.Toast
import com.example.ecoluxe.utils.generateChatId
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.ecoluxe.R
import com.example.ecoluxe.data.model.SwapItem
import com.example.ecoluxe.viewmodel.SwapViewModel
import com.google.firebase.auth.FirebaseAuth

// Define colors
private val LavenderPurple = Color(0xFFB57EDC)
private val White = Color(0xFFFFFFFF)
private val LightBackground = Color(0xFFF8F6FF)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwapListScreen(navController: NavController, viewModel: SwapViewModel = hiltViewModel()) {
    val items by viewModel.swapItems.collectAsState()
    val showUploadDialog = remember { mutableStateOf(false) }
    val showDetailPopup = remember { mutableStateOf(false) }
    val selectedItem = remember { mutableStateOf<SwapItem?>(null) }
    val context = LocalContext.current
    val selectedColor = remember { mutableStateOf("All") }
    val selectedSize = remember { mutableStateOf("All") }
    val menuExpanded = remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        viewModel.loadItems()
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
                        Text(
                            text = "Swap",
                            color = White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LavenderPurple
                ),
                actions = {

                    IconButton(onClick = { menuExpanded.value = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Filter", tint = Color.White)
                    }

                    DropdownMenu(
                        expanded = menuExpanded.value,
                        onDismissRequest = { menuExpanded.value = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Color: ${selectedColor.value}") },
                            onClick = {
                                selectedColor.value = if (selectedColor.value == "All") "Purple" else "All"
                                viewModel.loadItemsFiltered(selectedColor.value, selectedSize.value)
                                menuExpanded.value = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Size: ${selectedSize.value}") },
                            onClick = {
                                selectedSize.value = if (selectedSize.value == "All") "M" else "All"
                                viewModel.loadItemsFiltered(selectedColor.value, selectedSize.value)
                                menuExpanded.value = false
                            }
                        )
                    }

                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showUploadDialog.value = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
        containerColor = LightBackground
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
                            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                            val uploaderId = item.uploader

                            if (currentUserId == uploaderId) {
                                Toast.makeText(context, "You can't chat with yourself!", Toast.LENGTH_SHORT).show()
                                return@SwapItemCard
                            }

                            if (currentUserId != null) {
                                val chatId = generateChatId(currentUserId, uploaderId)
                                navController.navigate("chat/$chatId")
                            }
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
                    },
                    navController = navController
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
        containerColor = White,
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
                Text("Add Swap Item", style = MaterialTheme.typography.titleMedium.copy(color = LavenderPurple))

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(value = name.value, onValueChange = { name.value = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = size.value, onValueChange = { size.value = it }, label = { Text("Size") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = color.value, onValueChange = { color.value = it }, label = { Text("Color") }, modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(12.dp))

                Button(onClick = { imagePicker.launch("image/*") }, colors = ButtonDefaults.buttonColors(containerColor = LavenderPurple)) {
                    Text("Select Images", color = White)
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
                        println("Upload clicked") // ✅ Confirm click
                        Toast.makeText(context, "Uploading item...", Toast.LENGTH_SHORT).show()

                        if (name.value.isBlank() || size.value.isBlank() || color.value.isBlank() || imageUris.isEmpty()) {
                            Toast.makeText(context, "Please fill all fields & pick image", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        viewModel.uploadItem(
                            name = name.value,
                            size = size.value,
                            color = color.value,
                            images = imageUris,
                            context = context,
                            onSuccess = {
                                Toast.makeText(context, "Upload success!", Toast.LENGTH_SHORT).show()
                                name.value = ""
                                size.value = ""
                                color.value = ""
                                imageUris.clear()
                                onUploadSuccess()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LavenderPurple),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Upload", color = White)
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
    onChat: () -> Unit,
    navController: NavController
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        containerColor = White,
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
                    onClick = {
                        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                        val uploaderId = item.uploader

                        if (currentUserId == uploaderId) {
                            Toast.makeText(context, "You can't chat with yourself!", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (currentUserId != null) {
                            val chatId = generateChatId(currentUserId, uploaderId)
                            navController.navigate("chat/$chatId")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = LavenderPurple)
                ) {
                    Text("Chat with Uploader", color = White)
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
                            color = White,
                            modifier = Modifier
                                .background(LavenderPurple, RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = null, tint = White)
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
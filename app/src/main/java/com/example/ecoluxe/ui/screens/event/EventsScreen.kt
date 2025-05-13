package com.example.ecoluxe.ui.screens.event

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.ecoluxe.R
import com.example.ecoluxe.data.model.Event
import com.example.ecoluxe.data.network.uploadImageToImgur
import com.example.ecoluxe.viewmodel.EventViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(navController: NavController, viewModel: EventViewModel = hiltViewModel()) {
    val events by viewModel.events.collectAsState()
    val context = LocalContext.current
    val isAdmin = FirebaseAuth.getInstance().currentUser?.uid == "ByjOb5jrGearWTVvDWYFXrVoni23"

    val lavender = Color(0xFFB57EDC)
    val showUploadDialog = remember { mutableStateOf(false) }
    val showEditDialog = remember { mutableStateOf(false) }
    val eventToEdit = remember { mutableStateOf<Event?>(null) }

    val showInterestedPopup = remember { mutableStateOf(false) }
    val selectedEventId = remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ecoluxe_logo),
                            contentDescription = "Logo",
                            modifier = Modifier.size(32.dp).clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Events", color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = lavender)
            )
        },
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(onClick = { showUploadDialog.value = true }, containerColor = lavender) {
                    Icon(Icons.Default.Add, contentDescription = "Add Event")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Admin contact card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:ecoluxeevents@gmail.com")
                            putExtra(Intent.EXTRA_SUBJECT, "Event Hosting Inquiry")
                        }
                        context.startActivity(intent)
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1ECFB))
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Email, contentDescription = null, tint = lavender)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Want to host an event? Contact the admin")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            events.forEach { event ->
                EventCardWithMenu(
                    event = event,
                    onInterested = {
                        selectedEventId.value = event.id
                        showInterestedPopup.value = true
                    },
                    onEdit = {
                        eventToEdit.value = event
                        showEditDialog.value = true
                    },
                    onDelete = {
                        viewModel.deleteEvent(event.id) {}
                        Toast.makeText(context, "Event deleted", Toast.LENGTH_SHORT).show()
                    },
                    isAdmin = isAdmin
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (showUploadDialog.value) {
                UploadEventPopup(
                    onDismiss = { showUploadDialog.value = false },
                    onUpload = { title, desc, date, imageUrl ->
                        viewModel.uploadEvent(title, desc, date, imageUrl) {
                            showUploadDialog.value = false
                        }
                    }
                )
            }

            if (showEditDialog.value && eventToEdit.value != null) {
                EditEventPopup(
                    event = eventToEdit.value!!,
                    onDismiss = { showEditDialog.value = false },
                    onUpdate = { id, title, desc, date, imageUrl ->
                        viewModel.updateEvent(id, title, desc, date, imageUrl) {
                            showEditDialog.value = false
                        }
                    }
                )
            }

            if (showInterestedPopup.value && selectedEventId.value != null) {
                InterestedPopup(
                    onDismiss = { showInterestedPopup.value = false },
                    onSubmit = { name, email ->
                        viewModel.registerInterestManual(
                            eventId = selectedEventId.value!!,
                            name = name,
                            email = email,
                            context = context
                        )
                        showInterestedPopup.value = false
                    },
                    lavenderColor = lavender
                )
            }
        }
    }
}


@Composable
fun EventCardWithMenu(
    event: Event,
    onInterested: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    isAdmin: Boolean
) {
    val menuExpanded = remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = event.imageUrl,
                    contentDescription = event.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                )

                if (isAdmin) {
                    Box(modifier = Modifier.align(Alignment.TopEnd)) {
                        IconButton(onClick = { menuExpanded.value = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                        }
                        DropdownMenu(
                            expanded = menuExpanded.value,
                            onDismissRequest = { menuExpanded.value = false }
                        ) {
                            DropdownMenuItem(text = { Text("Edit") }, onClick = {
                                menuExpanded.value = false
                                onEdit()
                            })
                            DropdownMenuItem(text = { Text("Delete") }, onClick = {
                                menuExpanded.value = false
                                onDelete()
                            })
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(event.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(event.description, style = MaterialTheme.typography.bodyMedium)
            Text("Date: ${event.date}", style = MaterialTheme.typography.labelMedium)

            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = onInterested, modifier = Modifier.fillMaxWidth()) {
                Text("I'm Interested")
            }
        }
    }
}

@Composable
fun EditEventPopup(
    event: Event,
    onDismiss: () -> Unit,
    onUpdate: (String, String, String, String, String) -> Unit
) {
    val title = remember { mutableStateOf(event.title) }
    val desc = remember { mutableStateOf(event.description) }
    val date = remember { mutableStateOf(event.date) }
    val imageUrl = remember { mutableStateOf(event.imageUrl) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isUploading = remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            isUploading.value = true
            scope.launch {
                val url = uploadImageToImgur(context, uri)
                if (!url.isNullOrBlank()) imageUrl.value = url
                else Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show()
                isUploading.value = false
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Event", color = Color(0xFFB57EDC)) },
        text = {
            Column {
                OutlinedTextField(value = title.value, onValueChange = { title.value = it }, label = { Text("Title") })
                OutlinedTextField(value = desc.value, onValueChange = { desc.value = it }, label = { Text("Description") })
                OutlinedTextField(value = date.value, onValueChange = { date.value = it }, label = { Text("Date") })

                OutlinedTextField(
                    value = imageUrl.value,
                    onValueChange = { imageUrl.value = it },
                    label = { Text("Imgur Image URL (auto-filled)") },
                    readOnly = true,
                    trailingIcon = {
                        if (isUploading.value) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            IconButton(onClick = { launcher.launch("image/*") }) {
                                Icon(Icons.Default.Image, contentDescription = "Select Image")
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        onUpdate(event.id, title.value, desc.value, date.value, imageUrl.value)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB57EDC)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Update", color = Color.White)
                }

                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        },
        confirmButton = {}
    )
}
@Composable
fun UploadEventPopup(
    onDismiss: () -> Unit,
    onUpload: (String, String, String, String) -> Unit
) {
    val title = remember { mutableStateOf("") }
    val desc = remember { mutableStateOf("") }
    val date = remember { mutableStateOf("") }
    val imageUrl = remember { mutableStateOf("") }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isUploading = remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            isUploading.value = true
            scope.launch {
                val uploadedUrl = uploadImageToImgur(context, uri)
                if (!uploadedUrl.isNullOrBlank()) {
                    imageUrl.value = uploadedUrl
                } else {
                    Toast.makeText(context, "Image upload failed", Toast.LENGTH_SHORT).show()
                }
                isUploading.value = false
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = { Text("Add Event", color = Color(0xFFB57EDC)) },
        text = {
            Column {
                OutlinedTextField(value = title.value, onValueChange = { title.value = it }, label = { Text("Title") })
                OutlinedTextField(value = desc.value, onValueChange = { desc.value = it }, label = { Text("Description") })
                OutlinedTextField(value = date.value, onValueChange = { date.value = it }, label = { Text("Date") })

                OutlinedTextField(
                    value = imageUrl.value,
                    onValueChange = { imageUrl.value = it },
                    label = { Text("Imgur Image URL (auto-filled)") },
                    readOnly = true,
                    trailingIcon = {
                        if (isUploading.value) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            IconButton(onClick = { launcher.launch("image/*") }) {
                                Icon(Icons.Default.Image, contentDescription = "Select Image")
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (imageUrl.value.isBlank()) {
                            Toast.makeText(context, "Please upload an image first", Toast.LENGTH_SHORT).show()
                        } else {
                            onUpload(title.value, desc.value, date.value, imageUrl.value)
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
    )
}
@Composable
fun InterestedPopup(
    onDismiss: () -> Unit,
    onSubmit: (String, String) -> Unit,
    lavenderColor: Color
) {
    val name = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = {
            Text(
                "Register Interest",
                color = lavenderColor,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name.value,
                    onValueChange = { name.value = it },
                    label = { Text("Your Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = email.value,
                    onValueChange = { email.value = it },
                    label = { Text("Your Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button( onClick = {
                    if (name.value.isBlank() || email.value.isBlank()) {
                        Toast.makeText(context, "Please enter both name and email", Toast.LENGTH_SHORT).show()
                    } else {
                        onSubmit(name.value.trim(), email.value.trim())
                        Toast.makeText(context, "You've been registered!", Toast.LENGTH_SHORT).show()
                    }
                },

                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = lavenderColor)
                ) {
                    Text("I'm Coming", color = Color.White)
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Cancel", color = lavenderColor)
                }
            }
        }
    )
}

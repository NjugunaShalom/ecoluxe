package com.example.ecoluxe.ui.screens.event

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.SwapHoriz
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
import com.example.ecoluxe.viewmodel.EventViewModel
import com.example.ecoluxe.data.model.Event
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(navController: NavController,viewModel: EventViewModel = hiltViewModel()) {
    val events by viewModel.events.collectAsState()
    val showUploadDialog = remember { mutableStateOf(false) }
    val showRegistrationPopup = remember { mutableStateOf(false) }
    val selectedEventId = remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val lavender = Color(0xFFB57EDC)  // Primary lavender color
    val lavenderDark = Color(0xFF9152C0)  // Darker shade for some elements
    val white = Color.White

    val isAdmin = FirebaseAuth.getInstance().currentUser?.uid == "ByjOb5jrGearWTVvDWYFXrVoni23"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ecoluxe_logo),
                            contentDescription = "EcoLuxe Logo",
                            modifier = Modifier.size(32.dp).clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Events",
                            color = white,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = lavender,
                    titleContentColor = white,
                    actionIconContentColor = white
                ),
                modifier = Modifier.statusBarsPadding()
            )
        },
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(
                    onClick = { showUploadDialog.value = true },
                    containerColor = lavender,
                    contentColor = white
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Event")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Contact Admin Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF1ECFB))
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Email, contentDescription = null, tint = lavender)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Want to host an event? Contact the admin",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display event cards
            events.forEach { event ->
                EventCard(
                    event = event,
                    lavenderColor = lavender,
                    onInterested = {
                        viewModel.registerInterest(eventId = event.id, context)
                        selectedEventId.value = event.id
                        showRegistrationPopup.value = true
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Admin Upload Popup
            if (showUploadDialog.value) {
                UploadEventPopup(
                    onDismiss = { showUploadDialog.value = false },
                    onUpload = { title, desc, date, imageUrl ->
                        viewModel.uploadEvent(title, desc, date, imageUrl) {
                            showUploadDialog.value = false
                        }
                    },
                    lavenderColor = lavender
                )
            }
            if (showRegistrationPopup.value && selectedEventId.value != null) {
                InterestedPopup(
                    onDismiss = { showRegistrationPopup.value = false },
                    onSubmit = { name, email ->
                        viewModel.registerInterestManual(
                            eventId = selectedEventId.value!!,
                            name = name,
                            email = email,
                            context = context
                        )
                        showRegistrationPopup.value = false
                    },
                    lavenderColor = lavender
                )
            }
            // Add some bottom padding for better scrolling experience
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun EventCard(
    event: Event,
    lavenderColor: Color,
    onInterested: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            AsyncImage(
                model = event.imageUrl,
                contentDescription = event.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(180.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                event.title,
                style = MaterialTheme.typography.titleMedium,
                color = lavenderColor,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                event.description,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Date: ${event.date}",
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onInterested,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = lavenderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("I'm Interested", color = Color.White)
            }
        }
    }
}

@Composable
fun UploadEventPopup(
    onDismiss: () -> Unit,
    onUpload: (String, String, String, String) -> Unit,
    lavenderColor: Color
) {
    val title = remember { mutableStateOf("") }
    val desc = remember { mutableStateOf("") }
    val date = remember { mutableStateOf("") }
    val imageUrl = remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = { Text("Add Event", color = lavenderColor, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = title.value,
                    onValueChange = { title.value = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = desc.value,
                    onValueChange = { desc.value = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = date.value,
                    onValueChange = { date.value = it },
                    label = { Text("Date") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = imageUrl.value,
                    onValueChange = { imageUrl.value = it },
                    label = { Text("Imgur Image URL") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        onUpload(title.value, desc.value, date.value, imageUrl.value)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = lavenderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Upload", color = Color.White)
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

@Composable
fun InterestedPopup(
    onDismiss: () -> Unit,
    onSubmit: (String, String) -> Unit,
    lavenderColor: Color
) {
    val name = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register Interest", color = lavenderColor, fontWeight = FontWeight.Bold) },
        confirmButton = {},
        text = {
            Column(
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
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

                Button(
                    onClick = {
                        onSubmit(name.value, email.value)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = lavenderColor),
                    modifier = Modifier.fillMaxWidth()
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
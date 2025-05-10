package com.example.ecoluxe.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.ecoluxe.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onNavigateToChat: () -> Unit = {}
) {
    val showEditPopup = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val profileImageScale = remember { Animatable(0.8f) }
    var isContentVisible by remember { mutableStateOf(false) }

    val lavender = Color(0xFFD8CCF1)
    val sage = Color(0xFFCADFBB)

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
        profileImageScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        isContentVisible = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Profile", color = Color.White, fontWeight = FontWeight.Bold)
                },
                actions = {
                    IconButton(onClick = onNavigateToChat) {
                        Icon(Icons.Default.Chat, contentDescription = "Chat", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFB57EDC))
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(lavender, sage),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(48.dp))

                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .shadow(10.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    )

                    AsyncImage(
                        model = viewModel.imageUrl.value,
                        contentDescription = "Profile Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(100.dp)
                            .scale(profileImageScale.value)
                            .clip(CircleShape)
                            .border(3.dp, Color.White, CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                AnimatedVisibility(
                    visible = isContentVisible,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 3 }),
                    exit = fadeOut()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = viewModel.name.value,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                shadow = Shadow(
                                    color = Color.White.copy(alpha = 0.3f),
                                    offset = Offset(1f, 1f),
                                    blurRadius = 2f
                                )
                            )
                        )

                        Card(
                            modifier = Modifier.padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFB57EDC).copy(alpha = 0.6f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = viewModel.role.value,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = Color.White,
                                style = MaterialTheme.typography.labelLarge
                            )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = viewModel.bio.value,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }

                val pulseAnim = rememberInfiniteTransition().animateFloat(
                    initialValue = 0.95f,
                    targetValue = 1.05f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000),
                        repeatMode = RepeatMode.Reverse
                    )
                )

                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            val clickAnim = Animatable(1f)
                            clickAnim.animateTo(0.8f, animationSpec = tween(100))
                            clickAnim.animateTo(1f, animationSpec = tween(100))
                            showEditPopup.value = true
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .scale(pulseAnim.value),
                    containerColor = Color(0xFFB57EDC),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }

                if (showEditPopup.value) {
                    EditProfilePopup(viewModel = viewModel, onDismiss = { showEditPopup.value = false })
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun EditProfilePopup(viewModel: ProfileViewModel, onDismiss: () -> Unit) {
        val context = LocalContext.current

        var name by remember { mutableStateOf(viewModel.name.value) }
        var bio by remember { mutableStateOf(viewModel.bio.value) }
        var role by remember { mutableStateOf(viewModel.role.value) }
        var imageUri by remember { mutableStateOf<Uri?>(null) }
        var expanded by remember { mutableStateOf(false) }

        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
                uri -> uri?.let { imageUri = it }
        }

        val roles = listOf("Eco Enthusiast", "Tailor", "Stylist", "Event Curator")

        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {},
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Edit Profile", style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB57EDC)
                    ))

                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color(0xFFB57EDC), CircleShape)
                            .clickable { launcher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = imageUri ?: viewModel.imageUrl.value,
                            contentDescription = "Profile Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFB57EDC),
                            focusedLabelColor = Color(0xFFB57EDC)
                        )
                    )

                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("Bio") },
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFB57EDC),
                            focusedLabelColor = Color(0xFFB57EDC)
                        )
                    )

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = role,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Role") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            modifier = Modifier.menuAnchor(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFB57EDC),
                                focusedLabelColor = Color(0xFFB57EDC)
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            roles.forEach {
                                DropdownMenuItem(
                                    text = { Text(it) },
                                    onClick = {
                                        role = it
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.updateProfile(name, bio, role, imageUri, context) {
                                onDismiss()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB57EDC)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Update", color = Color.White)
                    }

                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF83A69B))
                    ) {
                        Text("Cancel")
                    }
                }
            }
        )
    }

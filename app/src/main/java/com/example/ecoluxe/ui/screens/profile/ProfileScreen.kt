package com.example.ecoluxe.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.ecoluxe.R
import com.example.ecoluxe.navigation.ROUTE_CHATLIST
import com.example.ecoluxe.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val showEditPopup = remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val profileImageScale = remember { Animatable(0.8f) }
    var isContentVisible by remember { mutableStateOf(false) }

    // Enhanced gradient colors
    val lavender = Color(0xFFD8CCF1)
    val sage = Color(0xFFCADFBB)
    val purple = Color(0xFFB57EDC)
    val lightPurple = Color(0xFFCFB3E8)

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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .shadow(4.dp, CircleShape)
                                .clip(CircleShape)
                                .background(Color.White)
                                .padding(4.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ecoluxe_logo),
                                contentDescription = "EcoLuxe Logo",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Text(
                            "Profile",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { navController.navigate(ROUTE_CHATLIST) },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            Icons.Default.Chat,
                            contentDescription = "Chat",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = purple,
                    titleContentColor = Color.White
                ),
                modifier = Modifier.shadow(8.dp)
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
            // Decorative background elements
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .offset((-100).dp, (-100).dp)
                    .blur(40.dp)
                    .background(
                        color = lightPurple.copy(alpha = 0.4f),
                        shape = CircleShape
                    )
            )

            Box(
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.BottomEnd)
                    .offset(100.dp, 50.dp)
                    .blur(30.dp)
                    .background(
                        color = sage.copy(alpha = 0.4f),
                        shape = CircleShape
                    )
            )

            // Main content with scrolling
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Use a scrollable column for content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(36.dp))

                    // Profile image with enhanced shadow effect
                    Box(contentAlignment = Alignment.Center) {
                        // Outer glow
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .shadow(16.dp, CircleShape)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.5f),
                                            Color.White.copy(alpha = 0.1f)
                                        )
                                    )
                                )
                        )

                        // Inner shadow ring
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .shadow(8.dp, CircleShape)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.3f))
                        )

                        // Actual profile image
                        AsyncImage(
                            model = viewModel.imageUrl.value,
                            contentDescription = "Profile Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(110.dp)
                                .scale(profileImageScale.value)
                                .clip(CircleShape)
                                .border(4.dp, Color.White, CircleShape)
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
                                    fontSize = 28.sp,
                                    shadow = Shadow(
                                        color = Color.White.copy(alpha = 0.5f),
                                        offset = Offset(1f, 1f),
                                        blurRadius = 3f
                                    )
                                ),
                                color = Color(0xFF3D3D3D)
                            )

                            Card(
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                                    .shadow(8.dp, RoundedCornerShape(18.dp)),
                                colors = CardDefaults.cardColors(
                                    containerColor = purple.copy(alpha = 0.7f)
                                ),
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = viewModel.role.value,
                                        modifier = Modifier
                                            .padding(horizontal = 16.dp, vertical = 6.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.White.copy(alpha = 0.2f))
                                            .padding(horizontal = 12.dp, vertical = 4.dp),
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Medium
                                        )
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = viewModel.bio.value,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            lineHeight = 24.sp
                                        ),
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )
                                }
                            }

                            // Add additional space to ensure quick actions appear below profile section
                            Spacer(modifier = Modifier.height(48.dp))

                            // Quick Actions section with animations
                            val cardScale = remember { Animatable(0.9f) }
                            val buttonBounce = remember { Animatable(0f) }

                            LaunchedEffect(isContentVisible) {
                                if (isContentVisible) {
                                    // Animate card appearing
                                    cardScale.animateTo(
                                        targetValue = 1f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        )
                                    )
                                    // Animate button bounce
                                    buttonBounce.animateTo(
                                        targetValue = 1f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        )
                                    )
                                }
                            }

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                                    .scale(cardScale.value)
                                    .shadow(12.dp, RoundedCornerShape(30.dp)),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White.copy(alpha = 0.9f)
                                ),
                                shape = RoundedCornerShape(30.dp),
                                elevation = CardDefaults.cardElevation(6.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Title with decorative element
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(bottom = 24.dp)
                                    ) {
                                        Divider(
                                            color = purple.copy(alpha = 0.5f),
                                            modifier = Modifier
                                                .width(30.dp)
                                                .height(2.dp)
                                        )

                                        Text(
                                            "Quick Actions",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF444444),
                                                fontSize = 18.sp
                                            ),
                                            modifier = Modifier.padding(horizontal = 12.dp)
                                        )

                                        Divider(
                                            color = purple.copy(alpha = 0.5f),
                                            modifier = Modifier
                                                .width(30.dp)
                                                .height(2.dp)
                                        )
                                    }

                                    // Action buttons with animation
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        // My Items Button with enhanced design
                                        Button(
                                            onClick = {
                                                coroutineScope.launch {
                                                    // Add click animation
                                                    val clickAnim = Animatable(1f)
                                                    clickAnim.animateTo(0.9f, animationSpec = tween(100))
                                                    clickAnim.animateTo(1f, animationSpec = spring())
                                                    navController.navigate("swaplist?myitems=true")
                                                }
                                            },
                                            shape = RoundedCornerShape(16.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = purple
                                            ),
                                            modifier = Modifier
                                                .height(54.dp)
                                                .scale(buttonBounce.value)
                                                .shadow(8.dp, RoundedCornerShape(16.dp)),
                                            elevation = ButtonDefaults.buttonElevation(
                                                defaultElevation = 4.dp,
                                                pressedElevation = 2.dp
                                            )
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White.copy(alpha = 0.2f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    Icons.Default.ShoppingBag,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp),
                                                    tint = Color.White
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                "My Items",
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    fontWeight = FontWeight.Medium,
                                                    letterSpacing = 0.5.sp
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            // Add bottom padding for scrollable content
                            Spacer(modifier = Modifier.height(40.dp))
                        }
                    }

                    if (showEditPopup.value) {
                        EditProfilePopup(
                            name = viewModel.name.value,
                            bio = viewModel.bio.value,
                            role = viewModel.role.value,
                            imageUrl = viewModel.imageUrl.value,
                            onSave = { name, bio, role, uri ->
                                viewModel.updateProfile(name, bio, role, uri, context) {
                                    showEditPopup.value = false
                                }
                            },
                            onDismiss = { showEditPopup.value = false }
                        )
                    }
                }

                // Enhanced floating action button animation
                val pulseAnim = rememberInfiniteTransition().animateFloat(
                    initialValue = 0.95f,
                    targetValue = 1.05f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000),
                        repeatMode = RepeatMode.Reverse
                    )
                )

            }
            // Place this directly inside the outer Box, AFTER the Column
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
                    .align(Alignment.BottomEnd) // ⬅ This works because it's inside Box
                    .padding(20.dp)
                    .scale(pulseAnim.value),
                containerColor = Color(0xFFB57EDC),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(28.dp))
            }

        }
    }
}
    @Composable
    fun EditProfilePopup(
        name: String,
        bio: String,
        role: String,
        imageUrl: String,
        onSave: (String, String, String, Uri?) -> Unit,
        onDismiss: () -> Unit
    ) {
        var newName by remember { mutableStateOf(name) }
        var newBio by remember { mutableStateOf(bio) }
        var newRole by remember { mutableStateOf(role) }
        var imageUri by remember { mutableStateOf<Uri?>(null) }
        val purple = Color(0xFFB57EDC)

        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
            it?.let { uri -> imageUri = uri }
        }

        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {},
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp),
            text = {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Edit Profile",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = purple
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color(0xFFF5F5F5))
                            .clickable { launcher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = imageUri ?: imageUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .border(3.dp, purple.copy(alpha = 0.3f), CircleShape)
                        )

                        // Edit overlay hint
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.2f))
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Change Photo",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = purple,
                            focusedLabelColor = purple
                        )
                    )

                    OutlinedTextField(
                        value = newBio,
                        onValueChange = { newBio = it },
                        label = { Text("Bio") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = purple,
                            focusedLabelColor = purple
                        )
                    )

                    OutlinedTextField(
                        value = newRole,
                        onValueChange = { newRole = it },
                        label = { Text("Role") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = purple,
                            focusedLabelColor = purple
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { onSave(newName, newBio, newRole, imageUri) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = purple),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(6.dp)
                    ) {
                        Text(
                            "Update",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color.Gray
                        )
                    ) {
                        Text("Cancel")
                    }
                }
            }
        )
    }


//@Preview(showBackground = true, showSystemUi = true)
//@Composable
//fun ProfileScreenPreview() {
//    rememberNavController(fun ProfileScreen)
//
//   }
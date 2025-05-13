package com.example.ecoluxe.ui.screens.home

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.ecoluxe.R
import com.example.ecoluxe.navigation.ROUTE_ECOTRACKER
import com.example.ecoluxe.navigation.ROUTE_EVENTS
import com.example.ecoluxe.navigation.ROUTE_PROFILE
import com.example.ecoluxe.navigation.ROUTE_SWAPLIST

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val primaryColor = Color(0xFFB57EDC)

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
                            text = "EcoLuxe",
                            style = TextStyle(
                                fontSize = 22.sp,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:ecoluxe@gmail.com")
                            putExtra(Intent.EXTRA_SUBJECT, "Customer Care")
                        }
                        context.startActivity(intent)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "Support",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = primaryColor),
                modifier = Modifier.shadow(elevation = 4.dp)
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFF8F6FF), Color(0xFFF4FFF4))
                    )
                )
                .padding(padding)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                AnimatedFeatureCard(
                    imageId = R.drawable.swap_card,
                    title = "Swap",
                    description = "Exchange fashion items sustainably",
                    onClick = { navController.navigate(ROUTE_SWAPLIST) },
                    delayMillis = 0
                )
                AnimatedFeatureCard(
                    imageId = R.drawable.events_card,
                    title = "Events",
                    description = "Attend eco fashion expos",
                    onClick = { navController.navigate(ROUTE_EVENTS) },
                    delayMillis = 200
                )
                AnimatedFeatureCard(
                    imageId = R.drawable.ecotracker_card,
                    title = "EcoTracker",
                    description = "Track your sustainable impact",
                    onClick = { navController.navigate(ROUTE_ECOTRACKER) },
                    delayMillis = 400
                )
                AnimatedFeatureCard(
                    imageId = R.drawable.profile_card,
                    title = "Profile",
                    description = "Set up your bio and portfolio",
                    onClick = { navController.navigate(ROUTE_PROFILE) },
                    delayMillis = 600
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun AnimatedFeatureCard(
    imageId: Int,
    title: String,
    description: String,
    onClick: () -> Unit,
    delayMillis: Int = 0
) {
    val offsetY = remember { Animatable(100f) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delayMillis.toLong())
        offsetY.animateTo(
            targetValue = 0f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
        )
    }

    Box(modifier = Modifier.offset(y = offsetY.value.dp)) {
        FeatureCard(imageId, title, description, onClick)
    }
}

@Composable
fun FeatureCard(
    imageId: Int,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clickable { onClick() }
            .padding(6.dp)
            .shadow(15.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = imageId),
                contentDescription = title,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(110.dp)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp))
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB57EDC)
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = TextStyle(fontSize = 13.sp, color = Color.Gray)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = Color(0xFFB57EDC),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Explore",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = Color(0xFFB57EDC),
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}

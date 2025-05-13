package com.example.ecoluxe.ui.screens.ecotracker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.ecoluxe.viewmodel.EcoTrackerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EcoTrackerScreen(
    navController: NavController,
    viewModel: EcoTrackerViewModel = hiltViewModel()
) {
    // Sage and lavender color palette
    val sage = Color(0xFFBFCBA8)
    val lavender = Color(0xFFB57EDC)
    val lightSage = Color(0xFFDCE8D0)
    val lightLavender = Color(0xFFE6D7F2)
    val background = Color(0xFFF9F8FC)

    val ecoPoints by viewModel.points.collectAsState()
    val unlockedBadges by viewModel.badges.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "EcoTracker",
                        color = lavender,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = background
                )
            )
        },
        containerColor = background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Points card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Eco Points",
                        style = MaterialTheme.typography.titleMedium,
                        color = sage
                    )
                    Text(
                        "$ecoPoints",
                        style = MaterialTheme.typography.headlineLarge,
                        color = lavender,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        "Next badge in: ${100 - (ecoPoints % 100)} points",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    LinearProgressIndicator(
                        progress = (ecoPoints % 100) / 100f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = lavender,
                        trackColor = lightSage
                    )
                }
            }

            // Activities section
            Text(
                "Log Activities",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActivityButton(
                        title = "Attended Swap",
                        points = 20,
                        gradientStart = sage,
                        gradientEnd = lightSage,
                        onClick = { viewModel.logActivity("Attended Swap", 20) }
                    )

                    ActivityButton(
                        title = "30 Day No-Buy Challenge",
                        points = 50,
                        gradientStart = lavender,
                        gradientEnd = lightLavender,
                        onClick = { viewModel.logActivity("30 Day No-Buy Challenge", 50) }
                    )
                }
            }

            // Badges section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Unlocked Badges",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )

                    if (unlockedBadges.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Complete activities to earn badges!",
                                color = Color.Gray,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            unlockedBadges.forEach { badge ->
                                BadgeItem(badge = badge)
                            }
                        }
                    }
                }
            }

            // Add space at the bottom for better scrolling experience
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ActivityButton(
    title: String,
    points: Int,
    gradientStart: Color,
    gradientEnd: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 16.dp, horizontal = 16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(gradientStart, gradientEnd)
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "+$points pts",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun BadgeItem(badge: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "🏆",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = badge,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.DarkGray
        )
    }
}
package com.example.ecoluxe.ui.screens.auth

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.ecoluxe.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigateToNext: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    // Animation configurations
    val alphaAnim by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 1000)
    )

    val scaleAnim by animateFloatAsState(
        targetValue = if (visible) 1.2f else 0.8f,
        animationSpec = tween(
            durationMillis = 1000,
            easing = FastOutSlowInEasing
        )
    )

    // Handle animation timing and navigation
    LaunchedEffect(Unit) {
        visible = true
        delay(3000)
        onNavigateToNext()
    }

    // Define theme colors for gradient
    val lavender = Color(0xFFE6E6FA)
    val sage = Color(0xFFBCBFA3)
    val gradientBrush = Brush.linearGradient(
        colors = listOf(lavender, sage)
    )

    // Main content
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ecoluxe_logo),
            contentDescription = "EcoLuxe Logo",
            modifier = Modifier
                .size(350.dp)
                .scale(scaleAnim)
                .alpha(alphaAnim)
        )
    }
}
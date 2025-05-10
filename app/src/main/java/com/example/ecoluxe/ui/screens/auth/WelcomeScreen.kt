package com.example.ecoluxe.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.compose.foundation.shape.CircleShape
import com.example.ecoluxe.R
import com.example.ecoluxe.navigation.ROUTE_LOGIN
import com.example.ecoluxe.navigation.ROUTE_SIGNUP

@Composable
fun WelcomeScreen(navController: NavHostController) {
    val lavender = Color(0xFFE6E6FA)
    val sage = Color(0xFFBCBFA3)
    val gradientBrush = Brush.linearGradient(
        colors = listOf(lavender, sage)
    )

    // Button colors
    val primaryButtonColor = Color(0xFF5D7052)
    val secondaryButtonColor = Color(0xFFB57EDC)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.ecoluxe_logo),
                contentDescription = "EcoLuxe Logo",
                modifier = Modifier.size(300.dp).clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(50.dp))

            // Login Button
            Button(
                onClick = { navController.navigate(ROUTE_LOGIN) },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(56.dp)
                    .clip(RoundedCornerShape(28.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryButtonColor
                )
            ) {
                Text(
                    text = "Login",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sign Up Button
            Button(
                onClick = { navController.navigate(ROUTE_SIGNUP) },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(56.dp)
                    .clip(RoundedCornerShape(28.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = secondaryButtonColor
                )
            ) {
                Text(
                    text = "Sign Up",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
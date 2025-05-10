package com.example.ecoluxe.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.ecoluxe.R
import com.example.ecoluxe.navigation.ROUTE_PROFILE
import com.example.ecoluxe.viewmodel.AuthViewModel

@Composable
fun SignupScreen(navController: NavController, viewModel: AuthViewModel = hiltViewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFF3E5F5), Color(0xFFE0F2E9)) // Lavender → Sage
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .blur(50.dp)
                        .background(Color(0xFF88B04B), shape = CircleShape)
                )
                Image(
                    painter = painterResource(id = R.drawable.ecoluxe_logo),
                    contentDescription = "EcoLuxe Logo",
                    modifier = Modifier.size(120.dp)
                )
            }

            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color(0xFF88B04B)
                )
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (password != confirmPassword) {
                        Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.signup(email.trim(), password.trim()) { success ->
                            if (success) {
                                navController.navigate(ROUTE_PROFILE)
                            } else {
                                Toast.makeText(context, "Sign up failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB57EDC)
                )
            ) {
                Text("Sign Up", color = Color.White)
            }

            TextButton(onClick = { navController.navigate("login") }) {
                Text("Already have an account? Log in", color = Color.Gray)
            }
        }
    }
}

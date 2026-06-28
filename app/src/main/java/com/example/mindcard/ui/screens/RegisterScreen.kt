package com.example.mindcard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.example.mindcard.Login
import com.example.mindcard.Main
import com.example.mindcard.ui.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    onNavigate: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel()
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE8E7FF),
                        Color(0xFFE8F8EC)
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(32.dp),
                    clip = false
                )
                .background(Color.White.copy(alpha = 0.95f), RoundedCornerShape(32.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Circular Logo Icon
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(PrimaryIndigo, DarkIndigo)
                        ),
                        CircleShape
                    )
                    .shadow(4.dp, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🚀", fontSize = 36.sp)
            }

            Text(
                text = "Create Account",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E2022)
            )

            Text(
                text = "Join us to level up your learning!",
                fontSize = 15.sp,
                color = OutlineColor
            )

            viewModel.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = Color.Red,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 4.dp),
                    textAlign = TextAlign.Center
                )
            }

            // Name Field Group
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Full Name",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E2022),
                    modifier = Modifier.padding(start = 4.dp)
                )
                OutlinedTextField(
                    value = viewModel.name,
                    onValueChange = { viewModel.name = it },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = OutlineColor) },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. John Doe", color = OutlineColor.copy(alpha = 0.5f)) },
                    enabled = !viewModel.isLoading,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFEFF1F8),
                        unfocusedContainerColor = Color(0xFFEFF1F8),
                        disabledContainerColor = Color(0xFFEFF1F8).copy(alpha = 0.6f),
                        focusedBorderColor = PrimaryIndigo,
                        unfocusedBorderColor = Color.Transparent,
                        disabledBorderColor = Color.Transparent
                    )
                )
            }

            // Email Field Group
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Email Address",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E2022),
                    modifier = Modifier.padding(start = 4.dp)
                )
                OutlinedTextField(
                    value = viewModel.email,
                    onValueChange = { viewModel.email = it },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = OutlineColor) },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("learner@mindcard.com", color = OutlineColor.copy(alpha = 0.5f)) },
                    enabled = !viewModel.isLoading,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFEFF1F8),
                        unfocusedContainerColor = Color(0xFFEFF1F8),
                        disabledContainerColor = Color(0xFFEFF1F8).copy(alpha = 0.6f),
                        focusedBorderColor = PrimaryIndigo,
                        unfocusedBorderColor = Color.Transparent,
                        disabledBorderColor = Color.Transparent
                    )
                )
            }

            // Password Field Group
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Password",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E2022),
                    modifier = Modifier.padding(start = 4.dp)
                )
                OutlinedTextField(
                    value = viewModel.password,
                    onValueChange = { viewModel.password = it },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = OutlineColor) },
                    shape = RoundedCornerShape(20.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("••••••••", color = OutlineColor.copy(alpha = 0.5f)) },
                    enabled = !viewModel.isLoading,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFEFF1F8),
                        unfocusedContainerColor = Color(0xFFEFF1F8),
                        disabledContainerColor = Color(0xFFEFF1F8).copy(alpha = 0.6f),
                        focusedBorderColor = PrimaryIndigo,
                        unfocusedBorderColor = Color.Transparent,
                        disabledBorderColor = Color.Transparent
                    )
                )
            }

            // Confirm Password Field Group
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Confirm Password",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E2022),
                    modifier = Modifier.padding(start = 4.dp)
                )
                OutlinedTextField(
                    value = viewModel.confirmPassword,
                    onValueChange = { viewModel.confirmPassword = it },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = OutlineColor) },
                    shape = RoundedCornerShape(20.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("••••••••", color = OutlineColor.copy(alpha = 0.5f)) },
                    enabled = !viewModel.isLoading,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFEFF1F8),
                        unfocusedContainerColor = Color(0xFFEFF1F8),
                        disabledContainerColor = Color(0xFFEFF1F8).copy(alpha = 0.6f),
                        focusedBorderColor = PrimaryIndigo,
                        unfocusedBorderColor = Color.Transparent,
                        disabledBorderColor = Color.Transparent
                    )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            SquishyButton(
                onClick = {
                    viewModel.register { uid ->
                        onNavigate(Main)
                    }
                },
                text = "Sign Up",
                enabled = !viewModel.isLoading,
                isLoading = viewModel.isLoading
            )

            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    color = OutlineColor,
                    fontSize = 15.sp
                )
                Text(
                    text = "Login here",
                    color = PrimaryIndigo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.clickable(enabled = !viewModel.isLoading) { onNavigate(Login) }
                )
            }
        }
    }
}

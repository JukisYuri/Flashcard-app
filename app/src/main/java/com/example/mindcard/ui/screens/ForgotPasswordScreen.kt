package com.example.mindcard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.example.mindcard.Login
import com.example.mindcard.ui.viewmodel.AuthViewModel

@Composable
fun ForgotPasswordScreen(
    onNavigate: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel()
) {
    var sentSuccess by remember { mutableStateOf(false) }

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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!sentSuccess) {
                // Circular Icon
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color(0xFFEFF1F8), CircleShape)
                        .shadow(2.dp, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🔑", fontSize = 36.sp)
                }

                Text(
                    text = "Forgot Password?",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E2022)
                )

                Text(
                    text = "Enter your email address and we'll send you a link to reset your password.",
                    fontSize = 14.sp,
                    color = OutlineColor,
                    textAlign = TextAlign.Center
                )

                viewModel.errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = Color.Red,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
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

                Spacer(modifier = Modifier.height(4.dp))

                SquishyButton(
                    onClick = {
                        viewModel.sendPasswordReset {
                            sentSuccess = true
                        }
                    },
                    text = "Send Reset Link",
                    enabled = !viewModel.isLoading,
                    isLoading = viewModel.isLoading
                )
            } else {
                // Success State Card
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(Color(0xFFE8F8EC), CircleShape)
                        .shadow(2.dp, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✉️", fontSize = 36.sp)
                }

                Text(
                    text = "Email Sent!",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E2022)
                )

                Text(
                    text = "A password reset link has been sent to ${viewModel.email}. Please check your inbox and spam folders.",
                    fontSize = 14.sp,
                    color = OutlineColor,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Back to Login",
                color = if (viewModel.isLoading) OutlineColor else PrimaryIndigo,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier
                    .clickable(enabled = !viewModel.isLoading) { onNavigate(Login) }
                    .padding(vertical = 4.dp)
            )
        }
    }
}

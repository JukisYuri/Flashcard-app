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
import androidx.navigation3.runtime.NavKey
import com.example.mindcard.Login
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ForgotPasswordScreen(
    onNavigate: (NavKey) -> Unit,
    modifier: Modifier = Modifier
) {
    val auth = FirebaseAuth.getInstance()
    var email by remember { mutableStateOf("") }
    var sentSuccess by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE8E7FF), // Light purple/indigo
                        Color(0xFFE8F8EC)  // Light green/teal
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
                    text = "Enter your email and we'll send you a recovery link.",
                    fontSize = 15.sp,
                    color = OutlineColor,
                    textAlign = TextAlign.Center
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = Color.Red,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 4.dp),
                        textAlign = TextAlign.Center
                    )
                }

                // Email Address Field Group
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
                        value = email,
                        onValueChange = { email = it },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = OutlineColor) },
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("name@example.com", color = OutlineColor.copy(alpha = 0.5f)) },
                        enabled = !isLoading,
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
                        if (email.isBlank()) {
                            errorMessage = "Please enter your email address."
                            return@SquishyButton
                        }
                        isLoading = true
                        errorMessage = null
                        auth.sendPasswordResetEmail(email.trim())
                            .addOnCompleteListener { task ->
                                isLoading = false
                                if (task.isSuccessful) {
                                    sentSuccess = true
                                } else {
                                    errorMessage = task.exception?.localizedMessage ?: "Failed to send reset email."
                                }
                            }
                    },
                    text = "Send Reset Link",
                    enabled = !isLoading,
                    isLoading = isLoading
                )

                Text(
                    text = "Back to Login",
                    modifier = Modifier
                        .clickable(enabled = !isLoading) { onNavigate(Login) }
                        .padding(top = 8.dp),
                    color = PrimaryIndigo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(SuccessBg.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("✅", fontSize = 40.sp)
                }

                Text(
                    text = "Link Sent!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryGreen
                )

                Text(
                    text = "We've sent a password reset link to $email. Please check your inbox.",
                    fontSize = 16.sp,
                    color = OutlineColor,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                SquishyButton(
                    onClick = { onNavigate(Login) },
                    text = "Back to Login"
                )
            }
        }
    }
}

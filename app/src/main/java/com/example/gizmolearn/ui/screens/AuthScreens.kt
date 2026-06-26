package com.example.gizmolearn.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import com.example.gizmolearn.ForgotPassword
import com.example.gizmolearn.Login
import com.example.gizmolearn.Main
import com.example.gizmolearn.Register

// Color palette definitions matching DESIGN.md
val PrimaryIndigo = Color(0xFF4648D4)
val DarkIndigo = Color(0xFF2F2EBE)
val SecondaryGreen = Color(0xFF006E2F)
val DarkGreen = Color(0xFF005321)
val TertiaryYellow = Color(0xFF735C00)
val DarkYellow = Color(0xFF4E3E00)
val SuccessBg = Color(0xFF6BFF8F)
val BackgroundFrost = Color(0xFFF7F9FB)
val OutlineColor = Color(0xFF767586)
val OutlineVariantColor = Color(0xFFC7C4D7)

@Composable
fun SquishyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = PrimaryIndigo,
    shadowColor: Color = DarkIndigo,
    textColor: Color = Color.White,
    text: String
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(containerColor, RoundedCornerShape(16.dp))
            .border(
                width = 2.dp,
                color = shadowColor,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                color = textColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun LoginScreen(
    onNavigate: (NavKey) -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE1E0FF),
                        BackgroundFrost
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(24.dp))
                .border(1.dp, OutlineVariantColor.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Logo Icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(PrimaryIndigo, RoundedCornerShape(16.dp))
                    .border(2.dp, DarkIndigo, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("🚀", fontSize = 32.sp)
            }

            Text(
                text = "GizmoLearn",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryIndigo
            )

            Text(
                text = "Ready to level up?",
                fontSize = 16.sp,
                color = OutlineColor
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("learner@gizmo.com") }
            )

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                shape = RoundedCornerShape(16.dp),
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("••••••••") }
            )

            Text(
                text = "Forgot Password?",
                color = PrimaryIndigo,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable { onNavigate(ForgotPassword) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            SquishyButton(
                onClick = { onNavigate(Main) },
                text = "Login"
            )

            Text(
                text = "New to GizmoLearn? Sign up here",
                modifier = Modifier
                    .clickable { onNavigate(Register) }
                    .padding(top = 8.dp),
                color = OutlineColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun RegisterScreen(
    onNavigate: (NavKey) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE1E0FF),
                        BackgroundFrost
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(24.dp))
                .border(1.dp, OutlineVariantColor.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(PrimaryIndigo, RoundedCornerShape(16.dp))
                    .border(2.dp, DarkIndigo, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("🚀", fontSize = 32.sp)
            }

            Text(
                text = "Create Account",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryIndigo
            )

            Text(
                text = "Level up your learning journey!",
                fontSize = 15.sp,
                color = OutlineColor
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Alex Gizmo") }
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("alex@gizmo.com") }
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                shape = RoundedCornerShape(16.dp),
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                shape = RoundedCornerShape(16.dp),
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            SquishyButton(
                onClick = { onNavigate(Login) },
                text = "Sign Up"
            )

            Text(
                text = "Already have an account? Login",
                modifier = Modifier
                    .clickable { onNavigate(Login) }
                    .padding(top = 8.dp),
                color = OutlineColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ForgotPasswordScreen(
    onNavigate: (NavKey) -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    var sentSuccess by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE1E0FF),
                        BackgroundFrost
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(24.dp))
                .border(1.dp, OutlineVariantColor.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!sentSuccess) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFFE1E0FF), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🔑", fontSize = 32.sp)
                }

                Text(
                    text = "Forgot Password?",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryIndigo
                )

                Text(
                    text = "Enter your email and we'll send you a recovery link.",
                    fontSize = 15.sp,
                    color = OutlineColor,
                    textAlign = TextAlign.Center
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("name@example.com") }
                )

                Spacer(modifier = Modifier.height(8.dp))

                SquishyButton(
                    onClick = { sentSuccess = true },
                    text = "Send Reset Link"
                )

                Text(
                    text = "Back to Login",
                    modifier = Modifier
                        .clickable { onNavigate(Login) }
                        .padding(top = 8.dp),
                    color = PrimaryIndigo,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(SuccessBg.copy(alpha = 0.2f), RoundedCornerShape(40.dp)),
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

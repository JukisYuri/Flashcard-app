package com.example.mindcard.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.example.mindcard.ForgotPassword
import com.example.mindcard.Main
import com.example.mindcard.Register
import com.example.mindcard.data.Database
import com.example.mindcard.ui.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

@Composable
fun LoginScreen(
    onNavigate: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = viewModel()
) {
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                viewModel.isLoading = true
                auth.signInWithCredential(credential)
                    .addOnCompleteListener { authTask ->
                        viewModel.isLoading = false
                        if (authTask.isSuccessful) {
                            val uid = auth.currentUser?.uid
                            if (uid != null) {
                                Database.initializeUserPersistence(uid)
                            }
                            onNavigate(Main)
                        } else {
                            viewModel.errorMessage = authTask.exception?.localizedMessage ?: "Firebase Sign In with Google failed."
                        }
                    }
            } catch (e: ApiException) {
                viewModel.isLoading = false
                viewModel.errorMessage = "Google Sign In ApiException (Code: ${e.statusCode}): ${e.localizedMessage}. Please verify SHA-1 settings on Firebase."
            }
        } else {
            viewModel.isLoading = false
            viewModel.errorMessage = "Google Sign In cancelled or failed (Result Code: ${result.resultCode}). Please ensure your debug SHA-1 is added to Firebase Console."
        }
    }

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
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
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
                text = "Mind Card",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E2022)
            )

            Text(
                text = "Ready to level up?",
                fontSize = 16.sp,
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

            Spacer(modifier = Modifier.height(4.dp))

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
                    leadingIcon = {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = null,
                            tint = OutlineColor
                        )
                    },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "learner@mindcard.com",
                            color = OutlineColor.copy(alpha = 0.5f)
                        )
                    },
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
                    leadingIcon = {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = OutlineColor
                        )
                    },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "••••••••",
                            color = OutlineColor.copy(alpha = 0.5f)
                        )
                    },
                    visualTransformation = PasswordVisualTransformation(),
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

            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Forgot Password?",
                    color = if (viewModel.isLoading) OutlineColor else PrimaryIndigo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clickable(enabled = !viewModel.isLoading) { onNavigate(ForgotPassword) }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            SquishyButton(
                onClick = {
                    viewModel.login { uid ->
                        Database.initializeUserPersistence(uid)
                        onNavigate(Main)
                    }
                },
                text = "Login",
                enabled = !viewModel.isLoading,
                isLoading = viewModel.isLoading
            )

            // OR Divider
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = OutlineVariantColor.copy(alpha = 0.6f),
                    thickness = 1.dp
                )
                Text(
                    text = "OR",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = OutlineColor.copy(alpha = 0.8f)
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = OutlineVariantColor.copy(alpha = 0.6f),
                    thickness = 1.dp
                )
            }

            // Google Login Button
            OutlinedButton(
                onClick = {
                    viewModel.isLoading = true
                    viewModel.errorMessage = null
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken("705846448248-smuta58jre9j37as30g510flrai5g41h.apps.googleusercontent.com")
                        .requestEmail()
                        .build()
                    val googleSignInClient = GoogleSignIn.getClient(context, gso)
                    googleSignInClient.signOut().addOnCompleteListener {
                        val signInIntent = googleSignInClient.signInIntent
                        launcher.launch(signInIntent)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF1E2022)
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = OutlineVariantColor
                ),
                enabled = !viewModel.isLoading
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    GoogleIcon(modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Login with Google",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E2022)
                    )
                }
            }

            Row(
                modifier = Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "New to Mind Card? ",
                    color = OutlineColor,
                    fontSize = 15.sp
                )
                Text(
                    text = "Sign up here",
                    color = PrimaryIndigo,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.clickable(enabled = !viewModel.isLoading) { onNavigate(Register) }
                )
            }
        }
    }
}

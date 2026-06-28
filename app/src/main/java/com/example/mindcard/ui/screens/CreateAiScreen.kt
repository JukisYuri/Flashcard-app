package com.example.mindcard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.example.mindcard.Main
import com.example.mindcard.ui.main.BackgroundFrost
import com.example.mindcard.ui.main.OutlineColor
import com.example.mindcard.ui.main.OutlineVariantColor
import com.example.mindcard.ui.main.PrimaryIndigo
import com.example.mindcard.ui.viewmodel.CreateAiViewModel
import kotlinx.coroutines.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAiScreen(
    onNavigate: (NavKey) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreateAiViewModel = viewModel()
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Generate with AI", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { onNavigate(Main) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundFrost)
            )
        },
        containerColor = BackgroundFrost
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (!viewModel.isGenerating) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(24.dp))
                        .border(1.dp, OutlineVariantColor.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(PrimaryIndigo.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✨", fontSize = 32.sp)
                    }

                    Text(
                        text = "AI Deck Generator",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF191C1E)
                    )

                    Text(
                        text = "Enter a topic, list of words, or a theme, and our AI will craft a personalized vocabulary deck with pronunciation, definitions, and examples.",
                        fontSize = 13.sp,
                        color = OutlineColor,
                        textAlign = TextAlign.Center
                    )

                    OutlinedTextField(
                        value = viewModel.prompt,
                        onValueChange = { viewModel.prompt = it },
                        label = { Text("What do you want to learn?") },
                        placeholder = { Text("e.g., French restaurant vocabulary, Medical terminology...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = OutlineVariantColor.copy(alpha = 0.5f),
                            focusedBorderColor = PrimaryIndigo
                        )
                    )

                    // Topic shortcuts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Food", "Travel", "Business").forEach { topic ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFEFF1F8))
                                    .clickable { viewModel.prompt = "Essential English words for $topic" }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = topic,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OutlineColor
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                viewModel.generateDeck(
                                    onSuccess = { onNavigate(Main) },
                                    showToast = { msg ->
                                        android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                        },
                        enabled = viewModel.prompt.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                    ) {
                        Text("✨ Generate My Set", modifier = Modifier.padding(vertical = 8.dp), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(color = PrimaryIndigo)
                    Text("Mind Card AI is crafting your flashcards...", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

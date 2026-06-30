package com.example.mindcard.lockscreen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mindcard.data.Database
import com.example.mindcard.theme.MindCardTheme

class LockScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Database.initialize(applicationContext)

        setContent {
            MindCardTheme {
                LockScreenContent(onDismiss = { finish() })
            }
        }
    }
}

@Composable
fun LockScreenContent(onDismiss: () -> Unit) {
    var isFlipped by remember { mutableStateOf(false) }
    val allCards = remember { Database.decks.flatMap { it.cards }.shuffled().take(3) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var answeredCount by remember { mutableIntStateOf(0) }

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0F1A))
            .padding(24.dp)
    ) {
        // Close button
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .size(40.dp)
                .background(Color.White.copy(alpha = 0.2f), CircleShape)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
        }

        if (allCards.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📚", fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No cards to review", fontSize = 18.sp, color = Color.White)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Open MindCard to add vocabulary", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))
                    ) {
                        Text("OK")
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Progress
                Text(
                    "${currentIndex + 1} / ${allCards.size}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Card
                val card = allCards[currentIndex]
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .graphicsLayer {
                            rotationY = rotation
                            cameraDistance = 12f * density
                        }
                        .clickable { isFlipped = !isFlipped }
                        .background(Color(0xFF1E2035), RoundedCornerShape(24.dp))
                        .border(2.dp, Color(0xFF2D3050), RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (rotation <= 90f) {
                        // FRONT
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Box(
                                modifier = Modifier.background(Color(0xFF6C63FF).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(card.pos, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9D96FF))
                            }
                            Text(card.englishWord, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
                            Text(card.pronunciation, fontSize = 16.sp, fontStyle = FontStyle.Italic, color = Color.Gray)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Tap to reveal", fontSize = 12.sp, color = Color.Gray.copy(alpha = 0.6f))
                        }
                    } else {
                        // BACK
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.graphicsLayer { rotationY = 180f }.padding(24.dp)
                        ) {
                            Text(card.definition, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9D96FF), textAlign = TextAlign.Center)
                            if (card.exampleSentence.isNotEmpty()) {
                                HorizontalDivider(color = Color(0xFF2D3050), modifier = Modifier.padding(vertical = 8.dp))
                                Text("\"${card.exampleSentence}\"", fontSize = 14.sp, fontStyle = FontStyle.Italic, color = Color.Gray, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Rating buttons
                if (isFlipped) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FeedbackButton("Again", Color(0xFFFF6B6B).copy(alpha = 0.2f), Color(0xFFFF6B6B), Modifier.weight(1f)) {
                            answeredCount++
                            if (currentIndex < allCards.size - 1) { currentIndex++; isFlipped = false } else { onDismiss() }
                        }
                        FeedbackButton("Hard", Color(0xFFFFD93D).copy(alpha = 0.2f), Color(0xFFFFD93D), Modifier.weight(1f)) {
                            answeredCount++
                            if (currentIndex < allCards.size - 1) { currentIndex++; isFlipped = false } else { onDismiss() }
                        }
                        FeedbackButton("Easy", Color(0xFF00D68F).copy(alpha = 0.2f), Color(0xFF00D68F), Modifier.weight(1f)) {
                            answeredCount++
                            if (currentIndex < allCards.size - 1) { currentIndex++; isFlipped = false } else { onDismiss() }
                        }
                    }
                } else {
                    Button(
                        onClick = { isFlipped = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C63FF))
                    ) {
                        Text("FLIP CARD", modifier = Modifier.padding(vertical = 8.dp), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun FeedbackButton(text: String, containerColor: Color, textColor: Color, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .background(containerColor, RoundedCornerShape(12.dp))
            .border(2.dp, textColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = textColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

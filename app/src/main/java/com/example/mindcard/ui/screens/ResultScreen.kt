package com.example.mindcard.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import com.example.mindcard.FlashcardStudy
import com.example.mindcard.Main
import com.example.mindcard.ui.main.PrimaryIndigo
import com.example.mindcard.ui.main.SecondaryGreen
import com.example.mindcard.ui.main.AccentYellow

@Composable
fun ResultScreen(
    deckId: String,
    accuracy: Int,
    xpEarned: Int,
    timeMinutes: Int,
    onNavigate: (NavKey) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Well Done!", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = PrimaryIndigo)
            Text("You've crushed another lesson.", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Box(
                modifier = Modifier
                    .size(140.dp)
                    .background(AccentYellow.copy(alpha = 0.2f), CircleShape)
                    .border(2.dp, AccentYellow, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🏆", fontSize = 72.sp)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatResultItem("Accuracy", "$accuracy%", "🎯", SecondaryGreen, Modifier.weight(1f))
                StatResultItem("XP Gained", "+$xpEarned", "⚡", AccentYellow, Modifier.weight(1f))
                StatResultItem("Duration", "${timeMinutes}m", "⏱️", PrimaryIndigo, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(
                    onClick = { onNavigate(FlashcardStudy(deckId)) },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryIndigo),
                    border = BorderStroke(2.dp, PrimaryIndigo)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Replay", fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = { onNavigate(Main) },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                ) {
                    Text("Home", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StatResultItem(
    label: String,
    value: String,
    icon: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(icon, fontSize = 24.sp)
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
            Text(label.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

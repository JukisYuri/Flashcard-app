package com.example.mindcard.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    text: String,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    val clickableModifier = if (enabled && !isLoading) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(clickableModifier)
            .background(
                if (enabled && !isLoading) containerColor else containerColor.copy(alpha = 0.6f),
                CircleShape
            )
            .border(
                width = 2.dp,
                color = if (enabled && !isLoading) shadowColor else shadowColor.copy(alpha = 0.6f),
                shape = CircleShape
            )
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = textColor,
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.5.dp
            )
        } else {
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
}

@Composable
fun GoogleIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(20.dp)) {
        val scale = size.width / 24f
        
        val bluePath = PathParser().parsePathString("M23.49 12.27c0-.79-.07-1.54-.19-2.27H12v4.51h6.47c-.29 1.48-1.14 2.73-2.4 3.58v3h3.86c2.26-2.09 3.56-5.17 3.56-8.82z").toPath()
        val greenPath = PathParser().parsePathString("M12 24c3.24 0 5.95-1.08 7.93-2.91l-3.86-3c-1.08.72-2.45 1.16-4.07 1.16-3.13 0-5.78-2.11-6.73-4.96H1.29v3.09C3.26 21.3 7.31 24 12 24z").toPath()
        val yellowPath = PathParser().parsePathString("M5.27 14.29c-.25-.72-.38-1.49-.38-2.29s.14-1.57.38-2.29V6.62H1.29C.47 8.24 0 10.06 0 12s.47 3.76 1.29 5.38l3.98-3.09z").toPath()
        val redPath = PathParser().parsePathString("M12 4.75c1.77 0 3.35.61 4.6 1.8l3.42-3.42C17.95 1.19 15.24 0 12 0 7.31 0 3.26 2.7 1.29 6.62l3.98 3.09c.95-2.85 3.6-4.96 6.73-4.96z").toPath()
        
        scale(scale, scale, pivot = androidx.compose.ui.geometry.Offset.Zero) {
            drawPath(bluePath, Color(0xFF4285F4))
            drawPath(greenPath, Color(0xFF34A853))
            drawPath(yellowPath, Color(0xFFFBBC05))
            drawPath(redPath, Color(0xFFEA4335))
        }
    }
}

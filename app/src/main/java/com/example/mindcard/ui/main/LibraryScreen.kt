package com.example.mindcard.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mindcard.data.Card
import com.example.mindcard.ui.screens.*
import com.example.mindcard.ui.viewmodel.LibraryViewModel

@Composable
fun LibraryScreen(
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = viewModel()
) {
    val totalCount = viewModel.getAllCardsCount()
    val filteredCards = viewModel.getFilteredCards()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("My Library", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191C1E))
        Text("Review all $totalCount words you've learned so far.", fontSize = 14.sp, color = OutlineColor)

        // Search Bar
        OutlinedTextField(
            value = viewModel.searchQuery,
            onValueChange = { viewModel.searchQuery = it },
            placeholder = { Text("Search words, definitions...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = OutlineVariantColor.copy(alpha = 0.5f),
                focusedBorderColor = PrimaryIndigo
            )
        )

        // Alphabet quick filter list
        val letters = ('A'..'Z').toList()
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (viewModel.selectedLetter == null) PrimaryIndigo else OutlineVariantColor.copy(alpha = 0.2f))
                        .clickable { viewModel.selectedLetter = null }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        "All",
                        color = if (viewModel.selectedLetter == null) Color.White else OutlineColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            items(letters) { letter ->
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (viewModel.selectedLetter == letter) PrimaryIndigo else OutlineVariantColor.copy(alpha = 0.2f))
                        .clickable { viewModel.selectedLetter = if (viewModel.selectedLetter == letter) null else letter }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        letter.toString(),
                        color = if (viewModel.selectedLetter == letter) Color.White else OutlineColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (filteredCards.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (totalCount == 0) "No words in your library yet.\nStart learning a deck to add words!" else "No search results matches.",
                    textAlign = TextAlign.Center,
                    color = OutlineColor,
                    fontSize = 15.sp
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredCards) { card ->
                    LibraryWordCard(card = card)
                }
            }
        }
    }
}

@Composable
fun LibraryWordCard(card: Card) {
    Box(
        modifier = Modifier
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.dp, OutlineVariantColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(PrimaryIndigo.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(card.pos, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PrimaryIndigo)
            }

            Text(card.englishWord, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191C1E))
            Text(card.pronunciation, fontSize = 12.sp, fontStyle = FontStyle.Italic, color = OutlineColor)

            HorizontalDivider(color = OutlineVariantColor.copy(alpha = 0.3f))

            Text(card.definition, fontSize = 13.sp, color = OutlineColor, maxLines = 3, overflow = TextOverflow.Ellipsis)
            if (card.exampleSentence.isNotEmpty()) {
                Text(
                    "\"${card.exampleSentence}\"",
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic,
                    color = OutlineColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}

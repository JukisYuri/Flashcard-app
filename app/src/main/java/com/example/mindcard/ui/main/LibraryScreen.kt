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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import com.example.mindcard.data.Database
import com.example.mindcard.ui.viewmodel.LibraryViewModel

@Composable
fun LibraryScreen(
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = viewModel()
) {
    val totalCount = viewModel.getAllCardsCount()
    val favVersion = viewModel.favoriteVersion
    val allCards = remember(favVersion) { viewModel.decks.flatMap { it.cards } }
    val filteredCards = remember(allCards, viewModel.searchQuery, viewModel.selectedLetter) {
        allCards.filter { card ->
            val matchesQuery = card.englishWord.contains(viewModel.searchQuery, ignoreCase = true) ||
                    card.definition.contains(viewModel.searchQuery, ignoreCase = true)
            val matchesLetter = viewModel.selectedLetter == null ||
                    card.englishWord.startsWith(viewModel.selectedLetter.toString(), ignoreCase = true)
            matchesQuery && matchesLetter
        }
    }
    var showFavoritesOnly by remember { mutableStateOf(false) }
    val displayCards = if (showFavoritesOnly) filteredCards.filter { it.isFavorite } else filteredCards

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("My Library", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Text("Review all $totalCount words you've learned so far.", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        OutlinedTextField(
            value = viewModel.searchQuery,
            onValueChange = { viewModel.searchQuery = it },
            placeholder = { Text("Search words, definitions...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                focusedBorderColor = PrimaryIndigo,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = !showFavoritesOnly,
                onClick = { showFavoritesOnly = false },
                label = { Text("All ($totalCount)") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = PrimaryIndigo,
                    selectedLabelColor = Color.White
                )
            )
            val favCount = viewModel.getFavoriteCount()
            FilterChip(
                selected = showFavoritesOnly,
                onClick = { showFavoritesOnly = true },
                label = { Text("★ Favorites ($favCount)") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFFF6B6B),
                    selectedLabelColor = Color.White
                )
            )
        }

        val letters = ('A'..'Z').toList()
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            item {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (viewModel.selectedLetter == null) PrimaryIndigo else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { viewModel.selectedLetter = null }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text("All", color = if (viewModel.selectedLetter == null) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                }
            }
            items(letters) { letter ->
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (viewModel.selectedLetter == letter) PrimaryIndigo else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { viewModel.selectedLetter = if (viewModel.selectedLetter == letter) null else letter }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        letter.toString(),
                        color = if (viewModel.selectedLetter == letter) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (displayCards.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    text = if (showFavoritesOnly) "No favorite words yet.\nTap ★ on a word to add it to favorites."
                             else if (totalCount == 0) "No words in your library yet.\nStart learning a deck to add words!"
                             else "No search results matches.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                items(displayCards, key = { it.id }) { card ->
                    LibraryWordCard(card = card, onToggleFavorite = {
                        val deck = viewModel.decks.find { deck -> deck.cards.any { it.id == card.id } }
                        if (deck != null) viewModel.toggleFavorite(deck.id, card.id)
                    })
                }
            }
        }
    }
}

@Composable
fun LibraryWordCard(card: Card, onToggleFavorite: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(PrimaryIndigo.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(card.pos, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PrimaryIndigo)
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clickable(onClick = onToggleFavorite),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (card.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Toggle favorite",
                        tint = if (card.isFavorite) Color(0xFFFF6B6B) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Text(card.englishWord, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(card.pronunciation, fontSize = 12.sp, fontStyle = FontStyle.Italic, color = MaterialTheme.colorScheme.onSurfaceVariant)
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Text(card.definition, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis)
            if (card.exampleSentence.isNotEmpty()) {
                Text(
                    "\"${card.exampleSentence}\"",
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}

package com.example.mindcard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import com.example.mindcard.CreateAI
import com.example.mindcard.CreateCard
import com.example.mindcard.Main
import com.example.mindcard.data.Database

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateDeckScreen(
    onNavigate: (NavKey) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Languages") }
    var showCategoryMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Deck", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { onNavigate(Main) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        if (name.isNotEmpty()) {
                            Database.addDeck(name, category)
                            onNavigate(Main)
                        }
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundFrost)
            )
        },
        containerColor = BackgroundFrost
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // AI Generator shortcut button
            Button(
                onClick = { onNavigate(CreateAI) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text("✨  Generate with AI", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Set Details section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .border(1.dp, OutlineVariantColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Set Details", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191C1E))

                // Name input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Set Name") },
                    placeholder = { Text("e.g. Spanish Verbs") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Category selector
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        label = { Text("Category") },
                        readOnly = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { showCategoryMenu = true }) {
                                Text("▼", fontSize = 12.sp)
                            }
                        }
                    )
                    DropdownMenu(
                        expanded = showCategoryMenu,
                        onDismissRequest = { showCategoryMenu = false }
                    ) {
                        listOf("Languages", "Science", "History", "Math").forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    showCategoryMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Actions section for adding cards
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(20.dp))
                    .border(1.dp, OutlineVariantColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Add Cards", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF191C1E))

                Text(
                    text = "Save the deck first to add cards manually, or use AI generation to build vocabulary instantly.",
                    fontSize = 13.sp,
                    color = OutlineColor,
                    textAlign = TextAlign.Center
                )

                Button(
                    onClick = {
                        if (name.isNotEmpty()) {
                            val newDeck = Database.addDeck(name, category)
                            onNavigate(CreateCard(newDeck.id))
                        }
                    },
                    enabled = name.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save & Add Card Manually")
                }
            }
        }
    }
}

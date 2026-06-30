package com.example.mindcard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import kotlin.time.Duration.Companion.milliseconds

suspend fun fetchMeaningFromQwen(word: String): String? {
    return withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val apiKey = com.example.mindcard.BuildConfig.QWEN_API_KEY
            val url = "https://dashscope-intl.aliyuncs.com/compatible-mode/v1/chat/completions"

            val jsonBody = JSONObject().apply {
                put("model", "qwen-plus")

                val messages = JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", "Bạn là một từ điển. Dịch từ hoặc cụm từ được cung cấp sang tiếng Việt. Trả về duy nhất một dòng nghĩa ngắn gọn nhất để làm flashcard, không giải thích, không gạch đầu dòng.")
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", word)
                    })
                }
                put("messages", messages)
                put("temperature", 0.1)
            }

            // Đóng gói request
            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $apiKey")
                .post(requestBody)
                .build()

            // Thực thi và đọc kết quả
            val response = client.newCall(request).execute()
            val responseString = response.body?.string()
            if (response.isSuccessful && responseString != null) {
                val jsonObject = JSONObject(responseString)
                val choices = jsonObject.getJSONArray("choices")
                val firstChoice = choices.getJSONObject(0)
                val message = firstChoice.getJSONObject("message")
                return@withContext message.getString("content").trim()
            } else {
                return@withContext "API Lỗi code ${response.code}: $responseString"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext "Lỗi Code: ${e.message}"
        }
    }
}

@Composable
fun QuickAddScreen(initialText: String, onClose: () -> Unit) {
    var frontText by remember { mutableStateOf(initialText) }
    var backText by remember { mutableStateOf("") }

    val decks = remember { com.example.mindcard.data.Database.decks }
    var selectedDeck by remember { mutableStateOf(decks.firstOrNull()) }
    var showDeckMenu by remember { mutableStateOf(false) }

    var suggestedMeaning by remember { mutableStateOf<String?>(null) }
    var isLoadingSuggestion by remember { mutableStateOf(false) }

    LaunchedEffect(frontText) {
        val wordToSearch = frontText.trim()
        if (wordToSearch.isNotBlank()) {
            isLoadingSuggestion = true
            suggestedMeaning = null
            delay(1000.milliseconds)

            val aiResponse = fetchMeaningFromQwen(wordToSearch)
            if (!aiResponse.isNullOrEmpty()) {
                suggestedMeaning = aiResponse
            }

            isLoadingSuggestion = false
        } else {
            suggestedMeaning = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { onClose() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                .padding(24.dp)
                .clickable(enabled = false) {}
        ) {
            Text(
                text = "Quick Add",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            if (isLoadingSuggestion || suggestedMeaning != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, start = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isLoadingSuggestion) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Looking up meaning...", fontSize = 12.sp, color = Color.Gray)
                    } else if (suggestedMeaning != null) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Suggestion",
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFF6C63FF)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Suggestion: $suggestedMeaning",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF6C63FF),
                            modifier = Modifier
                                .clickable {
                                    // Khi user bấm vào dòng này, nhét luôn text vào ô Meaning
                                    backText = suggestedMeaning!!
                                }
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Box {
                OutlinedTextField(
                    value = selectedDeck?.name ?: "No Deck Available",
                    onValueChange = {},
                    label = { Text("Save to Deck") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().clickable { showDeckMenu = true },
                    trailingIcon = {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null
                        )
                    },
                    enabled = false
                )
                DropdownMenu(
                    expanded = showDeckMenu,
                    onDismissRequest = { showDeckMenu = false }
                ) {
                    decks.forEach { deck ->
                        DropdownMenuItem(
                            text = { Text(deck.name) },
                            onClick = {
                                selectedDeck = deck
                                showDeckMenu = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = frontText,
                onValueChange = { frontText = it },
                label = { Text("Word") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = backText,
                onValueChange = { backText = it },
                label = { Text("Meaning") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onClose) { Text("Cancel") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (selectedDeck != null && frontText.isNotBlank()) {
                            val newCard = com.example.mindcard.data.Card(
                                id = java.util.UUID.randomUUID().toString(),
                                englishWord = frontText.trim(),
                                definition = backText.trim(),
                                pronunciation = "",
                                pos = "Noun",
                                exampleSentence = "",
                                synonyms = ""
                            )
                            com.example.mindcard.data.Database.addCardToDeck(
                                selectedDeck!!.id,
                                newCard
                            )
                        }
                        onClose()
                    },
                    enabled = selectedDeck != null && frontText.isNotBlank()
                ) {
                    Text("Save")
                }
            }
        }
    }
}
package com.example.mindcard.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.mindcard.data.Database
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** UI tests for [com.example.mindcard.ui.main.MainScreen]. */
class MainScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  fun setup() {
    Database.seedDemoData()
    composeTestRule.setContent {
      MainScreen(onItemClick = {})
    }
  }

  @Test
  fun testDemoDecks_exist() {
    composeTestRule.onNodeWithText("Basic Greetings").assertExists()
    composeTestRule.onNodeWithText("Food & Dining").assertExists()
  }
}

package com.example.mindcard

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.mindcard.ui.main.MainScreen
import com.example.mindcard.ui.screens.*

import androidx.compose.runtime.remember
import com.google.firebase.auth.FirebaseAuth
import com.example.mindcard.data.Database

@Composable
fun MainNavigation() {
  val auth = remember { FirebaseAuth.getInstance() }
  val currentUser = remember { auth.currentUser }
  val initialScreen = if (currentUser != null) {
      Database.initializeUserPersistence(currentUser.uid)
      Main
  } else {
      Login
  }
  val backStack = rememberNavBackStack(initialScreen)

  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider =
      entryProvider {
        entry<Login> {
          LoginScreen(onNavigate = { navKey -> backStack.add(navKey) })
        }
        entry<Register> {
          RegisterScreen(onNavigate = { navKey -> backStack.add(navKey) })
        }
        entry<ForgotPassword> {
          ForgotPasswordScreen(onNavigate = { navKey -> backStack.add(navKey) })
        }
        entry<Main> {
          MainScreen(onItemClick = { navKey -> backStack.add(navKey) })
        }
        entry<FlashcardStudy> { entry ->
          FlashcardStudyScreen(deckId = entry.deckId, onNavigate = { navKey -> backStack.add(navKey) })
        }
        entry<StudyResult> { entry ->
          val res = entry
          ResultScreen(
              deckId = res.deckId,
              accuracy = res.accuracy,
              xpEarned = res.xpEarned,
              timeMinutes = res.timeMinutes,
              onNavigate = { navKey -> backStack.add(navKey) }
          )
        }
        entry<CreateDeck> {
          CreateDeckScreen(onNavigate = { navKey -> backStack.add(navKey) })
        }
        entry<CreateAI> {
          CreateAiScreen(onNavigate = { navKey -> backStack.add(navKey) })
        }
        entry<CreateCard> { entry ->
          CreateCardScreen(deckId = entry.deckId, onNavigate = { navKey -> backStack.add(navKey) })
        }
      },
  )
}

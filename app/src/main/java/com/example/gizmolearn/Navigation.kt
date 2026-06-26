package com.example.gizmolearn

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.gizmolearn.ui.main.MainScreen
import com.example.gizmolearn.ui.screens.*

@Composable
fun MainNavigation() {
  val backStack = rememberNavBackStack(Login)

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

package com.example.mindcard

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.mindcard.ui.main.MainScreen
import com.example.mindcard.ui.screens.*

import androidx.compose.runtime.remember
import com.example.mindcard.data.Database
import com.example.mindcard.data.repository.AuthRepository

@Composable
fun MainNavigation() {
  val authRepository = remember { AuthRepository() }
  val initialScreen = if (authRepository.isUserSignedIn()) {
      Database.initializeUserPersistence(authRepository.getCurrentUser()!!.uid)
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
          MainScreen(
            onItemClick = { navKey ->
              if (navKey == Login) {
                authRepository.signOut()
                backStack.clear()
                backStack.add(Login)
              } else {
                backStack.add(navKey)
              }
            }
          )
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
        entry<CreateDeck> { entry ->
          CreateDeckScreen(deckId = entry.deckId, onNavigate = { navKey -> backStack.add(navKey) })
        }
        entry<CreateAI> {
          CreateAiScreen(onNavigate = { navKey -> backStack.add(navKey) })
        }
        entry<CreateCard> { entry ->
          CreateCardScreen(deckId = entry.deckId, onNavigate = { navKey -> backStack.add(navKey) })
        }
        entry<Settings> {
          SettingsScreen(onNavigate = { navKey -> backStack.add(navKey) })
        }
      },
  )
}

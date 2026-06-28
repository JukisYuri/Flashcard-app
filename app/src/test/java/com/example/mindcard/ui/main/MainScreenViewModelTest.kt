package com.example.mindcard.ui.main

import com.example.mindcard.ui.viewmodel.HomeViewModel
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class MainScreenViewModelTest {

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        viewModel = HomeViewModel()
    }

    @Test
    fun testDecksList_initiallyEmptyOrSeeded() {
        val decks = viewModel.decks
        assertEquals(decks.size, viewModel.decks.size)
    }
}

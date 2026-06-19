package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.data.local.AppDatabase
import com.example.data.repository.TutorRepository
import com.example.ui.TutorScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.TutorViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Initialize Room Database, Repo and TutorViewModel
    val database = AppDatabase.getDatabase(this)
    val historyDao = database.historyDao()
    val repository = TutorRepository(historyDao)
    
    val viewModel: TutorViewModel by viewModels {
      TutorViewModel.Factory(repository)
    }

    setContent {
      MyApplicationTheme {
        TutorScreen(viewModel = viewModel)
      }
    }
  }
}


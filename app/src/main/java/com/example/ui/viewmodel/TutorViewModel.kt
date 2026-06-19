package com.example.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.HistoryEntity
import com.example.data.repository.TutorRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface TutorUiState {
    object Idle : TutorUiState
    object Loading : TutorUiState
    data class Success(
        val englishTerm: String,
        val englishDefinition: String,
        val malayalamExplanation: String,
        val analogyTitle: String,
        val analogyDetails: String,
        val tutorEncouragement: String,
        val savedId: Int? = null
    ) : TutorUiState
    data class Error(val message: String) : TutorUiState
}

class TutorViewModel(private val repository: TutorRepository) : ViewModel() {

    var englishTermInput by mutableStateOf("")
    var englishDefinitionInput by mutableStateOf("")

    var uiState: TutorUiState by mutableStateOf(TutorUiState.Idle)
        private set

    val historyList: StateFlow<List<HistoryEntity>> = repository.allHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val bookmarkedList: StateFlow<List<HistoryEntity>> = repository.bookmarkedHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun explainTerm() {
        val term = englishTermInput.trim()
        val definition = englishDefinitionInput.trim()

        if (term.isEmpty()) {
            uiState = TutorUiState.Error("പഠിക്കേണ്ട വാക്ക് എന്താണെന്ന് ദയവായി മുകളിൽ എഴുതുക! (Please enter a term to explain!)")
            return
        }

        viewModelScope.launch {
            uiState = TutorUiState.Loading
            try {
                // Fetch from Gemini API
                val response = repository.fetchExplanationFromAI(term, definition)

                // Save to Room local DB
                val entity = HistoryEntity(
                    englishTerm = response.englishTerm,
                    englishDefinition = response.definition,
                    malayalamExplanation = response.explanation,
                    analogyTitle = response.analogyTitle,
                    analogyDetails = response.analogyDetails,
                    tutorEncouragement = response.encouragement
                )
                val newId = repository.insertHistory(entity).toInt()

                uiState = TutorUiState.Success(
                    englishTerm = response.englishTerm,
                    englishDefinition = response.definition,
                    malayalamExplanation = response.explanation,
                    analogyTitle = response.analogyTitle,
                    analogyDetails = response.analogyDetails,
                    tutorEncouragement = response.encouragement,
                    savedId = newId
                )

                // Optional: Clear inputs for next search
                englishTermInput = ""
                englishDefinitionInput = ""
            } catch (e: Exception) {
                uiState = TutorUiState.Error(e.localizedMessage ?: "എന്തോ തകരാർ സംഭവിച്ചു. ദയവായി വീണ്ടും ശ്രമിക്കുക!")
            }
        }
    }

    fun selectHistoryItem(item: HistoryEntity) {
        uiState = TutorUiState.Success(
            englishTerm = item.englishTerm,
            englishDefinition = item.englishDefinition,
            malayalamExplanation = item.malayalamExplanation,
            analogyTitle = item.analogyTitle,
            analogyDetails = item.analogyDetails,
            tutorEncouragement = item.tutorEncouragement,
            savedId = item.id
        )
    }

    fun toggleBookmark(id: Int, currentBookmarked: Boolean) {
        viewModelScope.launch {
            repository.updateBookmark(id, !currentBookmarked)
            
            // If the currently viewed item is the one bookmarked, update UI state so bookmark icon changes
            val state = uiState
            if (state is TutorUiState.Success && state.savedId == id) {
                // UI state doesn't hold bookmark value directly, but we let it refresh via Room history list if needed.
            }
        }
    }

    fun deleteHistoryItem(id: Int) {
        viewModelScope.launch {
            repository.deleteHistory(id)
            val state = uiState
            if (state is TutorUiState.Success && state.savedId == id) {
                uiState = TutorUiState.Idle
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            uiState = TutorUiState.Idle
        }
    }

    fun resetState() {
        uiState = TutorUiState.Idle
    }

    class Factory(private val repository: TutorRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TutorViewModel::class.java)) {
                return TutorViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

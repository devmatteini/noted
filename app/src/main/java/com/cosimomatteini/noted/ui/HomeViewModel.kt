package com.cosimomatteini.noted.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.features.Notes
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val activeNotes: List<ActiveNote> = emptyList(),
)

class HomeViewModel(
    notes: Notes,
    private val onCreateNoteRequested: () -> Unit = {},
) : ViewModel() {
    val uiState: StateFlow<HomeUiState> = notes()
        .map { notes -> HomeUiState(notes.filterIsInstance<ActiveNote>()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(),
        )

    fun onCreateNote() {
        onCreateNoteRequested()
    }
}

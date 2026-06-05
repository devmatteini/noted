package com.cosimomatteini.noted.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.features.ObserveNotes
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val activeNotes: List<ActiveNote> = emptyList(),
)

class HomeViewModel(
    observeNotes: ObserveNotes,
) : ViewModel() {
    val uiState: StateFlow<HomeUiState> = observeNotes()
        .map { notes -> HomeUiState(notes.filterIsInstance<ActiveNote>()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(),
        )
}

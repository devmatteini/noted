package com.cosimomatteini.noted.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.ArchivedNote
import com.cosimomatteini.noted.domain.Note
import com.cosimomatteini.noted.features.Notes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val notes: List<Note> = emptyList(),
    val filter: HomeFilter = HomeFilter.Active
)

enum class HomeFilter {
    Active,
    Archived
}

class HomeViewModel(notes: Notes) : ViewModel() {
    private val filter = MutableStateFlow(HomeFilter.Active)

    val uiState: StateFlow<HomeUiState> = notes()
        .combine(filter) { notes, filter ->
            HomeUiState(
                notes = visibleNotes(notes, filter),
                filter = filter
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState()
        )

    fun showActiveNotes() {
        filter.value = HomeFilter.Active
    }

    fun showArchivedNotes() {
        filter.value = HomeFilter.Archived
    }
}

internal fun visibleNotes(notes: List<Note>, filter: HomeFilter): List<Note> = when (filter) {
    HomeFilter.Active -> notes.filterIsInstance<ActiveNote>()
    HomeFilter.Archived -> notes.filterIsInstance<ArchivedNote>()
}

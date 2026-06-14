package com.cosimomatteini.noted.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.ArchivedNote
import com.cosimomatteini.noted.domain.DiscardedNote
import com.cosimomatteini.noted.domain.Note
import com.cosimomatteini.noted.features.Notes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val notes: List<Note> = emptyList(),
    val destination: HomeDestination = HomeDestination.Notes
)

enum class HomeDestination {
    Notes,
    Archive,
    Trash
}

class HomeViewModel(notes: Notes) : ViewModel() {
    private val destination = MutableStateFlow(HomeDestination.Notes)

    val uiState: StateFlow<HomeUiState> = notes()
        .combine(destination) { notes, destination ->
            HomeUiState(
                notes = visibleNotes(notes, destination),
                destination = destination
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState()
        )

    fun showNotes() {
        destination.value = HomeDestination.Notes
    }

    fun showArchive() {
        destination.value = HomeDestination.Archive
    }

    fun showTrash() {
        destination.value = HomeDestination.Trash
    }
}

internal fun visibleNotes(notes: List<Note>, destination: HomeDestination): List<Note> =
    when (destination) {
        HomeDestination.Notes -> notes.filterIsInstance<ActiveNote>()
        HomeDestination.Archive -> notes.filterIsInstance<ArchivedNote>()
        HomeDestination.Trash -> notes.filterIsInstance<DiscardedNote>()
    }

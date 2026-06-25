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
    val destination: HomeDestination = HomeDestination.Notes,
    val layout: NotesLayout = NotesLayout.List
)

enum class HomeDestination {
    Notes,
    Archive,
    Trash
}

class HomeViewModel(notes: Notes, private val notesLayoutPreference: NotesLayoutPreference) :
    ViewModel() {
    private val destination = MutableStateFlow(HomeDestination.Notes)
    private val layout = MutableStateFlow(notesLayoutPreference.load())

    val uiState: StateFlow<HomeUiState> = notes()
        .combine(destination) { notes, destination -> notes to destination }
        .combine(layout) { (notes, destination), layout ->
            HomeUiState(
                notes = visibleNotes(notes, destination),
                destination = destination,
                layout = layout
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(layout = layout.value)
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

    fun toggleLayout() {
        val nextLayout = when (layout.value) {
            NotesLayout.List -> NotesLayout.Grid
            NotesLayout.Grid -> NotesLayout.List
        }
        layout.value = nextLayout
        notesLayoutPreference.save(nextLayout)
    }
}

internal fun visibleNotes(notes: List<Note>, destination: HomeDestination): List<Note> =
    when (destination) {
        HomeDestination.Notes -> notes.filterIsInstance<ActiveNote>()
        HomeDestination.Archive -> notes.filterIsInstance<ArchivedNote>()
        HomeDestination.Trash -> notes.filterIsInstance<DiscardedNote>()
    }

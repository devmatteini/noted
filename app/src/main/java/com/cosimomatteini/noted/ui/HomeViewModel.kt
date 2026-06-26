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
    val layout: NotesLayout = NotesLayout.List,
    val searchQuery: String = "",
    val searchResults: SearchResults = SearchResults()
)

data class SearchResults(
    val activeNotes: List<ActiveNote> = emptyList(),
    val archivedNotes: List<ArchivedNote> = emptyList(),
    val discardedNotes: List<DiscardedNote> = emptyList()
)

val SearchResults.isEmpty: Boolean
    get() = activeNotes.isEmpty() && archivedNotes.isEmpty() && discardedNotes.isEmpty()

internal fun searchResults(notes: List<Note>, query: String): SearchResults {
    val normalizedQuery = query.trim()
    if (normalizedQuery.isEmpty()) {
        return SearchResults()
    }

    val matchingNotes = notes.filter { note -> note.matches(normalizedQuery) }
    return SearchResults(
        activeNotes = matchingNotes.filterIsInstance<ActiveNote>(),
        archivedNotes = matchingNotes.filterIsInstance<ArchivedNote>(),
        discardedNotes = matchingNotes.filterIsInstance<DiscardedNote>()
    )
}

private fun Note.matches(query: String): Boolean = title.value.contains(query, ignoreCase = true) ||
    description.value.contains(query, ignoreCase = true)

enum class HomeDestination {
    Notes,
    Archive,
    Trash
}

class HomeViewModel(notes: Notes, private val notesLayoutPreference: NotesLayoutPreference) :
    ViewModel() {
    private val destination = MutableStateFlow(HomeDestination.Notes)
    private val layout = MutableStateFlow(notesLayoutPreference.load())
    private val searchQuery = MutableStateFlow("")

    val uiState: StateFlow<HomeUiState> = notes()
        .combine(destination) { notes, destination -> notes to destination }
        .combine(layout) { (notes, destination), layout ->
            Triple(notes, destination, layout)
        }
        .combine(searchQuery) { (notes, destination, layout), searchQuery ->
            HomeUiState(
                notes = visibleNotes(notes, destination),
                destination = destination,
                layout = layout,
                searchQuery = searchQuery,
                searchResults = searchResults(notes, searchQuery)
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

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun clearSearchQuery() {
        searchQuery.value = ""
    }
}

internal fun visibleNotes(notes: List<Note>, destination: HomeDestination): List<Note> =
    when (destination) {
        HomeDestination.Notes -> notes.filterIsInstance<ActiveNote>()
        HomeDestination.Archive -> notes.filterIsInstance<ArchivedNote>()
        HomeDestination.Trash -> notes.filterIsInstance<DiscardedNote>()
    }

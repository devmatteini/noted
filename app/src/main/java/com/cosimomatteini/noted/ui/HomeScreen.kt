package com.cosimomatteini.noted.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cosimomatteini.noted.R
import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.ArchivedNote
import com.cosimomatteini.noted.domain.DiscardedNote
import com.cosimomatteini.noted.domain.Note
import com.cosimomatteini.noted.domain.NoteDescription
import com.cosimomatteini.noted.domain.NoteId
import com.cosimomatteini.noted.domain.NoteTitle
import com.cosimomatteini.noted.domain.ReminderAt
import com.cosimomatteini.noted.ui.theme.NotedTheme
import java.time.Instant
import java.util.UUID

@Composable
fun HomeRoute(
    viewModel: HomeViewModel,
    onCreateNote: () -> Unit,
    onEditNote: (ActiveNote) -> Unit,
    onOpenArchivedNote: (ArchivedNote) -> Unit,
    onOpenDiscardedNote: (DiscardedNote) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    HomeScreen(
        uiState = uiState,
        onCreateNote = onCreateNote,
        onEditNote = onEditNote,
        onOpenArchivedNote = onOpenArchivedNote,
        onOpenDiscardedNote = onOpenDiscardedNote,
        onShowNotes = viewModel::showNotes,
        onShowArchive = viewModel::showArchive,
        onShowTrash = viewModel::showTrash
    )
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onCreateNote: () -> Unit = {},
    onEditNote: (ActiveNote) -> Unit = {},
    onOpenArchivedNote: (ArchivedNote) -> Unit = {},
    onOpenDiscardedNote: (DiscardedNote) -> Unit = {},
    onShowNotes: () -> Unit = {},
    onShowArchive: () -> Unit = {},
    onShowTrash: () -> Unit = {}
) {
    Scaffold(
        floatingActionButton = {
            if (showCreateNoteAction(uiState.destination)) {
                FloatingActionButton(onClick = onCreateNote) {
                    Text(
                        text = "+",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }
        },
        bottomBar = {
            HomeNavigationBar(
                selectedDestination = uiState.destination,
                onShowNotes = onShowNotes,
                onShowArchive = onShowArchive,
                onShowTrash = onShowTrash
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (uiState.notes.isEmpty()) {
                EmptyNotes(
                    destination = uiState.destination,
                    modifier = Modifier.weight(1f)
                )
            } else {
                NotesList(
                    notes = uiState.notes,
                    onEditNote = onEditNote,
                    onOpenArchivedNote = onOpenArchivedNote,
                    onOpenDiscardedNote = onOpenDiscardedNote,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

internal fun showCreateNoteAction(destination: HomeDestination): Boolean = when (destination) {
    HomeDestination.Notes -> true
    HomeDestination.Archive -> false
    HomeDestination.Trash -> false
}

@Composable
private fun HomeNavigationBar(
    selectedDestination: HomeDestination,
    onShowNotes: () -> Unit,
    onShowArchive: () -> Unit,
    onShowTrash: () -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = selectedDestination == HomeDestination.Notes,
            onClick = onShowNotes,
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_note_stack),
                    contentDescription = null
                )
            },
            label = { Text(HomeDestination.Notes.label) }
        )
        NavigationBarItem(
            selected = selectedDestination == HomeDestination.Archive,
            onClick = onShowArchive,
            icon = {
                Icon(
                    imageVector = Icons.Filled.Archive,
                    contentDescription = null
                )
            },
            label = { Text(HomeDestination.Archive.label) }
        )
        NavigationBarItem(
            selected = selectedDestination == HomeDestination.Trash,
            onClick = onShowTrash,
            icon = {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = null
                )
            },
            label = { Text(HomeDestination.Trash.label) }
        )
    }
}

private val HomeDestination.label: String
    get() = when (this) {
        HomeDestination.Notes -> "Notes"
        HomeDestination.Archive -> "Archive"
        HomeDestination.Trash -> "Trash"
    }

@Composable
private fun EmptyNotes(destination: HomeDestination, modifier: Modifier = Modifier) {
    val title = when (destination) {
        HomeDestination.Notes -> "No notes yet"
        HomeDestination.Archive -> "No archived notes"
        HomeDestination.Trash -> "No notes in the trash"
    }
    val description = when (destination) {
        HomeDestination.Notes -> "Create a note to see it here."
        HomeDestination.Archive -> "Archive a note to see it here."
        HomeDestination.Trash -> "Discard a note to see it here."
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun NotesList(
    notes: List<Note>,
    onEditNote: (ActiveNote) -> Unit,
    onOpenArchivedNote: (ArchivedNote) -> Unit,
    onOpenDiscardedNote: (DiscardedNote) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(notes, key = { it.id.value }) { note ->
            NoteCard(
                note = note,
                onEditNote = onEditNote,
                onOpenArchivedNote = onOpenArchivedNote,
                onOpenDiscardedNote = onOpenDiscardedNote
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenEmptyPreview() {
    NotedTheme {
        HomeScreen(HomeUiState())
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenNotesPreview() {
    NotedTheme {
        HomeScreen(
            HomeUiState(
                notes = listOf(
                    ActiveNote(
                        id = NoteId(UUID.randomUUID()),
                        title = NoteTitle.of("My title"),
                        description = NoteDescription.of(
                            "My very looooooong description!!! " +
                                "This preview keeps going so the card can show truncation after " +
                                "two hundred and twenty five characters. It should end with an " +
                                "ellipsis instead of showing the full note body in the list. " +
                                "More text, more text, more text."
                        ),
                        reminderAt = ReminderAt(Instant.EPOCH),
                        createdAt = Instant.EPOCH,
                        updatedAt = Instant.EPOCH
                    ),
                    ActiveNote(
                        id = NoteId(UUID.randomUUID()),
                        title = NoteTitle.of(""),
                        description = NoteDescription.of("Check new laptop battery"),
                        createdAt = Instant.EPOCH,
                        updatedAt = Instant.EPOCH
                    )
                ),
                destination = HomeDestination.Notes
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenArchivedPreview() {
    NotedTheme {
        HomeScreen(
            HomeUiState(
                notes = listOf(
                    ArchivedNote(
                        id = NoteId(UUID.randomUUID()),
                        title = NoteTitle.of("Old note"),
                        description = NoteDescription.of("Archived content"),
                        createdAt = Instant.EPOCH,
                        updatedAt = Instant.EPOCH,
                        archivedAt = Instant.EPOCH
                    )
                ),
                destination = HomeDestination.Archive
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenTrashPreview() {
    NotedTheme {
        HomeScreen(
            HomeUiState(
                notes = listOf(
                    DiscardedNote(
                        id = NoteId(UUID.randomUUID()),
                        title = NoteTitle.of("Discarded note"),
                        description = NoteDescription.of("Trash content"),
                        createdAt = Instant.EPOCH,
                        updatedAt = Instant.EPOCH,
                        discardedAt = Instant.EPOCH
                    )
                ),
                destination = HomeDestination.Trash
            )
        )
    }
}

package com.cosimomatteini.noted.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material3.Card
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
import com.cosimomatteini.noted.ui.theme.NotedTheme
import java.time.Instant
import java.util.UUID

@Composable
fun HomeRoute(
    viewModel: HomeViewModel,
    onCreateNote: () -> Unit,
    onEditNote: (ActiveNote) -> Unit,
    onOpenArchivedNote: (ArchivedNote) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    HomeScreen(
        uiState = uiState,
        onCreateNote = onCreateNote,
        onEditNote = onEditNote,
        onOpenArchivedNote = onOpenArchivedNote,
        onShowNotes = viewModel::showNotes,
        onShowArchive = viewModel::showArchive
    )
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onCreateNote: () -> Unit = {},
    onEditNote: (ActiveNote) -> Unit = {},
    onOpenArchivedNote: (ArchivedNote) -> Unit = {},
    onShowNotes: () -> Unit = {},
    onShowArchive: () -> Unit = {}
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
                onShowArchive = onShowArchive
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
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

internal fun showCreateNoteAction(destination: HomeDestination): Boolean = when (destination) {
    HomeDestination.Notes -> true
    HomeDestination.Archive -> false
}

@Composable
private fun HomeNavigationBar(
    selectedDestination: HomeDestination,
    onShowNotes: () -> Unit,
    onShowArchive: () -> Unit
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
    }
}

private val HomeDestination.label: String
    get() = when (this) {
        HomeDestination.Notes -> "Notes"
        HomeDestination.Archive -> "Archive"
    }

@Composable
private fun EmptyNotes(destination: HomeDestination, modifier: Modifier = Modifier) {
    val title = when (destination) {
        HomeDestination.Notes -> "No notes yet"
        HomeDestination.Archive -> "No archived notes"
    }
    val description = when (destination) {
        HomeDestination.Notes -> "Create a note to see it here."
        HomeDestination.Archive -> "Archive a note to see it here."
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
                onOpenArchivedNote = onOpenArchivedNote
            )
        }
    }
}

@Composable
private fun NoteCard(
    note: Note,
    onEditNote: (ActiveNote) -> Unit,
    onOpenArchivedNote: (ArchivedNote) -> Unit
) {
    val modifier = when (note) {
        is ActiveNote ->
            Modifier
                .fillMaxWidth()
                .clickable { onEditNote(note) }

        is ArchivedNote ->
            Modifier
                .fillMaxWidth()
                .clickable { onOpenArchivedNote(note) }

        is DiscardedNote -> Modifier.fillMaxWidth()
    }

    Card(modifier) {
        Column(Modifier.padding(16.dp)) {
            if (note.title.value.isNotEmpty()) {
                Text(
                    text = note.title.value,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            if (note.description.value.isNotEmpty()) {
                Text(
                    text = note.description.value,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (note is ActiveNote && note.reminderAt != null) {
                Text(
                    text = "Reminder: ${note.reminderAt.formatReminderDateTime()}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
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
                        title = NoteTitle.of("First note"),
                        description = NoteDescription.of("Remember the milk"),
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

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.ArchivedNote
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
    onEditNote: (ActiveNote) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    HomeScreen(
        uiState = uiState,
        onCreateNote = onCreateNote,
        onEditNote = onEditNote,
        onShowActiveNotes = viewModel::showActiveNotes,
        onShowArchivedNotes = viewModel::showArchivedNotes
    )
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onCreateNote: () -> Unit = {},
    onEditNote: (ActiveNote) -> Unit = {},
    onShowActiveNotes: () -> Unit = {},
    onShowArchivedNotes: () -> Unit = {}
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateNote) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            HomeFilterRow(
                selectedFilter = uiState.filter,
                onShowActiveNotes = onShowActiveNotes,
                onShowArchivedNotes = onShowArchivedNotes
            )
            if (uiState.notes.isEmpty()) {
                EmptyNotes(
                    filter = uiState.filter,
                    modifier = Modifier.weight(1f)
                )
            } else {
                NotesList(
                    notes = uiState.notes,
                    onEditNote = onEditNote,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeFilterRow(
    selectedFilter: HomeFilter,
    onShowActiveNotes: () -> Unit,
    onShowArchivedNotes: () -> Unit
) {
    FilterChip(
        selected = selectedFilter == HomeFilter.Archived,
        onClick = {
            when (selectedFilter) {
                HomeFilter.Active -> onShowArchivedNotes()
                HomeFilter.Archived -> onShowActiveNotes()
            }
        },
        label = { Text(HomeFilter.Archived.label) },
        leadingIcon = if (selectedFilter == HomeFilter.Archived) {
            {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null
                )
            }
        } else {
            null
        },
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

private val HomeFilter.label: String
    get() = when (this) {
        HomeFilter.Active -> "Active"
        HomeFilter.Archived -> "Archived"
    }

@Composable
private fun EmptyNotes(filter: HomeFilter, modifier: Modifier = Modifier) {
    val title = when (filter) {
        HomeFilter.Active -> "No notes yet"
        HomeFilter.Archived -> "No archived notes"
    }
    val description = when (filter) {
        HomeFilter.Active -> "Create a note to see it here."
        HomeFilter.Archived -> "Archive a note to see it here."
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
                onEditNote = onEditNote
            )
        }
    }
}

@Composable
private fun NoteCard(note: Note, onEditNote: (ActiveNote) -> Unit) {
    val modifier = when (note) {
        is ActiveNote ->
            Modifier
                .fillMaxWidth()
                .clickable { onEditNote(note) }

        is ArchivedNote -> Modifier.fillMaxWidth()
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
                filter = HomeFilter.Active
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
                filter = HomeFilter.Archived
            )
        )
    }
}

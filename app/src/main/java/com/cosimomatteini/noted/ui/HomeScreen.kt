package com.cosimomatteini.noted.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.NoteDescription
import com.cosimomatteini.noted.domain.NoteId
import com.cosimomatteini.noted.domain.NoteTitle
import com.cosimomatteini.noted.ui.theme.NotedTheme
import java.time.Instant
import java.util.UUID

@Composable
fun HomeRoute(viewModel: HomeViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    HomeScreen(
        uiState = uiState,
        onCreateNote = viewModel::onCreateNote,
        onEditNote = viewModel::onEditNote,
    )
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onCreateNote: () -> Unit = {},
    onEditNote: (ActiveNote) -> Unit = {},
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateNote) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
        },
    ) { innerPadding ->
        if (uiState.activeNotes.isEmpty()) {
            EmptyNotes(Modifier.padding(innerPadding))
        } else {
            ActiveNotesList(
                activeNotes = uiState.activeNotes,
                onEditNote = onEditNote,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun EmptyNotes(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "No notes yet",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = "Create a note to see it here.",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun ActiveNotesList(
    activeNotes: List<ActiveNote>,
    onEditNote: (ActiveNote) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(activeNotes, key = { it.id.value }) { note ->
            NoteCard(
                note = note,
                onClick = { onEditNote(note) },
            )
        }
    }
}

@Composable
private fun NoteCard(
    note: ActiveNote,
    onClick: () -> Unit,
) {
    Card(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(Modifier.padding(16.dp)) {
            note.title?.let { title ->
                Text(
                    text = title.value,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Text(
                text = note.description.value,
                style = MaterialTheme.typography.bodyMedium,
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
                activeNotes = listOf(
                    ActiveNote(
                        id = NoteId(UUID.randomUUID()),
                        title = NoteTitle.of("First note"),
                        description = NoteDescription.ofUnsafe("Remember the milk"),
                        createdAt = Instant.EPOCH,
                        updatedAt = Instant.EPOCH,
                    ),
                    ActiveNote(
                        id = NoteId(UUID.randomUUID()),
                        title = null,
                        description = NoteDescription.ofUnsafe("Check new laptop battery"),
                        createdAt = Instant.EPOCH,
                        updatedAt = Instant.EPOCH,
                    ),
                ),
            ),
        )
    }
}

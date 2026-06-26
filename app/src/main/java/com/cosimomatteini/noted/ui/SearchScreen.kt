package com.cosimomatteini.noted.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
fun SearchRoute(
    viewModel: HomeViewModel,
    onBack: () -> Unit,
    onEditNote: (ActiveNote) -> Unit,
    onOpenArchivedNote: (ArchivedNote) -> Unit,
    onOpenDiscardedNote: (DiscardedNote) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    SearchScreen(
        query = uiState.searchQuery,
        results = uiState.searchResults,
        layout = uiState.layout,
        onQueryChange = viewModel::updateSearchQuery,
        onClearQuery = viewModel::clearSearchQuery,
        onBack = onBack,
        onEditNote = onEditNote,
        onOpenArchivedNote = onOpenArchivedNote,
        onOpenDiscardedNote = onOpenDiscardedNote
    )
}

@Composable
fun SearchScreen(
    query: String,
    results: SearchResults,
    layout: NotesLayout,
    onQueryChange: (String) -> Unit = {},
    onClearQuery: () -> Unit = {},
    onBack: () -> Unit = {},
    onEditNote: (ActiveNote) -> Unit = {},
    onOpenArchivedNote: (ArchivedNote) -> Unit = {},
    onOpenDiscardedNote: (DiscardedNote) -> Unit = {}
) {
    Scaffold(
        topBar = {
            SearchTopBar(
                query = query,
                onQueryChange = onQueryChange,
                onClearQuery = onClearQuery,
                onBack = onBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (query.isNotBlank() && results.isEmpty) {
                NoSearchResults(Modifier.weight(1f))
            } else {
                SearchResultsList(
                    results = results,
                    layout = layout,
                    onEditNote = onEditNote,
                    onOpenArchivedNote = onOpenArchivedNote,
                    onOpenDiscardedNote = onOpenDiscardedNote,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onBack: () -> Unit
) {
    val focusRequester = FocusRequester()

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Surface(color = MaterialTheme.colorScheme.surface) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(64.dp)
                    .padding(start = 4.dp, end = 4.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                TextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    placeholder = { Text("Search your notes") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = MaterialTheme.colorScheme.surface,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.surface
                    )
                )
                if (query.isNotEmpty()) {
                    IconButton(onClick = onClearQuery) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Clear search"
                        )
                    }
                }
            }
            HorizontalDivider()
        }
    }
}

@Composable
private fun NoSearchResults(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No notes found",
            style = MaterialTheme.typography.headlineSmall
        )
    }
}

@Composable
private fun SearchResultsList(
    results: SearchResults,
    layout: NotesLayout,
    onEditNote: (ActiveNote) -> Unit,
    onOpenArchivedNote: (ArchivedNote) -> Unit,
    onOpenDiscardedNote: (DiscardedNote) -> Unit,
    modifier: Modifier = Modifier
) {
    when (layout) {
        NotesLayout.List -> SearchResultsColumn(
            results = results,
            onEditNote = onEditNote,
            onOpenArchivedNote = onOpenArchivedNote,
            onOpenDiscardedNote = onOpenDiscardedNote,
            modifier = modifier
        )

        NotesLayout.Grid -> SearchResultsGrid(
            results = results,
            onEditNote = onEditNote,
            onOpenArchivedNote = onOpenArchivedNote,
            onOpenDiscardedNote = onOpenDiscardedNote,
            modifier = modifier
        )
    }
}

@Composable
private fun SearchResultsColumn(
    results: SearchResults,
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
        items(results.activeNotes, key = { it.id.value }) { note ->
            SearchResultCard(
                note = note,
                onEditNote = onEditNote,
                onOpenArchivedNote = onOpenArchivedNote,
                onOpenDiscardedNote = onOpenDiscardedNote,
                modifier = Modifier.fillMaxWidth()
            )
        }
        searchSection("Archive", results.archivedNotes)
        items(results.archivedNotes, key = { it.id.value }) { note ->
            SearchResultCard(
                note = note,
                onEditNote = onEditNote,
                onOpenArchivedNote = onOpenArchivedNote,
                onOpenDiscardedNote = onOpenDiscardedNote,
                modifier = Modifier.fillMaxWidth()
            )
        }
        searchSection("Trash", results.discardedNotes)
        items(results.discardedNotes, key = { it.id.value }) { note ->
            SearchResultCard(
                note = note,
                onEditNote = onEditNote,
                onOpenArchivedNote = onOpenArchivedNote,
                onOpenDiscardedNote = onOpenDiscardedNote,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun <T : Note> LazyListScope.searchSection(title: String, notes: List<T>) {
    if (notes.isNotEmpty()) {
        item {
            SearchSectionHeader(title)
        }
    }
}

@Composable
private fun SearchResultsGrid(
    results: SearchResults,
    onEditNote: (ActiveNote) -> Unit,
    onOpenArchivedNote: (ArchivedNote) -> Unit,
    onOpenDiscardedNote: (DiscardedNote) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalItemSpacing = 12.dp
    ) {
        items(results.activeNotes, key = { it.id.value }) { note ->
            SearchResultCard(
                note = note,
                onEditNote = onEditNote,
                onOpenArchivedNote = onOpenArchivedNote,
                onOpenDiscardedNote = onOpenDiscardedNote
            )
        }
        searchGridSection("Archive", results.archivedNotes)
        items(results.archivedNotes, key = { it.id.value }) { note ->
            SearchResultCard(
                note = note,
                onEditNote = onEditNote,
                onOpenArchivedNote = onOpenArchivedNote,
                onOpenDiscardedNote = onOpenDiscardedNote
            )
        }
        searchGridSection("Trash", results.discardedNotes)
        items(results.discardedNotes, key = { it.id.value }) { note ->
            SearchResultCard(
                note = note,
                onEditNote = onEditNote,
                onOpenArchivedNote = onOpenArchivedNote,
                onOpenDiscardedNote = onOpenDiscardedNote
            )
        }
    }
}

private fun <T : Note> LazyStaggeredGridScope.searchGridSection(title: String, notes: List<T>) {
    if (notes.isNotEmpty()) {
        item(span = StaggeredGridItemSpan.FullLine) {
            SearchSectionHeader(title)
        }
    }
}

@Composable
private fun SearchSectionHeader(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(top = 12.dp, start = 4.dp, end = 4.dp),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun SearchResultCard(
    note: Note,
    onEditNote: (ActiveNote) -> Unit,
    onOpenArchivedNote: (ArchivedNote) -> Unit,
    onOpenDiscardedNote: (DiscardedNote) -> Unit,
    modifier: Modifier = Modifier
) {
    NoteCard(
        note = note,
        onEditNote = onEditNote,
        onOpenArchivedNote = onOpenArchivedNote,
        onOpenDiscardedNote = onOpenDiscardedNote,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun SearchScreenPreview() {
    NotedTheme {
        SearchScreen(
            query = "test",
            results = SearchResults(
                activeNotes = listOf(previewActiveNote("Test active")),
                archivedNotes = listOf(previewArchivedNote()),
                discardedNotes = listOf(previewDiscardedNote())
            ),
            layout = NotesLayout.List
        )
    }
}

private fun previewActiveNote(title: String): ActiveNote = ActiveNote(
    id = NoteId(UUID.randomUUID()),
    title = NoteTitle.of(title),
    description = NoteDescription.of("Search result content"),
    createdAt = Instant.EPOCH,
    updatedAt = Instant.EPOCH
)

private fun previewArchivedNote(): ArchivedNote = ArchivedNote(
    id = NoteId(UUID.randomUUID()),
    title = NoteTitle.of("Archived result"),
    description = NoteDescription.of("Search result content"),
    createdAt = Instant.EPOCH,
    updatedAt = Instant.EPOCH,
    archivedAt = Instant.EPOCH
)

private fun previewDiscardedNote(): DiscardedNote = DiscardedNote(
    id = NoteId(UUID.randomUUID()),
    title = NoteTitle.of("Trash result"),
    description = NoteDescription.of("Search result content"),
    createdAt = Instant.EPOCH,
    updatedAt = Instant.EPOCH,
    discardedAt = Instant.EPOCH
)

package com.cosimomatteini.noted.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    onOpenDiscardedNote: (DiscardedNote) -> Unit,
    onOpenSearch: () -> Unit,
    onExportNotes: () -> Unit,
    onImportNotes: () -> Unit,
    notificationMessage: String?,
    onNotificationMessageShown: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    HomeScreen(
        uiState = uiState,
        onCreateNote = onCreateNote,
        onEditNote = onEditNote,
        onOpenArchivedNote = onOpenArchivedNote,
        onOpenDiscardedNote = onOpenDiscardedNote,
        onOpenSearch = onOpenSearch,
        onShowNotes = viewModel::showNotes,
        onShowArchive = viewModel::showArchive,
        onShowTrash = viewModel::showTrash,
        onToggleLayout = viewModel::toggleLayout,
        onExportNotes = onExportNotes,
        onImportNotes = onImportNotes,
        notificationMessage = notificationMessage,
        onNotificationMessageShown = onNotificationMessageShown
    )
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onCreateNote: () -> Unit = {},
    onEditNote: (ActiveNote) -> Unit = {},
    onOpenArchivedNote: (ArchivedNote) -> Unit = {},
    onOpenDiscardedNote: (DiscardedNote) -> Unit = {},
    onOpenSearch: () -> Unit = {},
    onShowNotes: () -> Unit = {},
    onShowArchive: () -> Unit = {},
    onShowTrash: () -> Unit = {},
    onToggleLayout: () -> Unit = {},
    onExportNotes: () -> Unit = {},
    onImportNotes: () -> Unit = {},
    notificationMessage: String? = null,
    onNotificationMessageShown: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(notificationMessage) {
        val currentMessage = notificationMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(currentMessage)
        onNotificationMessageShown()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            HomeTopBar(
                destination = uiState.destination,
                layout = uiState.layout,
                onOpenSearch = onOpenSearch,
                onToggleLayout = onToggleLayout,
                onExportNotes = onExportNotes,
                onImportNotes = onImportNotes
            )
        },
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
                    layout = uiState.layout,
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
private fun HomeTopBar(
    destination: HomeDestination,
    layout: NotesLayout,
    onOpenSearch: () -> Unit,
    onToggleLayout: () -> Unit,
    onExportNotes: () -> Unit,
    onImportNotes: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Surface(color = MaterialTheme.colorScheme.surface) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(64.dp)
                .padding(start = 24.dp, end = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                text = destination.label,
                style = MaterialTheme.typography.headlineSmall
            )
            Row {
                IconButton(onClick = onOpenSearch) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search notes"
                    )
                }
                IconButton(onClick = onToggleLayout) {
                    Icon(
                        imageVector = layout.toggleIcon,
                        contentDescription = layout.toggleContentDescription
                    )
                }
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = "Open more actions"
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Export") },
                        onClick = {
                            menuExpanded = false
                            onExportNotes()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Import") },
                        onClick = {
                            menuExpanded = false
                            onImportNotes()
                        }
                    )
                }
            }
        }
    }
}

private val NotesLayout.toggleIcon
    get() = when (this) {
        NotesLayout.List -> Icons.Outlined.GridView
        NotesLayout.Grid -> Icons.Outlined.ViewAgenda
    }

private val NotesLayout.toggleContentDescription: String
    get() = when (this) {
        NotesLayout.List -> "Show notes in grid"
        NotesLayout.Grid -> "Show notes in list"
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
        HomeNavItem(
            selected = selectedDestination == HomeDestination.Notes,
            onClick = onShowNotes,
            label = HomeDestination.Notes.label,
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_note_stack),
                    contentDescription = null
                )
            }
        )
        HomeNavItem(
            selected = selectedDestination == HomeDestination.Archive,
            onClick = onShowArchive,
            label = HomeDestination.Archive.label,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Archive,
                    contentDescription = null
                )
            }
        )
        HomeNavItem(
            selected = selectedDestination == HomeDestination.Trash,
            onClick = onShowTrash,
            label = HomeDestination.Trash.label,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null
                )
            }
        )
    }
}

@Composable
private fun RowScope.HomeNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    icon: @Composable () -> Unit
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = icon,
        label = { Text(label) }
    )
}

private val HomeDestination.label: String
    get() = when (this) {
        HomeDestination.Notes -> "Notes"
        HomeDestination.Archive -> "Archive"
        HomeDestination.Trash -> "Trash"
    }

@Composable
private fun EmptyNotes(destination: HomeDestination, modifier: Modifier = Modifier) {
    val (title, description) = when (destination) {
        HomeDestination.Notes -> "No notes yet" to "Create a note to see it here."
        HomeDestination.Archive -> "No archived notes" to "Archive a note to see it here."
        HomeDestination.Trash -> "No notes in the trash" to "Discard a note to see it here."
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
    layout: NotesLayout,
    onEditNote: (ActiveNote) -> Unit,
    onOpenArchivedNote: (ArchivedNote) -> Unit,
    onOpenDiscardedNote: (DiscardedNote) -> Unit,
    modifier: Modifier = Modifier
) {
    when (layout) {
        NotesLayout.List -> NotesColumn(
            notes = notes,
            onEditNote = onEditNote,
            onOpenArchivedNote = onOpenArchivedNote,
            onOpenDiscardedNote = onOpenDiscardedNote,
            modifier = modifier
        )

        NotesLayout.Grid -> NotesGrid(
            notes = notes,
            onEditNote = onEditNote,
            onOpenArchivedNote = onOpenArchivedNote,
            onOpenDiscardedNote = onOpenDiscardedNote,
            modifier = modifier
        )
    }
}

@Composable
private fun NotesColumn(
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
                onOpenDiscardedNote = onOpenDiscardedNote,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun NotesGrid(
    notes: List<Note>,
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

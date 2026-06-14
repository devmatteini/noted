package com.cosimomatteini.noted.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.tooling.preview.Preview
import com.cosimomatteini.noted.domain.ArchivedNote
import com.cosimomatteini.noted.domain.NoteDescription
import com.cosimomatteini.noted.domain.NoteId
import com.cosimomatteini.noted.domain.NoteTitle
import com.cosimomatteini.noted.ui.theme.NotedTheme
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.launch

@Composable
internal fun ArchivedNoteDetailsRoute(
    note: ArchivedNote,
    onBack: () -> Unit,
    onRestore: suspend () -> Unit,
    onDelete: suspend () -> Unit
) {
    ArchivedNoteDetailsScreen(
        title = note.title.value,
        description = note.description.value,
        onBack = onBack,
        onRestore = onRestore,
        onDelete = onDelete
    )
}

@Composable
fun ArchivedNoteDetailsScreen(
    title: String,
    description: String,
    onBack: () -> Unit,
    onRestore: suspend () -> Unit,
    onDelete: suspend () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    ReadOnlyNoteDetailsScreen(
        title = title,
        description = description,
        onBack = onBack
    ) {
        NoteActionIcon(
            imageVector = Icons.Filled.Unarchive,
            contentDescription = "Restore note",
            onClick = {
                coroutineScope.launch {
                    onRestore()
                }
            }
        )
        NoteActionIcon(
            imageVector = Icons.Filled.Delete,
            contentDescription = "Delete note",
            onClick = {
                coroutineScope.launch {
                    onDelete()
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ArchivedNoteDetailsScreenPreview() {
    NotedTheme {
        ArchivedNoteDetailsRoute(
            note = ArchivedNote(
                id = NoteId(UUID.randomUUID()),
                title = NoteTitle.of("Archived note"),
                description = NoteDescription.of("Read-only content"),
                createdAt = Instant.EPOCH,
                updatedAt = Instant.EPOCH,
                archivedAt = Instant.EPOCH
            ),
            onBack = {},
            onRestore = {},
            onDelete = {}
        )
    }
}

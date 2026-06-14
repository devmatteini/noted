package com.cosimomatteini.noted.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.History
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.tooling.preview.Preview
import com.cosimomatteini.noted.domain.DiscardedNote
import com.cosimomatteini.noted.domain.NoteDescription
import com.cosimomatteini.noted.domain.NoteId
import com.cosimomatteini.noted.domain.NoteTitle
import com.cosimomatteini.noted.ui.theme.NotedTheme
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.launch

@Composable
internal fun DiscardedNoteDetailsRoute(
    note: DiscardedNote,
    onBack: () -> Unit,
    onRestore: suspend () -> Unit,
    onPermanentlyDelete: suspend () -> Unit
) {
    DiscardedNoteDetailsScreen(
        title = note.title.value,
        description = note.description.value,
        onBack = onBack,
        onRestore = onRestore,
        onPermanentlyDelete = onPermanentlyDelete
    )
}

@Composable
fun DiscardedNoteDetailsScreen(
    title: String,
    description: String,
    onBack: () -> Unit,
    onRestore: suspend () -> Unit,
    onPermanentlyDelete: suspend () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    ReadOnlyNoteDetailsScreen(
        title = title,
        description = description,
        onBack = onBack
    ) {
        NoteActionIcon(
            imageVector = Icons.Filled.History,
            contentDescription = "Restore note",
            onClick = {
                coroutineScope.launch {
                    onRestore()
                }
            }
        )
        NoteActionIcon(
            imageVector = Icons.Filled.DeleteForever,
            contentDescription = "Permanently delete note",
            onClick = {
                coroutineScope.launch {
                    onPermanentlyDelete()
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DiscardedNoteDetailsScreenPreview() {
    NotedTheme {
        DiscardedNoteDetailsRoute(
            note = DiscardedNote(
                id = NoteId(UUID.randomUUID()),
                title = NoteTitle.of("Discarded note"),
                description = NoteDescription.of("Read-only content"),
                createdAt = Instant.EPOCH,
                updatedAt = Instant.EPOCH,
                discardedAt = Instant.EPOCH
            ),
            onBack = {},
            onRestore = {},
            onPermanentlyDelete = {}
        )
    }
}

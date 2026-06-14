package com.cosimomatteini.noted.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.ArchivedNote
import com.cosimomatteini.noted.domain.DiscardedNote
import com.cosimomatteini.noted.domain.Note

private const val DESCRIPTION_MAX_CHARS = 225

@Composable
internal fun NoteCard(
    note: Note,
    onEditNote: (ActiveNote) -> Unit,
    onOpenArchivedNote: (ArchivedNote) -> Unit,
    onOpenDiscardedNote: (DiscardedNote) -> Unit
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

        is DiscardedNote ->
            Modifier
                .fillMaxWidth()
                .clickable { onOpenDiscardedNote(note) }
    }

    OutlinedCard(modifier) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (note.title.value.isNotEmpty()) {
                Text(
                    text = note.title.value,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            if (note.description.value.isNotEmpty()) {
                Text(
                    text = note.description.value.ellipsizeNoteCardDescription(),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            if (note is ActiveNote && note.reminderAt != null) {
                NoteReminderChip(
                    text = note.reminderAt.formatReminderChipDateTime(),
                    onClick = { onEditNote(note) }
                )
            }
        }
    }
}

private fun String.ellipsizeNoteCardDescription(): String = if (length > DESCRIPTION_MAX_CHARS) {
    take(DESCRIPTION_MAX_CHARS).trimEnd() + "..."
} else {
    this
}

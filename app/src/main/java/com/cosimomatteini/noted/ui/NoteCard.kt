package com.cosimomatteini.noted.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
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
import com.cosimomatteini.noted.ui.theme.PinOrange

private const val DESCRIPTION_MAX_CHARS = 225

@Composable
internal fun NoteCard(
    note: Note,
    onEditNote: (ActiveNote) -> Unit,
    onOpenArchivedNote: (ArchivedNote) -> Unit,
    onOpenDiscardedNote: (DiscardedNote) -> Unit,
    modifier: Modifier = Modifier
) {
    val cardModifier = when (note) {
        is ActiveNote ->
            modifier.clickable { onEditNote(note) }

        is ArchivedNote ->
            modifier.clickable { onOpenArchivedNote(note) }

        is DiscardedNote ->
            modifier.clickable { onOpenDiscardedNote(note) }
    }

    val border = if (note is ActiveNote && note.isPinned) {
        BorderStroke(1.dp, PinOrange)
    } else {
        CardDefaults.outlinedCardBorder()
    }

    OutlinedCard(
        modifier = cardModifier,
        border = border
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (note.title.value.isNotEmpty()) {
                Text(
                    text = note.title.value,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            if (note.description.value.isNotEmpty()) {
                Text(
                    text = note.description.value.ellipsizeNoteCardDescription(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (note is ActiveNote && note.reminderAt != null) {
                NoteReminderChip(
                    reminderAt = note.reminderAt,
                    onClick = { onEditNote(note) },
                    compact = true
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

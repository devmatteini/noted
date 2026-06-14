package com.cosimomatteini.noted.domain

import java.time.Instant

data class ArchivedNote(
    override val id: NoteId,
    override val title: NoteTitle,
    override val description: NoteDescription,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    val archivedAt: Instant
) : Note {
    fun restore(restoredAt: Instant): ActiveNote = ActiveNote(
        id = id,
        title = title,
        description = description,
        reminderAt = null,
        createdAt = createdAt,
        updatedAt = restoredAt
    )
}

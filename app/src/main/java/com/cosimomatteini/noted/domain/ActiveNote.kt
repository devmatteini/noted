package com.cosimomatteini.noted.domain

import java.time.Instant
import java.util.UUID

data class ActiveNote(
    override val id: NoteId,
    override val title: NoteTitle,
    override val description: NoteDescription,
    override val createdAt: Instant,
    override val updatedAt: Instant,
) : Note {
    companion object {
        fun create(
            title: String,
            description: String,
            clock: Clock,
            id: NoteId = NoteId(UUID.randomUUID()),
        ): ActiveNote {
            val now = clock.now()
            val noteTitle = NoteTitle.parse(title)
            val noteDescription = NoteDescription.parse(description)

            return ActiveNote(
                id = id,
                title = noteTitle,
                description = noteDescription,
                createdAt = now,
                updatedAt = now,
            )
        }
    }

    fun update(
        title: String,
        description: String,
        updatedAt: Instant,
    ): ActiveNote =
        copy(
            title = NoteTitle.parse(title),
            description = NoteDescription.parse(description),
            updatedAt = updatedAt,
        )
}

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
        fun empty(
            clock: Clock,
            id: NoteId = NoteId(UUID.randomUUID()),
        ): ActiveNote {
            val now = clock.now()

            return ActiveNote(
                id = id,
                title = NoteTitle.of(""),
                description = NoteDescription.of(""),
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

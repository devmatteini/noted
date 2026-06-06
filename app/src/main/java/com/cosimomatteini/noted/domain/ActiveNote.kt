package com.cosimomatteini.noted.domain

import java.time.Instant
import java.util.UUID

data class ActiveNote(
    override val id: NoteId,
    override val title: NoteTitle?,
    override val description: NoteDescription,
    override val createdAt: Instant,
    override val updatedAt: Instant,
) : Note {
    companion object {
        fun create(
            title: String?,
            description: String,
            clock: Clock,
            id: NoteId = NoteId(UUID.randomUUID()),
        ): Result<ActiveNote> {
            val now = clock.now()
            val noteTitle = NoteTitle.parse(title)
            val noteDescription = NoteDescription.parse(description)
                .getOrElse { return Result.failure(it) }

            return Result.success(
                ActiveNote(
                    id = id,
                    title = noteTitle,
                    description = noteDescription,
                    createdAt = now,
                    updatedAt = now,
                ),
            )
        }
    }

    fun update(
        title: String?,
        description: String,
        updatedAt: Instant,
    ): Result<ActiveNote> {
        val noteDescription = NoteDescription.parse(description)
            .getOrElse { return Result.failure(it) }

        return Result.success(
            copy(
                title = NoteTitle.parse(title),
                description = noteDescription,
                updatedAt = updatedAt,
            ),
        )
    }
}

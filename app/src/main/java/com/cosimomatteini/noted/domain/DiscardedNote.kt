package com.cosimomatteini.noted.domain

import java.time.Instant

data class DiscardedNote(
    override val id: NoteId,
    override val title: NoteTitle,
    override val description: NoteDescription,
    override val createdAt: Instant,
    override val updatedAt: Instant,
    val discardedAt: Instant
) : Note

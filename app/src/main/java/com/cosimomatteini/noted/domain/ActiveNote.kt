package com.cosimomatteini.noted.domain

import java.time.Instant

data class ActiveNote(
    override val id: NoteId,
    override val title: NoteTitle?,
    override val description: NoteDescription,
    override val createdAt: Instant,
    override val updatedAt: Instant,
) : Note

package com.cosimomatteini.noted.domain

import java.time.Instant

sealed interface Note {
    val id: NoteId
    val title: NoteTitle?
    val description: NoteDescription
    val createdAt: Instant
    val updatedAt: Instant
}

package com.cosimomatteini.noted.features

import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.Clock
import com.cosimomatteini.noted.domain.NoteId
import com.cosimomatteini.noted.domain.NoteRepository

class UpdateNote(private val noteRepository: NoteRepository, private val clock: Clock) {
    suspend operator fun invoke(
        id: NoteId,
        title: String,
        description: String
    ): Result<ActiveNote> {
        val note = noteRepository.loadActive(id)
            ?: return Result.failure(IllegalArgumentException("Active note not found."))
        val updatedNote = note.update(
            title = title,
            description = description,
            updatedAt = clock.now()
        )

        noteRepository.save(updatedNote)
        return Result.success(updatedNote)
    }
}

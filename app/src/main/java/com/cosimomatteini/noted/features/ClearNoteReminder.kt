package com.cosimomatteini.noted.features

import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.Clock
import com.cosimomatteini.noted.domain.NoteId
import com.cosimomatteini.noted.domain.NoteRepository

class ClearNoteReminder(private val noteRepository: NoteRepository, private val clock: Clock) {
    suspend operator fun invoke(id: NoteId): Result<ActiveNote> {
        val note = noteRepository.load(id)
            ?: return Result.failure(IllegalArgumentException("Note not found."))
        val updatedNote = note.clearReminder(clock.now())

        noteRepository.save(updatedNote)
        return Result.success(updatedNote)
    }
}

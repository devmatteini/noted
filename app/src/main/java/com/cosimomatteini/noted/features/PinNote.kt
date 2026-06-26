package com.cosimomatteini.noted.features

import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.Clock
import com.cosimomatteini.noted.domain.NoteId
import com.cosimomatteini.noted.domain.NoteRepository

class PinNote(private val noteRepository: NoteRepository, private val clock: Clock) {
    suspend operator fun invoke(id: NoteId): Result<ActiveNote> {
        val note = noteRepository.loadActive(id)
            ?: return Result.failure(IllegalArgumentException("Active note not found."))
        val pinnedNote = note.pin(clock.now())

        noteRepository.save(pinnedNote)
        return Result.success(pinnedNote)
    }
}

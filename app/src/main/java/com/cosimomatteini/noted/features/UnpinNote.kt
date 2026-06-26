package com.cosimomatteini.noted.features

import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.Clock
import com.cosimomatteini.noted.domain.NoteId
import com.cosimomatteini.noted.domain.NoteRepository

class UnpinNote(private val noteRepository: NoteRepository, private val clock: Clock) {
    suspend operator fun invoke(id: NoteId): Result<ActiveNote> {
        val note = noteRepository.loadActive(id)
            ?: return Result.failure(IllegalArgumentException("Active note not found."))
        val unpinnedNote = note.unpin(clock.now())

        noteRepository.save(unpinnedNote)
        return Result.success(unpinnedNote)
    }
}

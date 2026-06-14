package com.cosimomatteini.noted.features

import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.Clock
import com.cosimomatteini.noted.domain.NoteId
import com.cosimomatteini.noted.domain.NoteRepository

class UnarchiveNote(private val noteRepository: NoteRepository, private val clock: Clock) {
    suspend operator fun invoke(id: NoteId): Result<ActiveNote> {
        val note = noteRepository.loadArchived(id)
            ?: return Result.failure(IllegalArgumentException("Archived note not found."))
        val restoredNote = note.restore(clock.now())

        noteRepository.save(restoredNote)
        return Result.success(restoredNote)
    }
}

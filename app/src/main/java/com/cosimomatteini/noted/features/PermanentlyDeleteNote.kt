package com.cosimomatteini.noted.features

import com.cosimomatteini.noted.domain.NoteId
import com.cosimomatteini.noted.domain.NoteRepository

class PermanentlyDeleteNote(private val noteRepository: NoteRepository) {
    suspend operator fun invoke(id: NoteId): Result<Unit> {
        noteRepository.loadDiscarded(id)
            ?: return Result.failure(IllegalArgumentException("Discarded note not found."))

        noteRepository.delete(id)
        return Result.success(Unit)
    }
}

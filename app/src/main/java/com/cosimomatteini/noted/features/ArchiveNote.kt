package com.cosimomatteini.noted.features

import com.cosimomatteini.noted.domain.ArchivedNote
import com.cosimomatteini.noted.domain.Clock
import com.cosimomatteini.noted.domain.NoteId
import com.cosimomatteini.noted.domain.NoteRepository

class ArchiveNote(
    private val noteRepository: NoteRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(id: NoteId): Result<ArchivedNote> {
        val note = noteRepository.load(id)
            ?: return Result.failure(IllegalArgumentException("Note not found."))
        val archivedNote = note.archive(clock.now())

        noteRepository.save(archivedNote)
        return Result.success(archivedNote)
    }
}

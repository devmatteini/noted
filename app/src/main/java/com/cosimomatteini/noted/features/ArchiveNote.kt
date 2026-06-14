package com.cosimomatteini.noted.features

import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.ArchivedNote
import com.cosimomatteini.noted.domain.Clock
import com.cosimomatteini.noted.domain.NoteId
import com.cosimomatteini.noted.domain.NoteRepository
import com.cosimomatteini.noted.domain.ReminderScheduler

class ArchiveNote(
    private val noteRepository: NoteRepository,
    private val reminderScheduler: ReminderScheduler,
    private val clock: Clock
) {
    suspend operator fun invoke(id: NoteId): Result<ArchivedNote> {
        val note = noteRepository.load(id) as? ActiveNote
            ?: return Result.failure(IllegalArgumentException("Active note not found."))
        val archivedNote = note.archive(clock.now())

        noteRepository.save(archivedNote)
        reminderScheduler.cancel(id)
        return Result.success(archivedNote)
    }
}

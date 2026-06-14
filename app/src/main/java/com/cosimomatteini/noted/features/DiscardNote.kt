package com.cosimomatteini.noted.features

import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.ArchivedNote
import com.cosimomatteini.noted.domain.Clock
import com.cosimomatteini.noted.domain.DiscardedNote
import com.cosimomatteini.noted.domain.NoteId
import com.cosimomatteini.noted.domain.NoteRepository
import com.cosimomatteini.noted.domain.ReminderScheduler

class DiscardNote(
    private val noteRepository: NoteRepository,
    private val reminderScheduler: ReminderScheduler,
    private val clock: Clock
) {
    suspend operator fun invoke(id: NoteId): Result<DiscardedNote> {
        val note = noteRepository.load(id)
            ?: return Result.failure(IllegalArgumentException("Note not found."))
        val discardedNote = when (note) {
            is ActiveNote -> note.discard(clock.now())
            is ArchivedNote -> note.discard(clock.now())
            is DiscardedNote -> note
        }

        noteRepository.save(discardedNote)
        if (note is ActiveNote) {
            reminderScheduler.cancel(id)
        }
        return Result.success(discardedNote)
    }
}

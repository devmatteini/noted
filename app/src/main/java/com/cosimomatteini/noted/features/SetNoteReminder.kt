package com.cosimomatteini.noted.features

import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.Clock
import com.cosimomatteini.noted.domain.NoteId
import com.cosimomatteini.noted.domain.NoteRepository
import com.cosimomatteini.noted.domain.ReminderAt
import com.cosimomatteini.noted.domain.ReminderScheduler

class SetNoteReminder(
    private val noteRepository: NoteRepository,
    private val reminderScheduler: ReminderScheduler,
    private val clock: Clock
) {
    suspend operator fun invoke(id: NoteId, reminderAt: ReminderAt): Result<ActiveNote> {
        val note = noteRepository.load(id)
            ?: return Result.failure(IllegalArgumentException("Note not found."))
        val now = clock.now()
        val updatedNote = note.setReminder(reminderAt, now)

        noteRepository.save(updatedNote)
        reminderScheduler.cancel(id)
        if (reminderAt.value > now) {
            reminderScheduler.schedule(id, reminderAt)
        }
        return Result.success(updatedNote)
    }
}

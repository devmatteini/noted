package com.cosimomatteini.noted.features

import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.Clock
import com.cosimomatteini.noted.domain.NoteId
import com.cosimomatteini.noted.domain.NoteRepository
import com.cosimomatteini.noted.domain.ReminderAt

class SetNoteReminder(private val noteRepository: NoteRepository, private val clock: Clock) {
    suspend operator fun invoke(id: NoteId, reminderAt: ReminderAt): Result<ActiveNote> {
        val note = noteRepository.load(id)
            ?: return Result.failure(IllegalArgumentException("Note not found."))
        val updatedNote = note.setReminder(reminderAt, clock.now())

        noteRepository.save(updatedNote)
        return Result.success(updatedNote)
    }
}

package com.cosimomatteini.noted.features

import com.cosimomatteini.noted.domain.NoteId
import com.cosimomatteini.noted.domain.NoteRepository
import com.cosimomatteini.noted.domain.ReminderScheduler

class DeleteNote(
    private val noteRepository: NoteRepository,
    private val reminderScheduler: ReminderScheduler
) {
    suspend operator fun invoke(id: NoteId) {
        noteRepository.delete(id)
        reminderScheduler.cancel(id)
    }
}

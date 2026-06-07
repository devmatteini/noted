package com.cosimomatteini.noted.features

import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.Clock
import com.cosimomatteini.noted.domain.NoteRepository
import com.cosimomatteini.noted.domain.ReminderScheduler
import kotlinx.coroutines.flow.first

class RestoreReminders(
    private val noteRepository: NoteRepository,
    private val reminderScheduler: ReminderScheduler,
    private val clock: Clock
) {
    suspend operator fun invoke() {
        noteRepository.observe().first()
            .filterIsInstance<ActiveNote>()
            .forEach { note ->
                val reminderAt = note.reminderAt ?: return@forEach
                if (reminderAt.value > clock.now()) {
                    reminderScheduler.schedule(note.id, reminderAt)
                }
            }
    }
}

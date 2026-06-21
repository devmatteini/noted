package com.cosimomatteini.noted.features

import com.cosimomatteini.noted.domain.Clock
import com.cosimomatteini.noted.domain.NoteRepository
import java.time.temporal.ChronoUnit

class DeleteExpiredDiscardedNotes(
    private val noteRepository: NoteRepository,
    private val clock: Clock,
    private val retentionDays: Long = 30L
) {
    suspend operator fun invoke() {
        noteRepository.deleteDiscardedBefore(clock.now().minus(retentionDays, ChronoUnit.DAYS))
    }
}

package com.cosimomatteini.noted.features

import com.cosimomatteini.noted.domain.ActiveNote
import com.cosimomatteini.noted.domain.Clock
import com.cosimomatteini.noted.domain.NoteRepository

class CreateNote(
    private val noteRepository: NoteRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(
        title: String?,
        description: String,
    ): Result<ActiveNote> {
        val note = ActiveNote.create(
            title = title,
            description = description,
            clock = clock,
        ).getOrElse { return Result.failure(it) }

        noteRepository.save(note)
        return Result.success(note)
    }
}

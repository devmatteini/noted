package com.cosimomatteini.noted.features

import com.cosimomatteini.noted.domain.Clock
import com.cosimomatteini.noted.domain.NoteRepository
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class ExportNotes(
    private val noteRepository: NoteRepository,
    private val clock: Clock,
    private val backupJsonCodec: BackupJsonCodec = BackupJsonCodec()
) {
    suspend operator fun invoke(): ExportedNotes {
        val exportedAt = clock.now()
        return ExportedNotes(
            filename = backupFileName(exportedAt),
            json = backupJsonCodec.encode(
                notes = noteRepository.loadAll(),
                exportedAt = exportedAt
            )
        )
    }
}

data class ExportedNotes(val filename: String, val json: String)

fun backupFileName(exportedAt: Instant): String = "noted-backup-${fileDate.format(exportedAt)}.json"

private val fileDate: DateTimeFormatter = DateTimeFormatter
    .ofPattern("yyyy-MM-dd")
    .withZone(ZoneOffset.UTC)

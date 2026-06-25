package com.cosimomatteini.noted.features

import com.cosimomatteini.noted.domain.Note

class ParseNotesBackupFile(private val backupJsonCodec: BackupJsonCodec = BackupJsonCodec()) {
    operator fun invoke(content: String): Result<List<Note>> = backupJsonCodec.decode(content)
}

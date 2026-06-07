package com.cosimomatteini.noted.ui

import com.cosimomatteini.noted.domain.NoteId
import com.cosimomatteini.noted.domain.ReminderAt

internal data class SaveReminderRequest(val noteId: NoteId, val reminderAt: ReminderAt)

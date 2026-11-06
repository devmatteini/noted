package com.cosimomatteini.noted.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class ShareNoteTest {
    @Test
    fun formatNoteShareText_includesTitleAndDescription() {
        assertEquals("Title\nDescription", formatNoteShareText("Title", "Description"))
    }

    @Test
    fun formatNoteShareText_omitsMissingTitle() {
        assertEquals("Description", formatNoteShareText("", "Description"))
    }

    @Test
    fun formatNoteShareText_omitsMissingDescription() {
        assertEquals("Title", formatNoteShareText("Title", ""))
    }

    @Test
    fun formatNoteShareText_returnsEmptyTextWhenNoteIsEmpty() {
        assertEquals("", formatNoteShareText("", ""))
    }
}

package com.cosimomatteini.noted.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteDescriptionTest {
    @Test
    fun parse_trimsDescription() {
        val description = NoteDescription.parse("  Buy coffee  ").getOrThrow()

        assertEquals("Buy coffee", description.value)
    }

    @Test
    fun parse_rejectsEmptyDescription() {
        val result = NoteDescription.parse("")

        assertTrue(result.isFailure)
    }

    @Test
    fun parse_rejectsBlankDescription() {
        val result = NoteDescription.parse("   ")

        assertTrue(result.isFailure)
    }
}

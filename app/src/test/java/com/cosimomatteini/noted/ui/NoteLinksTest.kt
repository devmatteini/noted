package com.cosimomatteini.noted.ui

import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.style.TextDecoration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteLinksTest {
    @Test
    fun findNoteUrlRanges_detectsHttpAndHttpsUrls() {
        val ranges = findNoteUrlRanges("Open http://a.test and https://b.test/path")

        assertEquals(
            listOf(
                NoteUrlRange(start = 5, end = 18, url = "http://a.test"),
                NoteUrlRange(start = 23, end = 42, url = "https://b.test/path")
            ),
            ranges
        )
    }

    @Test
    fun findNoteUrlRanges_ignoresBareDomains() {
        val ranges = findNoteUrlRanges("Open example.com or www.example.com")

        assertTrue(ranges.isEmpty())
    }

    @Test
    fun findNoteUrlRanges_trimsTrailingPunctuation() {
        val ranges = findNoteUrlRanges("Open (https://example.com).")

        assertEquals(
            listOf(NoteUrlRange(start = 6, end = 25, url = "https://example.com")),
            ranges
        )
    }

    @Test
    fun savedNoteDescriptionAnnotatedString_addsUrlAnnotations() {
        val text = savedNoteDescriptionAnnotatedString("Open https://example.com")

        val annotations = text.getLinkAnnotations(
            start = 5,
            end = 5
        )

        assertEquals("Open https://example.com", text.text)
        assertEquals(
            "https://example.com",
            (annotations.single().item as LinkAnnotation.Url).url
        )
    }

    @Test
    fun savedNoteDescriptionAnnotatedString_stylesUrlRanges() {
        val text = savedNoteDescriptionAnnotatedString("Open https://example.com")

        val style = text.spanStyles.single()

        assertEquals(5, style.start)
        assertEquals(24, style.end)
        assertEquals(TextDecoration.Underline, style.item.textDecoration)
    }
}

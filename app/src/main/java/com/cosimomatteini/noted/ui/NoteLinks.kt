package com.cosimomatteini.noted.ui

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration

private val noteUrlRegex = Regex("""\bhttps?://\S+""")
private val trailingUrlPunctuation = setOf('.', ',', '!', '?', ';', ':', ')', ']', '}')

internal data class NoteUrlRange(val start: Int, val end: Int, val url: String)

private val noteUrlSpanStyle = SpanStyle(
    textDecoration = TextDecoration.Underline
)

internal fun findNoteUrlRanges(text: String): List<NoteUrlRange> = noteUrlRegex.findAll(text)
    .mapNotNull { match ->
        val start = match.range.first
        var end = match.range.last + 1
        while (end > start && text[end - 1] in trailingUrlPunctuation) {
            end -= 1
        }
        if (end == start) {
            null
        } else {
            NoteUrlRange(
                start = start,
                end = end,
                url = text.substring(start, end)
            )
        }
    }
    .toList()

internal fun savedNoteDescriptionAnnotatedString(description: String): AnnotatedString =
    buildAnnotatedString {
        append(description)
        findNoteUrlRanges(description).forEach { range ->
            addStyle(
                style = noteUrlSpanStyle,
                start = range.start,
                end = range.end
            )
            addLink(
                url = LinkAnnotation.Url(range.url),
                start = range.start,
                end = range.end
            )
        }
    }

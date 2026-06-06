package com.cosimomatteini.noted.domain

@JvmInline
value class NoteDescription private constructor(val value: String) {
    companion object {
        fun ofUnsafe(value: String): NoteDescription = parse(value).getOrThrow()

        fun parse(value: String): Result<NoteDescription> {
            val trimmed = value.trim()
            if (trimmed.isEmpty()) {
                return Result.failure(IllegalArgumentException("Note description cannot be empty."))
            }
            return Result.success(NoteDescription(trimmed))
        }
    }
}

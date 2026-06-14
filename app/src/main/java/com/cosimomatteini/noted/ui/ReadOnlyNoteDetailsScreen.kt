package com.cosimomatteini.noted.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun ReadOnlyNoteDetailsScreen(
    title: String,
    description: String,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit
) {
    BackHandler(onBack = onBack)

    NoteDetailsScaffold(onBack = onBack) { innerPadding ->
        NoteDetailsContentColumn(innerPadding = innerPadding) {
            if (title.isNotEmpty()) {
                Text(
                    text = title,
                    style = noteTitleTextStyle(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (description.isNotEmpty()) {
                Text(
                    text = description,
                    style = noteDescriptionTextStyle(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            } else {
                Spacer(Modifier.weight(1f))
            }
            NoteActionsRow(content = actions)
        }
    }
}

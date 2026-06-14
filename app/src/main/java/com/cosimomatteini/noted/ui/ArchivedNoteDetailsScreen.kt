package com.cosimomatteini.noted.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cosimomatteini.noted.domain.ArchivedNote
import com.cosimomatteini.noted.domain.NoteDescription
import com.cosimomatteini.noted.domain.NoteId
import com.cosimomatteini.noted.domain.NoteTitle
import com.cosimomatteini.noted.ui.theme.NotedTheme
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.launch

@Composable
internal fun ArchivedNoteDetailsRoute(
    note: ArchivedNote,
    onBack: () -> Unit,
    onUnarchive: suspend () -> Unit,
    onDelete: suspend () -> Unit
) {
    ArchivedNoteDetailsScreen(
        title = note.title.value,
        description = note.description.value,
        onBack = onBack,
        onUnarchive = onUnarchive,
        onDelete = onDelete
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchivedNoteDetailsScreen(
    title: String,
    description: String,
    onBack: () -> Unit,
    onUnarchive: suspend () -> Unit,
    onDelete: suspend () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    BackHandler(onBack = onBack)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (title.isNotEmpty()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (description.isNotEmpty()) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            } else {
                Spacer(Modifier.weight(1f))
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            onUnarchive()
                        }
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Unarchive,
                        contentDescription = "Unarchive note"
                    )
                }
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            onDelete()
                        }
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete note"
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ArchivedNoteDetailsScreenPreview() {
    NotedTheme {
        ArchivedNoteDetailsRoute(
            note = ArchivedNote(
                id = NoteId(UUID.randomUUID()),
                title = NoteTitle.of("Archived note"),
                description = NoteDescription.of("Read-only content"),
                createdAt = Instant.EPOCH,
                updatedAt = Instant.EPOCH,
                archivedAt = Instant.EPOCH
            ),
            onBack = {},
            onUnarchive = {},
            onDelete = {}
        )
    }
}

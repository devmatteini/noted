package com.cosimomatteini.noted

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.cosimomatteini.noted.ui.theme.NotedTheme

class MainActivity : ComponentActivity() {
    private lateinit var appContainer: NotedAppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContainer = NotedAppContainer()
        enableEdgeToEdge()
        setContent {
            NotedTheme {
                NotedApp(appContainer)
            }
        }
    }
}

@Composable
@Suppress("UNUSED_PARAMETER")
fun NotedApp(appContainer: NotedAppContainer) = Unit

@Preview(showBackground = true)
@Composable
fun NotedAppPreview() {
    NotedTheme {
        NotedApp(NotedAppContainer())
    }
}

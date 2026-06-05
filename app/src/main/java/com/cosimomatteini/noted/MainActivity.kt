package com.cosimomatteini.noted

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.cosimomatteini.noted.ui.theme.NotedTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NotedTheme {
                NotedApp()
            }
        }
    }
}

@Composable
fun NotedApp() = Unit

@Preview(showBackground = true)
@Composable
fun NotedAppPreview() {
    NotedTheme {
        NotedApp()
    }
}

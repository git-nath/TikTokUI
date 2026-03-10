package com.example.tiktokui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.tiktokui.ui.screens.TikTokCommentsScreen
import com.example.tiktokui.ui.theme.TikTokUITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TikTokUITheme(darkTheme = false, dynamicColor = false) {
                TikTokCommentsScreen()
            }
        }
    }
}

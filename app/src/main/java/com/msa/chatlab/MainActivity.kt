package com.msa.chatlab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import com.msa.chatlab.core.common.ui.theme.ChatLabTheme
import com.msa.chatlab.navigation.AppShell

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ مهم: برای edge-to-edge واقعی
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()

        setContent {
            ChatLabTheme {
                AppShell()
            }
        }
    }
}

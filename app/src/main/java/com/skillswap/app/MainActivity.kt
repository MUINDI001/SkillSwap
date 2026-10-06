package com.skillswap.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.skillswap.app.navigation.MainScreen
import com.skillswap.app.ui.theme.SkillSwapTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SkillSwapTheme {
                MainScreen()
            }
        }
    }
}

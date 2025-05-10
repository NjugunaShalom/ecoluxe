package com.example.ecoluxe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ecoluxe.navigation.EcoLuxeApp
import com.example.ecoluxe.ui.theme.EcoluxeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EcoluxeTheme {
                EcoLuxeApp()
            }
        }
    }
}

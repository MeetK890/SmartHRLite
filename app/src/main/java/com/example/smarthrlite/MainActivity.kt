package com.example.smarthrlite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.smarthrlite.navigation.NavGraph
import com.example.smarthrlite.ui.theme.SmartHRLiteTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SmartHRLiteTheme {
                NavGraph()   // ✅ ONLY THIS
            }
        }
    }
}










































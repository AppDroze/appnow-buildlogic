package com.appnow.samples.androidapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.appnow.samples.shared.SharedApi

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SharedApi.initialize()
        setContent {
            MaterialTheme {
                Text(SharedApi.getMessage())
            }
        }
    }
}


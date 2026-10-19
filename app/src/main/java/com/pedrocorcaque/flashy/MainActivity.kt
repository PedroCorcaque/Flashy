package com.pedrocorcaque.flashy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.pedrocorcaque.flashy.ui.theme.FlashyTheme
import androidx.navigation.compose.rememberNavController
import com.pedrocorcaque.flashy.ui.navigation.FlashyNavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FlashyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    FlashyNavGraph(navController = navController)
                }
            }
        }
    }
}

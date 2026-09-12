package com.misnotiks.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.misnotiks.app.data.DataStore
import com.misnotiks.app.ui.AppNavHost
import com.misnotiks.app.ui.ThemeState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = DataStore(applicationContext)
        val themeState = ThemeState(applicationContext)

        setContent {
            val colorScheme = if (themeState.darkTheme) darkColorScheme() else lightColorScheme()
            MaterialTheme(colorScheme = colorScheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    AppNavHost(navController = navController, store = store, themeState = themeState)
                }
            }
        }
    }
}

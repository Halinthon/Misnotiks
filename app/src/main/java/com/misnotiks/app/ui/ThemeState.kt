package com.misnotiks.app.ui

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Guarda si el usuario prefiere tema oscuro o claro.
 * Por defecto usa el tema del sistema la primera vez que se abre la app.
 */
class ThemeState(context: Context) {
    private val prefs = context.getSharedPreferences("misnotiks_prefs", Context.MODE_PRIVATE)

    private val systemIsDark: Boolean = run {
        val flags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        flags == Configuration.UI_MODE_NIGHT_YES
    }

    var darkTheme by mutableStateOf(prefs.getBoolean(KEY_DARK, systemIsDark))
        private set

    fun toggle() {
        darkTheme = !darkTheme
        prefs.edit().putBoolean(KEY_DARK, darkTheme).apply()
    }

    companion object {
        private const val KEY_DARK = "dark_theme"
    }
}

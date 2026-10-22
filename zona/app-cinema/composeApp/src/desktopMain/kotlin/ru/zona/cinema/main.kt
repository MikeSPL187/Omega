package ru.zona.cinema

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

private val Portrait = DpSize(390.dp, 844.dp)
private val Landscape = DpSize(844.dp, 390.dp)

/** Desktop preview: a phone-sized window that turns landscape for the player. */
fun main() = application {
    val state = rememberWindowState(size = Portrait)
    Window(onCloseRequest = ::exitApplication, state = state, title = "Zona Cinema") {
        ZonaApp(requestLandscape = { state.size = if (it) Landscape else Portrait })
    }
}

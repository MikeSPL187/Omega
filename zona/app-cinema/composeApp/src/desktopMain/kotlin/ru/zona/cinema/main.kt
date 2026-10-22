package ru.zona.cinema

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

private val Portrait = DpSize(390.dp, 844.dp)
private val Landscape = DpSize(844.dp, 390.dp)

private val Tv = DpSize(960.dp, 540.dp)

/**
 * Desktop preview: a phone-sized window that turns landscape for the player.
 * `./gradlew :composeApp:run --args=tv` opens the Android TV layout (arrows + Enter for the D-pad).
 */
fun main(args: Array<String>) = application {
    val tv = "tv" in args
    val state = rememberWindowState(size = if (tv) Tv else Portrait)
    Window(onCloseRequest = ::exitApplication, state = state, title = "Zona Cinema") {
        if (tv) {
            ZonaApp(initial = Screen.TvHome)
        } else {
            ZonaApp(requestLandscape = { state.size = if (it) Landscape else Portrait })
        }
    }
}

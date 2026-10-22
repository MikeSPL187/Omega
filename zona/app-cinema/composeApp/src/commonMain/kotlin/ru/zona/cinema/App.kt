package ru.zona.cinema

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import ru.zona.cinema.data.Demo
import ru.zona.cinema.screens.CardScreen
import ru.zona.cinema.screens.HomeScreen
import ru.zona.cinema.screens.PlayerScreen
import ru.zona.cinema.theme.ZonaColors
import ru.zona.cinema.theme.ZonaTheme

sealed interface Screen {
    data object Home : Screen
    data class Card(val titleId: String) : Screen
    data class Player(val titleId: String, val season: Int = 2, val episode: Int = 5, val streamsOpen: Boolean = false) : Screen
}

class Navigator internal constructor(private val get: () -> List<Screen>, private val set: (List<Screen>) -> Unit) {
    val canGoBack: Boolean get() = get().size > 1
    fun open(screen: Screen) = set(get() + screen)
    fun back() { if (canGoBack) set(get().dropLast(1)) }
}

/**
 * Root of the Zona Cinema UI.
 *
 * @param requestLandscape called with true while the player is shown so the host can rotate.
 * @param backHandler host hook for the system back gesture (Android); no-op elsewhere.
 */
@Composable
fun ZonaApp(
    initial: Screen = Screen.Home,
    requestLandscape: (Boolean) -> Unit = {},
    backHandler: @Composable (enabled: Boolean, onBack: () -> Unit) -> Unit = { _, _ -> },
) {
    var stack by remember { mutableStateOf(listOf(initial)) }
    val nav = remember { Navigator({ stack }, { stack = it }) }
    ZonaTheme {
        backHandler(stack.size > 1) { nav.back() }
        Box(Modifier.fillMaxSize().background(ZonaColors.Bg)) {
            when (val s = stack.last()) {
                Screen.Home -> HomeScreen(nav)
                is Screen.Card -> CardScreen(Demo.byId(s.titleId), nav)
                is Screen.Player -> PlayerScreen(s, nav, requestLandscape)
            }
        }
    }
}

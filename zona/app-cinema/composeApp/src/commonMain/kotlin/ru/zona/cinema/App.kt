package ru.zona.cinema

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ru.zona.cinema.data.Demo
import ru.zona.cinema.screens.CardScreen
import ru.zona.cinema.screens.CatalogScreen
import ru.zona.cinema.screens.DownloadsScreen
import ru.zona.cinema.screens.HomeScreen
import ru.zona.cinema.screens.LoginScreen
import ru.zona.cinema.screens.MyScreen
import ru.zona.cinema.screens.OfflineScreen
import ru.zona.cinema.screens.PlayerScreen
import ru.zona.cinema.screens.ProfileScreen
import ru.zona.cinema.screens.SearchScreen
import ru.zona.cinema.screens.TrailerFeedScreen
import ru.zona.cinema.screens.TvHomeScreen
import ru.zona.cinema.theme.ZonaColors
import ru.zona.cinema.theme.ZonaTheme
import ru.zona.cinema.ui.FloatingNavBar
import ru.zona.cinema.ui.Tab

sealed interface Screen {
    /** A bottom-bar tab; always the root of the stack. */
    data class Main(val tab: Tab = Tab.Home, val filtersOpen: Boolean = false) : Screen
    data class Card(val titleId: String) : Screen
    data class Player(val titleId: String, val season: Int = 2, val episode: Int = 5, val streamsOpen: Boolean = false) : Screen
    data class Trailers(val startTitleId: String? = null) : Screen
    data object Downloads : Screen
    data object Login : Screen
    data object Offline : Screen
    /** Android TV home (rail + rows); root of the stack on TV. */
    data object TvHome : Screen

    companion object {
        val Home = Main(Tab.Home)
    }
}

class Navigator internal constructor(private val get: () -> List<Screen>, private val set: (List<Screen>) -> Unit) {
    val canGoBack: Boolean get() = get().size > 1
    fun open(screen: Screen) = set(get() + screen)
    fun back() { if (canGoBack) set(get().dropLast(1)) }

    /** Bottom-bar navigation: each tab starts a fresh stack. */
    fun tab(tab: Tab) = set(listOf(Screen.Main(tab)))
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
    var stack by remember {
        mutableStateOf(if (initial is Screen.Main || initial == Screen.TvHome) listOf(initial) else listOf(Screen.Home, initial))
    }
    val nav = remember { Navigator({ stack }, { stack = it }) }
    ZonaTheme {
        backHandler(stack.size > 1) { nav.back() }
        Box(Modifier.fillMaxSize().background(ZonaColors.Bg)) {
            when (val s = stack.last()) {
                is Screen.Main -> {
                    when (s.tab) {
                        Tab.Home -> HomeScreen(nav)
                        Tab.Catalog -> CatalogScreen(nav, s.filtersOpen)
                        Tab.My -> MyScreen(nav)
                        Tab.Search -> SearchScreen(nav)
                        Tab.Profile -> ProfileScreen(nav)
                    }
                    if (!s.filtersOpen) FloatingNavBar(s.tab, onSelect = nav::tab, modifier = Modifier.align(Alignment.BottomCenter))
                }
                is Screen.Card -> CardScreen(Demo.byId(s.titleId), nav)
                is Screen.Player -> PlayerScreen(s, nav, requestLandscape)
                is Screen.Trailers -> TrailerFeedScreen(s.startTitleId, nav)
                Screen.Downloads -> DownloadsScreen(nav)
                Screen.Login -> LoginScreen(nav)
                Screen.Offline -> OfflineScreen(nav)
                Screen.TvHome -> TvHomeScreen(nav)
            }
        }
    }
}

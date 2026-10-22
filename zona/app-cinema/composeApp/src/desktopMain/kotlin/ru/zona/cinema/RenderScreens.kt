package ru.zona.cinema

import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import org.jetbrains.skia.EncodedImageFormat
import java.io.File

/** Renders the key screens to PNG at 2x without a display: `./gradlew renderScreens`. */
@OptIn(InternalComposeUiApi::class) // KeyEvent(key, type) to simulate the D-pad
fun main(args: Array<String>) {
    val out = File(args.firstOrNull() ?: "build/screens").apply { mkdirs() }
    // name, screen, width × height in dp (tall sizes show the whole scroll).
    val shots = listOf(
        Shot("01-home", Screen.Home, 390, 844),
        Shot("01-home-full", Screen.Home, 390, 1640),
        Shot("02-card", Screen.Card("north"), 390, 844),
        Shot("02-card-full", Screen.Card("north"), 390, 1860),
        Shot("03-player", Screen.Player("north"), 844, 390),
        Shot("04-streams", Screen.Player("north", streamsOpen = true), 844, 390),
        Shot("05-card-movie", Screen.Card("flight"), 390, 1400),
        Shot("06-catalog", Screen.Main(ru.zona.cinema.ui.Tab.Catalog), 390, 844),
        Shot("07-filters", Screen.Main(ru.zona.cinema.ui.Tab.Catalog, filtersOpen = true), 390, 844),
        Shot("08-search", Screen.Main(ru.zona.cinema.ui.Tab.Search), 390, 844),
        Shot("09-my", Screen.Main(ru.zona.cinema.ui.Tab.My), 390, 844),
        Shot("10-profile", Screen.Main(ru.zona.cinema.ui.Tab.Profile), 390, 844),
        Shot("11-downloads", Screen.Downloads, 390, 844),
        Shot("12-login", Screen.Login, 390, 844),
        Shot("13-offline", Screen.Offline, 390, 844),
        Shot("14-trailers", Screen.Trailers("harbor"), 390, 844),
        Shot("15-tv", Screen.TvHome, 960, 540),
        // D-pad check: four steps right (a wide display face), then down into the next row.
        Shot("16-tv-right", Screen.TvHome, 960, 540, keys = List(4) { Key.DirectionRight }),
        Shot("17-tv-down", Screen.TvHome, 960, 540, keys = listOf(Key.DirectionRight, Key.DirectionDown)),
    )
    for ((name, screen, w, h, keys) in shots) {
        ImageComposeScene(w * 2, h * 2, Density(2f)) { ZonaApp(initial = screen) }.use { scene ->
            // A few frames so fonts, images and enter animations settle.
            var t = 0L
            repeat(40) { scene.render(t); t += 50_000_000L; Thread.sleep(15) }
            for (code in keys) {
                for (type in listOf(KeyEventType.KeyDown, KeyEventType.KeyUp)) scene.sendKeyEvent(KeyEvent(code, type))
                repeat(20) { scene.render(t); t += 50_000_000L; Thread.sleep(10) }
            }
            val png = scene.render(t).encodeToData(EncodedImageFormat.PNG) ?: error("encode failed")
            File(out, "$name.png").writeBytes(png.bytes)
            println("rendered $name")
        }
    }
}

private data class Shot(val name: String, val screen: Screen, val w: Int, val h: Int, val keys: List<Key> = emptyList())

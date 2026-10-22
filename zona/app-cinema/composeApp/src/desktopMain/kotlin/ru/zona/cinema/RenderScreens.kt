package ru.zona.cinema

import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import org.jetbrains.skia.EncodedImageFormat
import java.io.File

/** Renders the key screens to PNG at 2x without a display: `./gradlew renderScreens`. */
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
    )
    for ((name, screen, w, h) in shots) {
        ImageComposeScene(w * 2, h * 2, Density(2f)) { ZonaApp(initial = screen) }.use { scene ->
            // A few frames so fonts, images and enter animations settle.
            var t = 0L
            repeat(40) { scene.render(t); t += 50_000_000L; Thread.sleep(15) }
            val png = scene.render(t).encodeToData(EncodedImageFormat.PNG) ?: error("encode failed")
            File(out, "$name.png").writeBytes(png.bytes)
            println("rendered $name")
        }
    }
}

private data class Shot(val name: String, val screen: Screen, val w: Int, val h: Int)

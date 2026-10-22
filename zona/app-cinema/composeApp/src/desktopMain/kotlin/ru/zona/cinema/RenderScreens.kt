package ru.zona.cinema

import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.unit.Density
import androidx.compose.ui.use
import org.jetbrains.skia.EncodedImageFormat
import java.io.File

/** Renders the key screens to PNG at 2x without a display: `./gradlew renderScreens`. */
fun main(args: Array<String>) {
    val out = File(args.firstOrNull() ?: "build/screens").apply { mkdirs() }
    val shots = listOf(
        Triple("01-home", Screen.Home, false),
        Triple("02-card", Screen.Card("north"), false),
        Triple("03-player", Screen.Player("north"), true),
        Triple("04-streams", Screen.Player("north", streamsOpen = true), true),
        Triple("05-card-movie", Screen.Card("flight"), false),
    )
    for ((name, screen, landscape) in shots) {
        val (w, h) = if (landscape) 844 to 390 else 390 to 844
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

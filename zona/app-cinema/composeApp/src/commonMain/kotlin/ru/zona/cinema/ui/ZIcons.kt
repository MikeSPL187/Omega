package ru.zona.cinema.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/** Icon set from the mockups (24×24, 2px stroke), built from SVG path data. */
object ZIcons {
    private fun stroke(name: String, vararg d: String, width: Float = 2f) = build(name, d, filled = false, width = width)
    private fun fill(name: String, vararg d: String) = build(name, d, filled = true, width = 0f)

    private fun build(name: String, paths: Array<out String>, filled: Boolean, width: Float, size: Float = 24f): ImageVector {
        val b = ImageVector.Builder(name, size.dp, size.dp, size, size)
        for (d in paths) {
            b.addPath(
                pathData = PathParser().parsePathString(d).toNodes(),
                fill = if (filled) SolidColor(Color.Black) else null,
                stroke = if (filled) null else SolidColor(Color.Black),
                strokeLineWidth = width,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
        return b.build()
    }

    val Back = stroke("back", "M15 5l-7 7 7 7", width = 2.2f)
    val Close = stroke("close", "M6 6l12 12M18 6 6 18", width = 2.2f)
    val ChevronRight = stroke("chevronRight", "M9 5l7 7-7 7", width = 2.4f)
    val ChevronDown = stroke("chevronDown", "M6 9l6 6 6-6", width = 2.4f)
    val Play = fill("play", "M6 4v16l14-8z")
    val Pause = fill("pause", "M7 4h2a1 1 0 0 1 1 1v14a1 1 0 0 1-1 1H7a1 1 0 0 1-1-1V5a1 1 0 0 1 1-1zM15 4h2a1 1 0 0 1 1 1v14a1 1 0 0 1-1 1h-2a1 1 0 0 1-1-1V5a1 1 0 0 1 1-1z")
    val Plus = stroke("plus", "M12 5v14M5 12h14", width = 2.4f)
    val Check = stroke("check", "M5 12l5 5 9-10", width = 2.6f)
    val Bell = stroke("bell", "M6 16V11a6 6 0 0 1 12 0v5l2 2H4zM10 21h4")
    val Download = stroke("download", "M12 4v11M7 10l5 5 5-5M5 20h14")
    val Flag = stroke("flag", "M5 21V4h11l-2 4 2 4H5")
    val Share = stroke("share", "M12 3v12M7 8l5-5 5 5M5 13v6a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2v-6")
    val Cast = stroke("cast", "M3 8V6a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2h-6M3 12a8 8 0 0 1 8 8M3 16a4 4 0 0 1 4 4")
    val Trailer = stroke("trailer", "M6 5h12a3 3 0 0 1 3 3v8a3 3 0 0 1-3 3H6a3 3 0 0 1-3-3V8a3 3 0 0 1 3-3z", "M10 9.5v5l4.5-2.5z")
    val Mic = stroke("mic", "M12 3a3 3 0 0 1 3 3v5a3 3 0 0 1-6 0V6a3 3 0 0 1 3-3z", "M5 11a7 7 0 0 0 14 0M12 18v3")
    val VolumeOff = stroke("volumeOff", "M4 9h4l5-4v14l-5-4H4z", "M17 9l4 6M21 9l-4 6")
    val Rewind = stroke("rewind", "M4.5 12a7.5 7.5 0 1 0 2.8-5.8M4.5 4v4.2h4.2", width = 1.8f)
    val Forward = stroke("forward", "M19.5 12a7.5 7.5 0 1 1-2.8-5.8M19.5 4v4.2h-4.2", width = 1.8f)
    val Skip = fill("skip", "M5 5v14l8-7zM13 5v14l8-7z")
    val Next = fill("next", "M16 5h2v14h-2zM4 5v14l11-7z")
    val Pip = stroke("pip", "M5 5h14a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2z", "M13 11h5v5h-5z")
    val Lock = stroke("lock", "M7 11h10a2 2 0 0 1 2 2v6a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2v-6a2 2 0 0 1 2-2zM8 11V8a4 4 0 0 1 8 0v3")
    val Subtitles = stroke("subtitles", "M5 5h14a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2zM7 13h4M13 13h4M7 16h6")
    val Episodes = stroke("episodes", "M4 6h16M4 12h16M4 18h10")

    val Home = fill("home", "M3 10.5 12 3l9 7.5V20a1 1 0 0 1-1 1h-5v-6H9v6H4a1 1 0 0 1-1-1z")
    val HomeOutline = stroke("homeOutline", "M3 10.5 12 3l9 7.5V20a1 1 0 0 1-1 1h-5v-6H9v6H4a1 1 0 0 1-1-1z")
    private val gridPaths = arrayOf(
        "M6 4h3a2 2 0 0 1 2 2v3a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2z",
        "M15 4h3a2 2 0 0 1 2 2v3a2 2 0 0 1-2 2h-3a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2z",
        "M6 13h3a2 2 0 0 1 2 2v3a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2v-3a2 2 0 0 1 2-2z",
        "M15 13h3a2 2 0 0 1 2 2v3a2 2 0 0 1-2 2h-3a2 2 0 0 1-2-2v-3a2 2 0 0 1 2-2z",
    )
    val Catalog = stroke("catalog", *gridPaths)
    val CatalogFilled = fill("catalogFilled", *gridPaths)
    val Bookmark = stroke("bookmark", "M6 3h12v18l-6-4-6 4z")
    val BookmarkFilled = fill("bookmarkFilled", "M6 3h12v18l-6-4-6 4z")
    val Search = stroke("search", "M18 11a7 7 0 1 1-14 0a7 7 0 1 1 14 0z", "M20 20l-4-4")
    val SearchBold = stroke("searchBold", "M18 11a7 7 0 1 1-14 0a7 7 0 1 1 14 0z", "M20 20l-4-4", width = 2.8f)
    val Profile = stroke("profile", "M16 8a4 4 0 1 1-8 0a4 4 0 1 1 8 0z", "M4 21c1.5-4 4.5-6 8-6s6.5 2 8 6")
    val ProfileFilled = fill("profileFilled", "M16 8a4 4 0 1 1-8 0a4 4 0 1 1 8 0z", "M4 21c1.5-4 4.5-6 8-6s6.5 2 8 6z")

    /** The Zona mark, viewBox 248×256. */
    val Logo: ImageVector = ImageVector.Builder("logo", 24.dp, 25.dp, 248f, 256f).apply {
        addPath(
            pathData = PathParser().parsePathString(
                "M0,25.61C0,48.29 74.4,76.64 74.4,89.62C74.4,105.8 0,210.32 0,229.76C0,246.15 9.54,256 38.14,256H219.37C238.44,256 248,239.6 248,229.76C248,203.51 185.99,165.28 185.99,149.09C185.99,132.88 248,45.53 248,26.11C248,9.91 235.27,0.15 213.01,0.15C203.46,0.15 62.32,0 49.59,0C49.3,0 48.99,0 48.68,0C35.2,0 0,0.29 0,25.61Z"
            ).toNodes(),
            fill = SolidColor(Color(0xFF0094F6)),
        )
    }.build()
}

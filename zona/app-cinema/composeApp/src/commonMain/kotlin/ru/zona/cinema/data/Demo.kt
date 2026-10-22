package ru.zona.cinema.data

import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.DrawableResource
import ru.zona.cinema.res.Res
import ru.zona.cinema.res.p_dunes
import ru.zona.cinema.res.p_flight
import ru.zona.cinema.res.p_forest
import ru.zona.cinema.res.p_harbor
import ru.zona.cinema.res.p_neon
import ru.zona.cinema.res.p_north
import ru.zona.cinema.res.p_orbit
import ru.zona.cinema.res.p_peaks
import ru.zona.cinema.res.w_dunes
import ru.zona.cinema.res.w_flight
import ru.zona.cinema.res.w_forest
import ru.zona.cinema.res.w_harbor
import ru.zona.cinema.res.w_neon
import ru.zona.cinema.res.w_north
import ru.zona.cinema.res.w_orbit
import ru.zona.cinema.res.w_peaks
import ru.zona.cinema.theme.TitleFace

/** Procedural key art (zona/design/v3/scenes.py): portrait poster and 16:9 still. */
enum class Art(val poster: DrawableResource, val wide: DrawableResource) {
    North(Res.drawable.p_north, Res.drawable.w_north),
    Harbor(Res.drawable.p_harbor, Res.drawable.w_harbor),
    Flight(Res.drawable.p_flight, Res.drawable.w_flight),
    Dunes(Res.drawable.p_dunes, Res.drawable.w_dunes),
    Forest(Res.drawable.p_forest, Res.drawable.w_forest),
    Neon(Res.drawable.p_neon, Res.drawable.w_neon),
    Orbit(Res.drawable.p_orbit, Res.drawable.w_orbit),
    Peaks(Res.drawable.p_peaks, Res.drawable.w_peaks),
}

/** How a title's name is lettered on its poster. */
data class Lettering(val face: TitleFace, val sizeSp: Float, val color: Color, val trackingEm: Float)

data class Title(
    val id: String,
    val name: String,
    val original: String,
    val art: Art,
    val lettering: Lettering,
    val isSeries: Boolean,
    val year: Int,
    val country: String,
    val genres: String,
    val age: String,
    val kp: String,
    val imdb: String,
    val zona: String,
    val description: String,
) {
    val kind get() = if (isSeries) "Сериал" else "Фильм"
    val meta get() = "$kind · $year · $genres"
}

data class Progress(val title: Title, val label: String, val fraction: Float, val season: Int = 0, val episode: Int = 0) {
    val remaining get() = label.substringAfterLast(" · ")
}

data class Clip(val title: Title, val kind: String, val duration: String, val alignX: Float)

data class Episode(val number: Int, val duration: String, val date: String, val watched: Boolean, val progress: Float?, val upcoming: Boolean)

data class CastMember(val initials: String, val name: String, val role: String, val from: Color, val to: Color)

/** A dubbing track and the qualities it is available in. */
data class Voice(val name: String, val qualities: List<String>, val subtitles: Boolean, val source: String)

/**
 * Demo catalogue for the UI preview. Names, numbers and descriptions are placeholders,
 * not real data; wire these screens to the Zona API models (mobi.zona.data.model) later.
 */
object Demo {
    private const val placeholderPlot =
        "Описание сюжета — три-четыре строки текста из каталога, обрезанные по высоте. Пользователь видит начало и кнопку, чтобы раскрыть остальное."

    val north = Title("north", "Северный ветер", "North Wind", Art.North, Lettering(TitleFace.Oswald, 19f, Color.White, .05f),
        true, 2025, "Россия", "драма, триллер", "16+", "8.4", "8.1", "8.6", placeholderPlot)
    val harbor = Title("harbor", "Тихая гавань", "Quiet Harbor", Art.Harbor, Lettering(TitleFace.Playfair, 17f, Color(0xFFFFE7C2), 0f),
        true, 2025, "Россия", "драма", "16+", "8.0", "7.8", "8.2", placeholderPlot)
    val flight = Title("flight", "Ночной рейс", "Night Flight", Art.Flight, Lettering(TitleFace.Russo, 15f, Color(0xFFD6E4FF), .04f),
        false, 2024, "Россия", "триллер", "16+", "7.7", "7.3", "7.9", placeholderPlot)
    val dunes = Title("dunes", "Пески", "Sands", Art.Dunes, Lettering(TitleFace.Unbounded, 20f, Color(0xFFFFE0B0), .02f),
        false, 2025, "Казахстан", "драма", "12+", "8.1", "7.6", "8.0", placeholderPlot)
    val forest = Title("forest", "Тайга", "Taiga", Art.Forest, Lettering(TitleFace.Oswald, 24f, Color(0xFFEEF4EC), .32f),
        false, 2024, "Россия", "триллер", "16+", "7.4", "7.0", "7.5", placeholderPlot)
    val neon = Title("neon", "Неон", "Neon", Art.Neon, Lettering(TitleFace.Unbounded, 22f, Color(0xFFFF8CCB), .04f),
        false, 2024, "Россия", "драма", "18+", "7.9", "7.5", "8.0", placeholderPlot)
    val orbit = Title("orbit", "Орбита", "Orbit", Art.Orbit, Lettering(TitleFace.Russo, 19f, Color(0xFFFFE2C2), .18f),
        false, 2024, "Россия", "фантастика", "12+", "7.2", "6.9", "7.4", placeholderPlot)
    val peaks = Title("peaks", "Вершина", "The Summit", Art.Peaks, Lettering(TitleFace.Playfair, 16f, Color.White, 0f),
        false, 2025, "Россия", "приключения", "12+", "7.6", "7.2", "7.8", placeholderPlot)

    val all = listOf(north, harbor, flight, dunes, forest, neon, orbit, peaks)
    fun byId(id: String) = all.first { it.id == id }

    val continueWatching = listOf(
        Progress(north, "2 сезон · 5 серия · ещё 23 мин", .62f, season = 2, episode = 5),
        Progress(flight, "Фильм · ещё 48 мин", .38f),
    )

    val clips = listOf(
        Clip(harbor, "Трейлер", "1:12", 0f),
        Clip(neon, "Сцена из фильма", "0:42", -.2f),
        Clip(forest, "Тизер", "0:30", .2f),
    )

    val top10 = listOf(north, neon, harbor, dunes, flight)
    val freshMovies = listOf(dunes, peaks, orbit, forest)
    val similar = listOf(harbor, forest, flight, peaks, neon, dunes)

    fun episodes(season: Int): List<Episode> = if (season == 1) {
        (1..9).map { Episode(it, "48 мин", "${it + 2} октября 2024", watched = true, progress = null, upcoming = false) }
    } else {
        val dates = listOf("12 марта", "19 марта", "26 марта", "2 апреля", "9 апреля", "16 апреля", "23 апреля", "30 апреля", "14 мая")
        (1..9).map { n ->
            Episode(n, if (n == 6) "52 мин" else "50 мин", dates[n - 1], watched = n < 5, progress = if (n == 5) .62f else null, upcoming = n == 9)
        }
    }

    val scenes = listOf(
        Clip(north, "Шторм у маяка", "0:38", -.4f),
        Clip(north, "Тизер 2 сезона", "0:30", .4f),
        Clip(forest, "За кадром", "1:05", 0f),
    )

    val cast = listOf(
        CastMember("АП", "Актёр Первый", "Капитан", Color(0xFF2C5E86), Color(0xFF0E1E2C)),
        CastMember("АВ", "Актёр Второй", "Смотритель", Color(0xFF6B4C9A), Color(0xFF1B1440)),
        CastMember("АТ", "Актёр Третий", "Врач", Color(0xFFB2456B), Color(0xFF2A1638)),
        CastMember("АЧ", "Актёр Четвёртый", "Радист", Color(0xFF3D5A4D), Color(0xFF0C1913)),
    )

    val voices = listOf(
        Voice("LostFilm", listOf("1080p", "720p"), subtitles = false, source = "Источник 1"),
        Voice("HDrezka Studio", listOf("1080p", "720p"), subtitles = true, source = "Источник 2"),
        Voice("Кубик в Кубе", listOf("1080p"), subtitles = false, source = "Источник 3"),
        Voice("Оригинал", listOf("480p"), subtitles = true, source = "Источник 3"),
    )

    /**
     * Every demo episode plays Apple's public HLS sample (1080p…270p ladder, WebVTT subtitles)
     * until the Zona API supplies real stream links per voice and quality.
     */
    const val TEST_STREAM = "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_fmp4/master.m3u8"

    fun streamFor(title: Title, season: Int, episode: Int) = TEST_STREAM
}

package ru.zona.cinema.data

/** [filter] is the genre value used by catalogue filters. */
data class Genre(val name: String, val filter: String, val art: Art, val alignX: Float = 0f)

enum class DownloadState { Running, Queued, Failed, Done }

data class Download(
    val title: Title,
    val label: String,
    val state: DownloadState,
    val fraction: Float,
    val detail: String,
    val alignX: Float = 0f,
)

/** Demo data for catalogue, search, "My" and downloads. Placeholder values. */
object Library {
    val genres = listOf(
        Genre("Драмы", "Драма", Art.Harbor),
        Genre("Триллеры", "Триллер", Art.Neon),
        Genre("Фантастика", "Фантастика", Art.Orbit, .4f),
        Genre("Приключения", "Приключения", Art.Peaks),
        Genre("Детективы", "Детектив", Art.North, -.5f),
        Genre("Боевики", "Боевик", Art.Dunes, -.2f),
    )

    val catalog = listOf(Demo.neon, Demo.dunes, Demo.peaks, Demo.orbit, Demo.flight, Demo.forest, Demo.harbor, Demo.north, Demo.neon)
    val series = listOf(Demo.north, Demo.harbor)

    val recentQueries = listOf("северный ветер", "комедии 2024", "тихая гавань")

    val favorites = listOf(Demo.north, Demo.harbor, Demo.neon, Demo.dunes, Demo.peaks)
    /** Titles with an episode the user has not seen yet. */
    val freshEpisodes = setOf(Demo.north.id, Demo.harbor.id)

    val downloads = listOf(
        Download(Demo.flight, "Ночной рейс", DownloadState.Running, .42f, "42% · 680 МБ из 1,6 ГБ · ~4 мин"),
        Download(Demo.north, "Северный ветер · С2 Е6", DownloadState.Queued, 0f, "В очереди · 1,2 ГБ", .4f),
        Download(Demo.harbor, "Тихая гавань · С1 Е1", DownloadState.Failed, .12f, "Прервалось на 12%: пропал интернет"),
        Download(Demo.north, "Северный ветер · С2 Е5", DownloadState.Done, .62f, "2 сезон · 5 серия · осталось 23 мин", -.5f),
        Download(Demo.forest, "Тайга", DownloadState.Done, 0f, "Фильм · 1 ч 52 мин"),
    )
}

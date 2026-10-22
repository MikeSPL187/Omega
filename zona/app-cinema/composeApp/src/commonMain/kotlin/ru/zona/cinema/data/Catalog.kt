package ru.zona.cinema.data

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import ru.zona.cinema.data.api.ApiEntity
import ru.zona.cinema.data.api.ApiEpisode
import ru.zona.cinema.data.api.Dictionaries
import ru.zona.cinema.data.api.Filter
import ru.zona.cinema.data.api.ZonaApi
import ru.zona.cinema.theme.TitleFace
import kotlin.math.roundToInt
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** A season of a series with its episodes in order. */
data class Season(val number: Int, val episodes: List<Episode>)

/** One catalogue page: the titles loaded so far and how many match in total. */
data class CatalogPage(val titles: List<Title>, val total: Int)

/** Home screen rows, loaded together. */
data class HomeFeed(val hero: Title, val top: List<Title>, val movies: List<Title>, val series: List<Title>, val fresh: List<Title>)

/**
 * Catalogue backed by the Zona API: lists, search, cards and episodes, mapped to the UI
 * models. Entities are cached by id, so a card opened from a list shows at once and only
 * fills in its details. Demo ids ("north", …) still resolve to the demo titles.
 */
object Catalog {
    private val api = ZonaApi()
    private val lock = Mutex()
    private val cache = mutableMapOf<String, Title>()
    /** The API returns one entity per request; keep a few in flight. */
    private val inFlight = Semaphore(10)

    fun cached(id: String): Title? = cache[id] ?: Demo.byIdOrNull(id)

    suspend fun home(): HomeFeed = coroutineScope {
        // One request first, so the burst below shares its HTTP/2 connection instead of
        // racing to open several (each new one may stall, see ZonaApi).
        val popular = api.movieIds(0, 20, Filter())
        val movies = async { titles(popular.data) }
        val series = async { series(Filter(), 0, 12) }
        val fresh = async { movies(Filter(sortBy = "release_date", yearTo = currentYear()), 0, 16) }
        val m = movies.await()
        val s = series.await()
        HomeFeed(
            hero = (s + m).firstOrNull { it.backdropUrl != null } ?: (s + m).first(),
            top = m.take(10),
            movies = m.drop(10),
            series = s,
            // Release-date order starts with announced titles; keep what is already out.
            fresh = fresh.await().filter { it.backdropUrl != null }.take(12),
        )
    }

    suspend fun movies(filter: Filter, offset: Int, limit: Int): List<Title> = titles(api.movieIds(offset, limit, filter).data)
    suspend fun series(filter: Filter, offset: Int, limit: Int): List<Title> = titles(api.serialIds(offset, limit, filter).data)

    /** First [limit] titles matching [filter], movies or series. */
    suspend fun browse(series: Boolean, filter: Filter, limit: Int): CatalogPage {
        val ids = if (series) api.serialIds(0, limit, filter) else api.movieIds(0, limit, filter)
        return CatalogPage(titles(ids.data), ids.total)
    }

    /** API ids for genre and country names shown in the filters. */
    fun genreId(name: String): String? = Dictionaries.genres.entries.firstOrNull { it.value.equals(name, ignoreCase = true) }?.key
    fun countryId(name: String): String? {
        val full = if (name == "Корея") "Южная Корея" else name
        return Dictionaries.countries.entries.firstOrNull { it.value == full }?.key
    }

    suspend fun search(query: String): List<Title> = api.search(query).map { remember(it.toTitle()) }

    /** Full card data; returns the cached list version first if [cached] is enough. */
    suspend fun details(id: String): Title {
        val e = api.entity(id, occupancy = "Full") ?: error("Нет такого фильма: $id")
        return remember(e.toTitle(full = true))
    }

    suspend fun seasons(id: String): List<Season> =
        api.episodes(id).map { it.toEpisode() }.groupBy { it.season }.entries.sortedBy { it.key }
            .map { (n, eps) -> Season(n, eps.sortedBy { it.number }) }

    suspend fun titles(ids: List<String>): List<Title> = coroutineScope {
        ids.map { id ->
            async {
                cache[id] ?: inFlight.withPermit { runCatching { api.entity(id) }.getOrNull() }?.let { remember(it.toTitle()) }
            }
        }.awaitAll().filterNotNull()
    }

    private suspend fun remember(t: Title): Title = lock.withLock {
        // A list response must not replace details already loaded for the card.
        val kept = cache[t.id]?.takeIf { t.details == null && it.details != null } ?: t
        cache[t.id] = kept
        kept
    }
}

private const val IMG = "https://imgr1.imgzona.video/images"

/** Image paths from the original app (Ih/u.java in 3.0.75): `/images/<kind>/<id / 1000>/<id>.jpg`. */
private fun posterUrl(id: Long) = "$IMG/film_240/${id / 1000}/$id.jpg"
private fun backdropUrl(id: Long) = "$IMG/backdrop_1920/${id / 1000}/$id.jpg"
private fun actorUrl(id: Long) = "$IMG/actor/${id / 1000}/$id.jpg"

private val fallbackArt = Art.entries

private fun ApiEntity.toTitle(full: Boolean = false): Title {
    val idNum = id.toLongOrNull() ?: 0L
    val genreNames = genreIds.mapNotNull { Dictionaries.genres[it] }.map { it.lowercase() }
    val countryNames = countryIds.mapNotNull { Dictionaries.countries[it] }
    return Title(
        id = id,
        name = name.best,
        original = name.original?.takeIf { it.isNotBlank() && it != name.best }.orEmpty(),
        art = fallbackArt[(idNum % fallbackArt.size).toInt()],
        lettering = Lettering(TitleFace.Oswald, 19f, Color.White, .05f),
        isSeries = isSeries,
        year = year ?: 0,
        country = countryNames.firstOrNull().orEmpty(),
        genres = genreNames.take(2).joinToString(", "),
        age = minAge?.let { "$it+" } ?: "",
        kp = ratings?.kinopoisk?.value.format(),
        imdb = ratings?.imdb?.value.format(),
        zona = ratings?.zona?.value.format(),
        description = description?.best.orEmpty(),
        posterUrl = idNum.takeIf { it > 0 }?.let(::posterUrl),
        backdropUrl = backdropId?.let(::backdropUrl),
        details = if (!full) null else TitleDetails(
            directors = directors.joinToString(", ") { it.name.best },
            cast = actors.take(20).map { Person(it.id, it.name.best, it.id.toLongOrNull()?.let(::actorUrl)) },
            runtimeMinutes = runtime?.takeIf { it > 0 },
            premiere = releaseDateInt?.let(::formatDate),
            countries = countryNames.joinToString(", "),
            genres = genreNames.joinToString(", "),
            relatedIds = rels.sortedBy { it.p }.map { it.rmi.toString() }.filter { it != id },
        ),
    )
}

@OptIn(ExperimentalTime::class)
private fun ApiEpisode.toEpisode(): Episode {
    val released = releaseDate
    return Episode(
        number = num.episode,
        duration = "",
        date = released?.let(::formatDate).orEmpty(),
        watched = false,
        progress = null,
        upcoming = released != null && released > Clock.System.now().toEpochMilliseconds(),
        name = title?.best.orEmpty(),
        season = num.season,
    )
}

/** "8.4"; empty when the rating is missing or zero. */
private fun Double?.format(): String {
    if (this == null || this <= 0.0) return ""
    val tenths = (this * 10).roundToInt()
    return "${tenths / 10}.${tenths % 10}"
}

private val months = listOf("января", "февраля", "марта", "апреля", "мая", "июня", "июля", "августа", "сентября", "октября", "ноября", "декабря")

/** "12 марта 2025" from epoch milliseconds (UTC). */
internal fun formatDate(epochMillis: Long): String {
    // Days to civil date (Howard Hinnant's algorithm), to avoid a date-time dependency.
    val z = epochMillis.floorDiv(86_400_000L) + 719_468
    val era = z.floorDiv(146_097L)
    val doe = z - era * 146_097
    val yoe = (doe - doe / 1460 + doe / 36_524 - doe / 146_096) / 365
    val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
    val mp = (5 * doy + 2) / 153
    val day = doy - (153 * mp + 2) / 5 + 1
    val month = if (mp < 10) mp + 3 else mp - 9
    val year = yoe + era * 400 + if (month <= 2) 1 else 0
    return "$day ${months[(month - 1).toInt()]} $year"
}

@OptIn(ExperimentalTime::class)
private fun currentYear(): Int = formatDate(Clock.System.now().toEpochMilliseconds()).substringAfterLast(' ').toInt()

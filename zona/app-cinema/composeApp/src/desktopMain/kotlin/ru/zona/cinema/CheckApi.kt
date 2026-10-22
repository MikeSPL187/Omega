package ru.zona.cinema

import kotlinx.coroutines.runBlocking
import ru.zona.cinema.data.Catalog
import ru.zona.cinema.data.api.ZonaApi
import kotlin.time.measureTimedValue

/** `./gradlew checkApi`: calls the live Zona API through [Catalog] and prints timings. */
fun main() = runBlocking {
    val start = kotlin.time.TimeSource.Monotonic.markNow()
    ZonaApi.log = { println("  [${start.elapsedNow().inWholeMilliseconds} ms] $it") }
    val (home, t1) = measureTimedValue { Catalog.home() }
    println("home: hero=${home.hero.name}, top=${home.top.size}, series=${home.series.size}, fresh=${home.fresh.size} in $t1")
    val (found, t2) = measureTimedValue { Catalog.search("матрица") }
    println("search «матрица»: ${found.size} in $t2")
    val (card, t3) = measureTimedValue { Catalog.details("5512084") }
    println("card: ${card.name}, cast=${card.details?.cast?.size} in $t3")
    val (seasons, t4) = measureTimedValue { Catalog.seasons("5512084") }
    println("seasons: ${seasons.map { "${it.number}:${it.episodes.size}" }} in $t4")
}

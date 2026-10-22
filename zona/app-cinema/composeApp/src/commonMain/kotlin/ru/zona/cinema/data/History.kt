package ru.zona.cinema.data

import androidx.compose.runtime.mutableStateListOf

/**
 * What was watched in this session, newest first, for "Продолжить просмотр". Kept in memory
 * only; syncing it with the Zona account (SyncUserApi) comes with sign-in.
 */
object History {
    val items = mutableStateListOf<Progress>()

    fun record(title: Title, season: Int, episode: Int, positionSec: Int, durationSec: Int) {
        if (durationSec <= 0 || positionSec < 10) return
        val left = (durationSec - positionSec) / 60
        val where = if (title.isSeries) "$season сезон · $episode серия" else "Фильм"
        val entry = Progress(title, "$where · ещё $left мин", positionSec.toFloat() / durationSec, season, episode)
        items.removeAll { it.title.id == title.id }
        items.add(0, entry)
    }
}

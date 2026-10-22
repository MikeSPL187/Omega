package ru.zona.cinema.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import ru.zona.cinema.Navigator
import ru.zona.cinema.Screen
import ru.zona.cinema.data.Demo
import ru.zona.cinema.data.Episode
import ru.zona.cinema.data.Title
import ru.zona.cinema.theme.Zona
import ru.zona.cinema.theme.ZonaColors
import ru.zona.cinema.theme.ZonaShapes
import ru.zona.cinema.ui.Artwork
import ru.zona.cinema.ui.Poster
import ru.zona.cinema.ui.PrimaryButton
import ru.zona.cinema.ui.ProgressLine
import ru.zona.cinema.ui.RoundIconButton
import ru.zona.cinema.ui.SectionHeader
import ru.zona.cinema.ui.ZChip
import ru.zona.cinema.ui.ZIcon
import ru.zona.cinema.ui.ZIcons
import ru.zona.cinema.ui.glass

private enum class CardTab(val label: String) { Episodes("Серии"), Scenes("Сцены"), About("О сериале"), Similar("Похожие") }

@Composable
fun CardScreen(title: Title, nav: Navigator) {
    var tab by remember { mutableStateOf(if (title.isSeries) CardTab.Episodes else CardTab.About) }
    var inMy by remember { mutableStateOf(true) }
    var notify by remember { mutableStateOf(false) }
    var downloading by remember { mutableStateOf(false) }
    var season by remember { mutableIntStateOf(2) }
    var expanded by remember { mutableStateOf(false) }
    val progress = Demo.continueWatching.firstOrNull { it.title.id == title.id }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 32.dp)) {
            item(key = "hero") { Hero(title) }

            item(key = "actions") {
                Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)) {
                    val resume = progress?.takeIf { it.season > 0 }
                    PrimaryButton(
                        "Смотреть",
                        { nav.open(Screen.Player(title.id, resume?.season ?: 1, resume?.episode ?: 1)) },
                        Modifier.fillMaxWidth(),
                        height = 54.dp,
                        trailing = resume?.let { "${it.season} сезон, ${it.episode} серия" },
                    )
                    if (progress != null) {
                        Row(Modifier.padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            ProgressLine(progress.fraction, Modifier.weight(1f), track = ZonaColors.Surface2)
                            Spacer(Modifier.width(10.dp))
                            Text("ещё 23 мин", style = Zona.meta.copy(fontSize = 12.sp))
                        }
                    }
                    Row(Modifier.fillMaxWidth().padding(top = 18.dp)) {
                        Action(ZIcons.Trailer, "Трейлер", false) {}
                        Action(if (inMy) ZIcons.Check else ZIcons.Plus, if (inMy) "В Моём" else "В Моё", inMy) { inMy = !inMy }
                        if (title.isSeries) Action(ZIcons.Bell, "Новые серии", notify) { notify = !notify }
                        Action(ZIcons.Download, if (downloading) "Скачивается" else "Скачать", downloading) { downloading = !downloading }
                        Action(ZIcons.Flag, "Проблема", false) {}
                    }
                    Text(
                        title.description,
                        modifier = Modifier.padding(top = 20.dp).clickable { expanded = !expanded },
                        color = ZonaColors.TextSecondary,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        maxLines = if (expanded) Int.MAX_VALUE else 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            item(key = "tabs") {
                val tabs = if (title.isSeries) CardTab.entries else CardTab.entries - CardTab.Episodes
                Column(Modifier.padding(top = 14.dp)) {
                    Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(22.dp)) {
                        tabs.forEach { t ->
                            val on = t == tab
                            Column(
                                Modifier.clickable(role = Role.Tab) { tab = t }.semantics { selected = on },
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Box(Modifier.height(45.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        if (t == CardTab.About && !title.isSeries) "О фильме" else t.label,
                                        fontSize = 15.sp,
                                        fontWeight = if (on) FontWeight.Bold else FontWeight.Medium,
                                        color = if (on) ZonaColors.Text else ZonaColors.TextMuted,
                                    )
                                }
                                Box(
                                    Modifier.height(3.dp).width(if (on) 40.dp else 0.dp)
                                        .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)).background(ZonaColors.Blue),
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = Color(0x14FFFFFF))
                }
            }

            when (tab) {
                CardTab.Episodes -> {
                    item(key = "ep-tools") {
                        Row(Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ZChip("$season сезон", { season = if (season == 2) 1 else 2 }, trailing = ZIcons.ChevronDown)
                            ZChip("LostFilm", { nav.open(Screen.Player(title.id, streamsOpen = true)) }, leading = ZIcons.Mic, trailing = ZIcons.ChevronDown)
                        }
                    }
                    val eps = Demo.episodes(season)
                    val visible = if (season == 2) eps.subList(3, 6) else eps.takeLast(3)
                    items(visible, key = { "ep-$season-${it.number}" }) { e ->
                        EpisodeRow(title, e, onPlay = { nav.open(Screen.Player(title.id, season, e.number)) })
                    }
                    item(key = "all-eps") {
                        Row(
                            Modifier.padding(start = 16.dp, end = 16.dp, top = 6.dp).fillMaxWidth().height(48.dp)
                                .clip(ZonaShapes.Pill).background(ZonaColors.Surface2).clickable(role = Role.Button) {},
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Все ${eps.size} серий", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            ZIcon(ZIcons.ChevronRight, size = 16.dp)
                        }
                    }
                    item(key = "scenes-inline") { ScenesRow(nav, Modifier.padding(top = 18.dp)) }
                    item(key = "cast") { CastRow(Modifier.padding(top = 18.dp)) }
                }
                CardTab.Scenes -> item(key = "scenes") { ScenesRow(nav, Modifier.padding(top = 12.dp)) }
                CardTab.About -> {
                    item(key = "about") { AboutTable(title) }
                    item(key = "cast-about") { CastRow(Modifier.padding(top = 18.dp)) }
                }
                CardTab.Similar -> item(key = "similar") { SimilarGrid(nav) }
            }
        }

        Row(
            Modifier.statusBarsPadding().padding(horizontal = 12.dp, vertical = 4.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RoundIconButton(ZIcons.Back, "Назад", { nav.back() })
            Spacer(Modifier.weight(1f))
            RoundIconButton(ZIcons.Cast, "Транслировать на ТВ", {})
            RoundIconButton(ZIcons.Share, "Поделиться", {})
        }
    }
}

@Composable
private fun Hero(title: Title) {
    Box(Modifier.fillMaxWidth().height(600.dp)) {
        Artwork(title.art.wide, Modifier.fillMaxSize(), alignX = -.5f)
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(0f to Color(0x9907090B), .2f to Color.Transparent, .4f to Color.Transparent, .76f to Color(0xE607090B), 1f to ZonaColors.Bg)
            )
        )
        Row(
            Modifier.padding(start = 16.dp, top = 300.dp).height(28.dp).glass().padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(ZonaColors.Blue))
            Spacer(Modifier.width(6.dp))
            Text("Трейлер · 0:48", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                title.name.uppercase().replace(" ", "\n"),
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontFamily = Zona.fonts.of(title.lettering.face), fontWeight = FontWeight.Bold,
                    fontSize = 54.sp, lineHeight = 50.sp, letterSpacing = .04.em, color = title.lettering.color,
                ),
                modifier = Modifier.semantics { contentDescription = title.name },
            )
            Text(
                "${title.original} · ${title.kind} · ${title.year} · ${title.country} · ${title.genres}",
                modifier = Modifier.padding(top = 12.dp),
                color = ZonaColors.TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center,
            )
            Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                RatingPill("КП", title.kp, ZonaColors.Amber)
                RatingPill("IMDb", title.imdb)
                RatingPill("Zona", title.zona)
                RatingPill(null, title.age)
            }
        }
    }
}

@Composable
private fun RatingPill(label: String?, value: String, valueColor: Color = ZonaColors.Text) {
    Row(
        Modifier.height(28.dp).clip(ZonaShapes.Pill).background(ZonaColors.Surface2).padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (label != null) {
            Text(label, fontSize = 12.sp, color = ZonaColors.TextMuted)
            Spacer(Modifier.width(5.dp))
        }
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.Action(icon: ImageVector, label: String, active: Boolean, onClick: () -> Unit) {
    Column(
        Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).clickable(role = Role.Button, onClick = onClick).padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier.size(52.dp).clip(CircleShape).background(if (active) ZonaColors.BlueTint else ZonaColors.Surface2),
            contentAlignment = Alignment.Center,
        ) { ZIcon(icon, tint = if (active) ZonaColors.Blue else ZonaColors.Text) }
        Text(
            label,
            modifier = Modifier.padding(top = 6.dp),
            fontSize = 12.sp,
            color = if (active) ZonaColors.Text else ZonaColors.TextMuted,
            maxLines = 1,
        )
    }
}

@Composable
private fun EpisodeRow(title: Title, e: Episode, onPlay: () -> Unit) {
    val current = e.progress != null
    Row(
        Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onPlay).padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(144.dp).height(81.dp).clip(RoundedCornerShape(12.dp)).background(ZonaColors.Surface2)) {
            val art = listOf(title.art, Demo.forest.art, Demo.harbor.art)[e.number % 3]
            Artwork(art.wide, Modifier.fillMaxSize(), alignX = ((e.number * 37) % 100) / 50f - 1f, alpha = if (e.watched) .5f else 1f)
            if (current) {
                Box(Modifier.align(Alignment.Center).size(34.dp).glass(CircleShape), contentAlignment = Alignment.Center) { ZIcon(ZIcons.Play, size = 12.dp) }
                ProgressLine(e.progress ?: 0f, Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(start = 8.dp, end = 8.dp, bottom = 6.dp))
            }
            if (e.watched) {
                Box(
                    Modifier.align(Alignment.TopEnd).padding(6.dp).height(20.dp).clip(ZonaShapes.Pill).background(Color(0xCC07090B)).padding(horizontal = 6.dp),
                    contentAlignment = Alignment.Center,
                ) { Text("Просмотрено", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ZonaColors.TextSecondary) }
            }
        }
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text("${e.number}. Серия ${e.number}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = if (e.watched) ZonaColors.TextMuted else ZonaColors.Text)
            Text(
                when {
                    current -> "Вы остановились · ещё 23 мин"
                    e.upcoming -> "Выйдет ${e.date}"
                    else -> "${e.duration} · ${e.date}"
                },
                modifier = Modifier.padding(top = 4.dp),
                fontSize = 12.sp,
                color = if (current) ZonaColors.Link else ZonaColors.TextMuted,
            )
        }
        RoundIconButton(
            if (e.upcoming) ZIcons.Bell else ZIcons.Download,
            if (e.upcoming) "Напомнить о серии ${e.number}" else "Скачать серию ${e.number}",
            {}, glass = false, tint = ZonaColors.TextMuted,
        )
    }
}

@Composable
private fun ScenesRow(nav: Navigator, modifier: Modifier = Modifier) {
    Column(modifier) {
        SectionHeader("Сцены", action = "Все 12")
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 6.dp)) {
            items(Demo.scenes) { c ->
                Box(Modifier.width(112.dp).height(200.dp).clip(RoundedCornerShape(14.dp)).clickable(role = Role.Button) { nav.open(Screen.Card(c.title.id)) }) {
                    Artwork(c.title.art.poster, Modifier.fillMaxSize(), alignX = c.alignX)
                    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(.5f to Color.Transparent, 1f to Color(0xE007090B))))
                    Box(Modifier.padding(8.dp).height(22.dp).glass().padding(horizontal = 8.dp), contentAlignment = Alignment.Center) {
                        Text(c.duration, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(c.kind, Modifier.align(Alignment.BottomStart).padding(10.dp), fontSize = 13.sp, lineHeight = 17.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CastRow(modifier: Modifier = Modifier) {
    Column(modifier) {
        SectionHeader("Актёры", action = "Все")
        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.padding(top = 8.dp)) {
            items(Demo.cast) { a ->
                Column(Modifier.width(74.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier.size(70.dp).clip(CircleShape).background(Brush.linearGradient(listOf(a.from, a.to))),
                        contentAlignment = Alignment.Center,
                    ) { Text(a.initials, style = Zona.heading.copy(fontSize = 22.sp)) }
                    Text(a.name, Modifier.padding(top = 8.dp), fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
                    Text(a.role, Modifier.padding(top = 2.dp), fontSize = 11.sp, color = ZonaColors.TextMuted)
                }
            }
        }
    }
}

@Composable
private fun AboutTable(title: Title) {
    val rows = listOf(
        "Страна" to title.country,
        "Жанр" to title.genres,
        "Режиссёр" to "Имя Фамилия",
        "Премьера" to "12 марта ${title.year} в мире",
        "Длительность" to if (title.isSeries) "50 мин. · серия" else "1 ч 52 мин",
    )
    Column(Modifier.padding(16.dp).clip(ZonaShapes.Card).background(ZonaColors.Surface1)) {
        rows.forEachIndexed { i, (k, v) ->
            if (i > 0) HorizontalDivider(color = Color(0x0FFFFFFF))
            Row(Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(k, color = ZonaColors.TextMuted, fontSize = 14.sp, modifier = Modifier.weight(1f))
                Text(v, fontSize = 14.sp)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SimilarGrid(nav: Navigator) {
    FlowRow(
        Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        maxItemsInEachRow = 3,
    ) {
        Demo.similar.forEach { t ->
            Poster(t, { nav.open(Screen.Card(t.id)) }, Modifier.weight(1f).height(168.dp), lettering = .85f)
        }
    }
}

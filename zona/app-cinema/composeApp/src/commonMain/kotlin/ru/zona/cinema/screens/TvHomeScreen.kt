package ru.zona.cinema.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.gestures.BringIntoViewSpec
import androidx.compose.foundation.gestures.LocalBringIntoViewSpec
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import ru.zona.cinema.Navigator
import ru.zona.cinema.Screen
import ru.zona.cinema.data.Demo
import ru.zona.cinema.data.Progress
import ru.zona.cinema.data.Title
import ru.zona.cinema.theme.TitleFace
import ru.zona.cinema.theme.Zona
import ru.zona.cinema.theme.ZonaColors
import ru.zona.cinema.theme.ZonaShapes
import ru.zona.cinema.ui.Artwork
import ru.zona.cinema.ui.PosterTitle
import ru.zona.cinema.ui.ZIcon
import ru.zona.cinema.ui.ZIcons
import ru.zona.cinema.ui.bottomFade
import ru.zona.cinema.ui.focusRing
import ru.zona.cinema.ui.glass

/** Left-rail sections of the TV app (tv-ref.dc.html). */
enum class TvSection(val label: String, val icon: ImageVector) {
    Search("Поиск", ZIcons.Search),
    Home("Главная", ZIcons.Home),
    Movies("Фильмы", ZIcons.Film),
    Series("Сериалы", ZIcons.Series),
    Channels("ТВ-каналы", ZIcons.Channels),
    Sport("Спорт", ZIcons.Sport),
    My("Моё", ZIcons.Bookmark),
    Profile("Профиль", ZIcons.Profile),
}

private class TvRow(val title: String, val items: List<TvItem>)
private class TvItem(val title: Title, val progress: Progress? = null)

private fun rowsFor(section: TvSection): List<TvRow> {
    val series = Demo.all.filter { it.isSeries }
    val movies = Demo.all.filterNot { it.isSeries }
    val popular = listOf(Demo.north, Demo.harbor, Demo.forest, Demo.flight, Demo.neon, Demo.dunes, Demo.peaks)
    val resume = TvRow("Продолжить просмотр", Demo.continueWatching.map { TvItem(it.title, it) })
    fun row(title: String, titles: List<Title>) = TvRow(title, titles.map { TvItem(it) })
    return when (section) {
        TvSection.Home -> listOf(row("Популярные сериалы", popular), resume, row("Новинки кино", Demo.freshMovies), row("Топ-10 недели", Demo.top10))
        TvSection.Movies -> listOf(row("Новинки кино", Demo.freshMovies), row("Все фильмы", movies))
        TvSection.Series -> listOf(row("Популярные сериалы", series), row("Похожие", Demo.similar.filter { it.isSeries }))
        TvSection.My -> listOf(resume, row("Избранное", Demo.similar))
        else -> emptyList()
    }
}

/** Distance from a row's top to its posters (heading + gap), so a focused 1.1× poster still fits on screen. */
private val PosterTop = 32.dp

/**
 * Vertical focus moves scroll the rows so the focused row sits right under the hero
 * (its posters at [PosterTop]) instead of the default "just make it visible".
 */
private class RowTopSpec(private val posterTopPx: Float) : BringIntoViewSpec {
    override fun calculateScrollDistance(offset: Float, size: Float, containerSize: Float) = offset - posterTopPx
}

/**
 * Android TV home (tv-ref.dc.html), 960×540dp. The hero shows whichever poster has focus;
 * the D-pad moves between the rail and the rows, and the focused row slides to the top.
 */
@OptIn(ExperimentalFoundationApi::class) // LocalBringIntoViewSpec
@Composable
fun TvHomeScreen(nav: Navigator) {
    var section by rememberSaveable { mutableStateOf(TvSection.Home) }
    val rows = remember(section) { rowsFor(section) }
    var focused by remember(section) { mutableStateOf(rows.firstOrNull()?.items?.first() ?: TvItem(Demo.north)) }
    val firstPoster = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // Focus the first poster once, when it is first laid out (rows are lazy).
    var initialFocus by remember { mutableStateOf(true) }

    Box(
        Modifier.fillMaxSize().background(ZonaColors.Bg)
            // Arrow keys move focus on desktop too; on Android the D-pad does this already.
            .onKeyEvent { e ->
                if (e.type != KeyEventType.KeyDown) return@onKeyEvent false
                val dir = when (e.key) {
                    Key.DirectionUp -> FocusDirection.Up
                    Key.DirectionDown -> FocusDirection.Down
                    Key.DirectionLeft -> FocusDirection.Left
                    Key.DirectionRight -> FocusDirection.Right
                    else -> return@onKeyEvent false
                }
                focusManager.moveFocus(dir)
            },
    ) {
        Crossfade(focused.title, Modifier.align(Alignment.TopEnd).size(760.dp, 430.dp), label = "backdrop") { t ->
            Box(Modifier.fillMaxSize()) {
                Artwork(t.art.wide, Modifier.fillMaxSize(), alignX = -.6f)
                Box(
                    Modifier.fillMaxSize().background(
                        Brush.horizontalGradient(0f to ZonaColors.Bg, .35f to Color(0x8C07090B), .65f to Color.Transparent),
                    ),
                )
            }
        }
        Box(
            Modifier.fillMaxWidth().padding(top = 200.dp).height(340.dp)
                .background(Brush.verticalGradient(0f to Color.Transparent, .7f to ZonaColors.Bg)),
        )

        if (rows.isEmpty()) {
            EmptySection(section, Modifier.padding(start = 112.dp, top = 44.dp))
        } else {
            Crossfade(focused, Modifier.padding(start = 112.dp, top = 44.dp).width(440.dp), label = "hero") { Hero(it) }
            // A lazy list (not a plain Column) so the D-pad can reach rows below the screen edge.
            val rowSpec = RowTopSpec(with(LocalDensity.current) { PosterTop.toPx() })
            val defaultSpec = LocalBringIntoViewSpec.current
            CompositionLocalProvider(LocalBringIntoViewSpec provides rowSpec) {
                LazyColumn(Modifier.fillMaxSize().padding(top = 318.dp)) {
                    itemsIndexed(rows) { r, row ->
                        CompositionLocalProvider(LocalBringIntoViewSpec provides defaultSpec) {
                            TvPosterRow(
                                row,
                                firstFocus = if (r == 0 && initialFocus) firstPoster else null,
                                onFirstFocused = { initialFocus = false },
                                onFocus = { item -> focused = item },
                                onOpen = { item ->
                                    val p = item.progress
                                    nav.open(if (p != null && p.season > 0) Screen.Player(item.title.id, p.season, p.episode) else Screen.Player(item.title.id))
                                },
                            )
                        }
                    }
                }
            }
        }

        Rail(section, onSelect = { section = it }, modifier = Modifier.padding(16.dp).fillMaxHeight())
    }
}

@Composable
private fun Rail(selected: TvSection, onSelect: (TvSection) -> Unit, modifier: Modifier) {
    Column(
        modifier.width(60.dp).glass(RoundedCornerShape(30.dp)).padding(top = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(ZIcons.Logo, contentDescription = "Zona", tint = Color.Unspecified, modifier = Modifier.padding(bottom = 14.dp).size(26.dp, 27.dp))
        TvSection.entries.forEach { s ->
            val on = s == selected
            Box(
                Modifier.size(44.dp).focusRing(CircleShape).clip(CircleShape)
                    .background(if (on) Color(0x330094F6) else Color.Transparent)
                    .clickable(role = Role.Tab) { onSelect(s) }
                    .semantics { contentDescription = s.label; this.selected = on },
                contentAlignment = Alignment.Center,
            ) { ZIcon(s.icon, size = 20.dp, tint = if (on) ZonaColors.Blue else ZonaColors.TextMuted) }
        }
    }
}

@Composable
private fun Hero(item: TvItem) {
    val t = item.title
    val p = item.progress
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(ZonaColors.Blue))
            Spacer(Modifier.width(8.dp))
            Text(kicker(item).uppercase(), fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = .08.em)
        }
        Text(
            t.name.uppercase(),
            // Display faces carry tall ascent/descent that a tight lineHeight does not trim, so the
            // gaps around the name are smaller than in the mockup to land on the same layout.
            modifier = Modifier.padding(top = 2.dp).semantics { heading() },
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = TextStyle(
                fontFamily = Zona.fonts.of(t.lettering.face),
                fontSize = heroSize(t.lettering.face).sp,
                lineHeight = (heroSize(t.lettering.face) * .92f).sp,
                letterSpacing = .04.em,
                color = ZonaColors.Text,
            ),
        )
        Text(
            buildAnnotatedString {
                withStyle(SpanStyle(color = ZonaColors.Amber, fontWeight = FontWeight.Bold)) { append("КП ${t.kp}") }
                append(" · ${t.kind} · ${t.year} · ${t.genres} · ${t.age}")
            },
            fontSize = 15.sp,
            color = ZonaColors.TextSecondary,
        )
        Text(
            if (p != null) p.label else t.description,
            modifier = Modifier.padding(top = 10.dp),
            fontSize = 16.sp,
            lineHeight = 23.sp,
            color = ZonaColors.TextSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun kicker(item: TvItem) = when {
    item.progress != null -> "Продолжить просмотр"
    item.title.year >= 2025 -> if (item.title.isSeries) "Новый сезон" else "Премьера"
    else -> item.title.kind
}

/** Display faces differ in width; keep long names within two lines of the 440dp column. */
private fun heroSize(face: TitleFace) = when (face) {
    TitleFace.Oswald -> 62f
    TitleFace.Playfair -> 54f
    TitleFace.Russo -> 46f
    TitleFace.Unbounded -> 44f
}

@Composable
private fun TvPosterRow(
    row: TvRow,
    firstFocus: FocusRequester?,
    onFirstFocused: () -> Unit,
    onFocus: (TvItem) -> Unit,
    onOpen: (TvItem) -> Unit,
) {
    Column {
        Text(
            row.title,
            style = Zona.heading.copy(fontSize = 20.sp),
            maxLines = 1,
            modifier = Modifier.padding(start = 112.dp).height(PosterTop - 8.dp).semantics { heading() },
        )
        // Bottom padding leaves room for the focused poster's 1.1× scale and ring.
        LazyRow(
            contentPadding = PaddingValues(start = 112.dp, end = 64.dp, top = 8.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            itemsIndexed(row.items) { i, item ->
                if (i == 0 && firstFocus != null) {
                    LaunchedEffect(Unit) {
                        runCatching { firstFocus.requestFocus() }
                        onFirstFocused()
                    }
                }
                TvPoster(
                    item,
                    onFocus = { onFocus(item) },
                    onClick = { onOpen(item) },
                    modifier = if (i == 0 && firstFocus != null) Modifier.focusRequester(firstFocus) else Modifier,
                )
            }
        }
    }
}

@Composable
private fun TvPoster(item: TvItem, onFocus: () -> Unit, onClick: () -> Unit, modifier: Modifier = Modifier) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.1f else 1f, label = "scale")
    Box(
        modifier
            .size(112.dp, 168.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin(0f, 0f)
                shadowElevation = if (focused) 24.dp.toPx() else 0f
                shape = ZonaShapes.Poster
            }
            .focusRing(ZonaShapes.Poster, gap = 0.dp)
            .onFocusChanged { focused = it.isFocused; if (it.isFocused) onFocus() }
            .clip(ZonaShapes.Poster)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = item.title.name },
    ) {
        Artwork(item.title.art.poster, Modifier.fillMaxSize(), alpha = if (focused) 1f else .75f)
        Box(Modifier.fillMaxSize().bottomFade(.5f, Color(0xD907090B)))
        PosterTitle(item.title, Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(start = 5.dp, end = 5.dp, bottom = 9.dp), scale = .85f)
        item.progress?.let { p ->
            Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp).height(3.dp).clip(ZonaShapes.Pill).background(Color(0x40FFFFFF))) {
                Box(Modifier.fillMaxWidth(p.fraction).fillMaxHeight().background(ZonaColors.Blue))
            }
        }
    }
}

@Composable
private fun EmptySection(section: TvSection, modifier: Modifier) {
    Column(modifier.width(440.dp)) {
        Text(section.label, style = Zona.screenTitle.copy(fontSize = 40.sp), modifier = Modifier.semantics { heading() })
        Text(
            "Раздел появится вместе с подключением каталога Zona. Пока доступны Главная, Фильмы, Сериалы и Моё.",
            modifier = Modifier.padding(top = 12.dp),
            fontSize = 16.sp, lineHeight = 23.sp, color = ZonaColors.TextSecondary,
        )
    }
}

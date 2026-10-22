package ru.zona.cinema.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.zona.cinema.Navigator
import ru.zona.cinema.Screen
import ru.zona.cinema.data.Catalog
import ru.zona.cinema.data.CatalogPage
import ru.zona.cinema.data.Library
import ru.zona.cinema.data.api.Filter
import ru.zona.cinema.ui.ErrorBlock
import ru.zona.cinema.ui.Load
import ru.zona.cinema.ui.LoadingBlock
import ru.zona.cinema.ui.rememberLoad
import ru.zona.cinema.theme.Zona
import ru.zona.cinema.theme.ZonaColors
import ru.zona.cinema.theme.ZonaShapes
import ru.zona.cinema.ui.Artwork
import ru.zona.cinema.ui.Poster
import ru.zona.cinema.ui.PrimaryButton
import ru.zona.cinema.ui.RoundIconButton
import ru.zona.cinema.ui.ZChip
import ru.zona.cinema.ui.ZIcons

private val sections = listOf("Фильмы", "Сериалы", "ТВ", "Спорт")
private val sorts = listOf("Популярные", "Новые", "По рейтингу")
private val allGenres = listOf("Драма", "Комедия", "Триллер", "Фантастика", "Детектив", "Боевик", "Приключения", "Мультфильм", "Ужасы", "Мелодрама")
private const val PAGE = 30

/** "фильм / фильма / фильмов" for [n]. */
private fun plural(n: Int, forms: List<String>): String {
    val mod100 = n % 100
    val mod10 = n % 10
    return when {
        mod100 in 11..14 -> forms[2]
        mod10 == 1 -> forms[0]
        mod10 in 2..4 -> forms[1]
        else -> forms[2]
    }
}

/** Filter state shared by the catalogue and its filter sheet. */
class CatalogFilters {
    var genres by mutableStateOf(emptySet<String>())
    var country by mutableStateOf("Все")
    var fromYear by mutableStateOf(1990)
    var rating by mutableStateOf("Любой")
    var age by mutableStateOf("Все")
    var hideWatched by mutableStateOf(false)
    var goodQuality by mutableStateOf(false)
    var hideUnavailable by mutableStateOf(true)

    val count: Int get() = genres.size + (if (fromYear > 1990) 1 else 0) + (if (rating != "Любой") 1 else 0) +
        (if (country != "Все") 1 else 0) + (if (age != "Все") 1 else 0)

    /** The API filter for these choices; age and the toggles have no API field yet. */
    fun toApi(sort: Int) = Filter(
        sortBy = listOf("popularity", "release_date", "rating")[sort],
        genres = genres.mapNotNull { Catalog.genreId(it) }.joinToString("|").ifEmpty { null },
        countries = Catalog.countryId(country)?.takeIf { country != "Все" },
        yearFrom = fromYear.takeIf { it > 1990 },
        ratingFrom = rating.removeSuffix("+").toDoubleOrNull(),
    )

    fun reset() {
        genres = emptySet(); country = "Все"; fromYear = 1990; rating = "Любой"; age = "Все"
        hideWatched = false; goodQuality = false; hideUnavailable = true
    }
}

@Composable
fun CatalogScreen(nav: Navigator, filtersInitiallyOpen: Boolean = false) {
    var section by remember { mutableStateOf(0) }
    var sort by remember { mutableStateOf(0) }
    var filtersOpen by remember { mutableStateOf(filtersInitiallyOpen) }
    val filters = remember { CatalogFilters() }
    val filter = filters.toApi(sort)
    // Pages grow by 30 as the grid nears its end; cached titles make the re-fetch cheap.
    var limit by remember(section, filter) { mutableIntStateOf(PAGE) }
    val page = rememberLoad(section, filter, limit) {
        if (section > 1) CatalogPage(emptyList(), 0) else Catalog.browse(series = section == 1, filter = filter, limit = limit)
    }
    var shown by remember(section, filter) { mutableStateOf<CatalogPage?>(null) }
    LaunchedEffect(page.state) { page.value?.let { shown = it } }
    val items = shown?.titles.orEmpty()
    val grid = rememberLazyGridState()
    val nearEnd by remember { derivedStateOf { grid.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 } }
    LaunchedEffect(nearEnd, shown) {
        val p = shown ?: return@LaunchedEffect
        if (nearEnd >= p.titles.size - 6 && p.titles.size < p.total && page.state is Load.Ready) limit += PAGE
    }

    Box(Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            state = grid,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 112.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item(span = { GridItemSpan(3) }) {
                Column(Modifier.statusBarsPadding()) {
                    Row(Modifier.fillMaxWidth().height(56.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Каталог", style = Zona.screenTitle, modifier = Modifier.weight(1f))
                        RoundIconButton(ZIcons.Search, "Поиск", { nav.tab(ru.zona.cinema.ui.Tab.Search) }, glass = false, iconSize = 22.dp)
                    }
                    Row(Modifier.padding(top = 4.dp).fillMaxWidth().clip(ZonaShapes.Pill).background(ZonaColors.Surface1).padding(4.dp)) {
                        sections.forEachIndexed { i, label ->
                            val on = i == section
                            Box(
                                Modifier.weight(1f).height(40.dp).clip(ZonaShapes.Pill)
                                    .background(if (on) Color.White else Color.Transparent)
                                    .clickable(role = Role.Tab) { section = i }.semantics { selected = on },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(label, fontSize = 14.sp, fontWeight = if (on) FontWeight.Bold else FontWeight.Medium, color = if (on) ZonaColors.Bg else ZonaColors.TextMuted)
                            }
                        }
                    }
                    LazyRow(Modifier.padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(Library.genres.take(4)) { g ->
                            Box(
                                Modifier.width(132.dp).height(64.dp).clip(RoundedCornerShape(14.dp))
                                    .clickable(role = Role.Button) { filters.genres = setOf(g.filter) },
                            ) {
                                Artwork(g.art.wide, Modifier.fillMaxSize(), alignX = g.alignX)
                                Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(0xD907090B), Color(0x1A07090B)))))
                                Text(g.name, style = Zona.heading.copy(fontSize = 17.sp), modifier = Modifier.align(Alignment.BottomStart).padding(12.dp))
                            }
                        }
                    }
                    LazyRow(Modifier.padding(top = 12.dp, bottom = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            ZChip("Фильтры", { filtersOpen = true }, leading = ZIcons.Filters)
                        }
                        item { ZChip(sorts[sort], { sort = (sort + 1) % sorts.size }, trailing = ZIcons.ChevronDown) }
                        items(filters.genres.toList()) { g ->
                            ActiveFilterChip(g) { filters.genres = filters.genres - g }
                        }
                        if (filters.country != "Все") item { ActiveFilterChip(filters.country) { filters.country = "Все" } }
                        if (filters.fromYear > 1990) item { ActiveFilterChip("с ${filters.fromYear}") { filters.fromYear = 1990 } }
                        if (filters.rating != "Любой") item { ActiveFilterChip("КП ${filters.rating}") { filters.rating = "Любой" } }
                    }
                }
            }
            if (section > 1) {
                item(span = { GridItemSpan(3) }) {
                    Text(
                        "${sections[section]} пока нет: эти разделы работают через другие методы API, их подключим следующими.",
                        style = Zona.meta, modifier = Modifier.padding(vertical = 24.dp),
                    )
                }
            }
            items(items, key = { it.id }) { t ->
                Column {
                    Poster(t, { nav.open(Screen.Card(t.id)) }, Modifier.fillMaxWidth().height(168.dp), lettering = .8f)
                    Text(
                        listOf(t.year.takeIf { it > 0 }?.toString(), t.genres.ifEmpty { null }).filterNotNull().joinToString(" · "),
                        style = Zona.meta.copy(fontSize = 12.sp), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
            when (val st = page.state) {
                Load.Loading -> item(span = { GridItemSpan(3) }) { LoadingBlock(Modifier.fillMaxWidth().height(120.dp)) }
                is Load.Failed -> item(span = { GridItemSpan(3) }) { ErrorBlock(st.message, page.retry, Modifier.fillMaxWidth()) }
                is Load.Ready -> if (section <= 1 && items.isEmpty()) item(span = { GridItemSpan(3) }) {
                    Text("Ничего не нашлось. Попробуйте ослабить фильтры.", style = Zona.meta, modifier = Modifier.padding(vertical = 24.dp))
                }
            }
        }

        AnimatedVisibility(filtersOpen, enter = fadeIn(), exit = fadeOut()) {
            Box(
                Modifier.fillMaxSize().background(Color(0x9907090B))
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { filtersOpen = false },
            )
        }
        AnimatedVisibility(
            filtersOpen,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
        ) {
            FiltersSheet(filters, resultCount = shown?.total, series = section == 1, onClose = { filtersOpen = false })
        }
    }
}

@Composable
private fun ActiveFilterChip(text: String, onRemove: () -> Unit) {
    Row(
        Modifier.height(36.dp).clip(ZonaShapes.Pill).background(ZonaColors.BlueTint)
            .clickable(role = Role.Button, onClickLabel = "Убрать фильтр $text", onClick = onRemove).padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.width(6.dp))
        ru.zona.cinema.ui.ZIcon(ZIcons.Close, size = 14.dp)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FiltersSheet(f: CatalogFilters, resultCount: Int?, series: Boolean, onClose: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().statusBarsPadding().padding(top = 40.dp)
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)).background(ZonaColors.Surface1)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
            .navigationBarsPadding().padding(horizontal = 16.dp),
    ) {
        Box(Modifier.padding(top = 10.dp).size(36.dp, 4.dp).clip(ZonaShapes.Pill).background(ZonaColors.Outline).align(Alignment.CenterHorizontally))
        Row(Modifier.fillMaxWidth().height(52.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Фильтры", style = Zona.heading.copy(fontSize = 24.sp), modifier = Modifier.weight(1f))
            Text(
                "Сбросить",
                modifier = Modifier.clip(ZonaShapes.Pill).clickable(role = Role.Button) { f.reset() }.padding(12.dp),
                color = ZonaColors.Link, fontSize = 15.sp, fontWeight = FontWeight.Medium,
            )
        }
        Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
            FilterLabel("ЖАНРЫ", if (f.genres.isEmpty()) "любые" else "${f.genres.size} выбрано")
            FlowRow(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                allGenres.forEach { g ->
                    ZChip(g, { f.genres = if (g in f.genres) f.genres - g else f.genres + g }, selected = g in f.genres)
                }
            }
            FilterLabel("СТРАНА", null, Modifier.padding(top = 20.dp))
            FlowRow(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Все", "Россия", "США", "Корея").forEach { c -> ZChip(c, { f.country = c }, selected = f.country == c) }
            }
            FilterLabel("ГОДЫ ВЫХОДА", if (f.fromYear > 1990) "с ${f.fromYear}" else "любые", Modifier.padding(top = 20.dp))
            Segmented(listOf("Любые", "2000+", "2010+", "2020+"), when (f.fromYear) { 2000 -> 1; 2010 -> 2; 2020 -> 3; else -> 0 }) {
                f.fromYear = listOf(1990, 2000, 2010, 2020)[it]
            }
            FilterLabel("РЕЙТИНГ КП", null, Modifier.padding(top = 18.dp))
            val ratings = listOf("Любой", "6+", "7+", "8+")
            Segmented(ratings, ratings.indexOf(f.rating)) { f.rating = ratings[it] }
            FilterLabel("ВОЗРАСТ", null, Modifier.padding(top = 18.dp))
            val ages = listOf("Все", "6+", "12+", "16+", "18+")
            Segmented(ages, ages.indexOf(f.age)) { f.age = ages[it] }
            Spacer(Modifier.height(10.dp))
            Toggle("Скрывать просмотренное", f.hideWatched) { f.hideWatched = it }
            Toggle("Только хорошее качество", f.goodQuality) { f.goodQuality = it }
            Toggle("Скрывать недоступные", f.hideUnavailable) { f.hideUnavailable = it }
        }
        PrimaryButton(
            if (resultCount == null) "Показать" else "Показать $resultCount " + plural(resultCount, if (series) listOf("сериал", "сериала", "сериалов") else listOf("фильм", "фильма", "фильмов")),
            onClose, Modifier.fillMaxWidth().padding(vertical = 16.dp), icon = null, height = 54.dp)
    }
}

@Composable
private fun FilterLabel(text: String, value: String?, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().padding(top = 4.dp)) {
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ZonaColors.TextMuted, letterSpacing = .9.sp, modifier = Modifier.weight(1f))
        if (value != null) Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun Segmented(options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    Row(Modifier.padding(top = 10.dp).fillMaxWidth().clip(ZonaShapes.Pill).background(ZonaColors.Surface2).padding(3.dp)) {
        options.forEachIndexed { i, o ->
            val on = i == selected
            Box(
                Modifier.weight(1f).height(36.dp).clip(ZonaShapes.Pill).background(if (on) Color.White else Color.Transparent)
                    .clickable(role = Role.RadioButton) { onSelect(i) }.semantics { this.selected = on },
                contentAlignment = Alignment.Center,
            ) { Text(o, fontSize = 14.sp, fontWeight = if (on) FontWeight.Bold else FontWeight.Medium, color = if (on) ZonaColors.Bg else ZonaColors.TextSecondary) }
        }
    }
}

@Composable
fun Toggle(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(48.dp).clickable(role = Role.Switch) { onChange(!checked) }.semantics { selected = checked },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 15.sp, modifier = Modifier.weight(1f))
        Box(
            Modifier.size(52.dp, 32.dp).clip(ZonaShapes.Pill).background(if (checked) ZonaColors.Blue else ZonaColors.Surface3).padding(4.dp),
            contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
        ) { Box(Modifier.size(24.dp).clip(CircleShape).background(if (checked) Color.White else ZonaColors.TextMuted)) }
    }
}

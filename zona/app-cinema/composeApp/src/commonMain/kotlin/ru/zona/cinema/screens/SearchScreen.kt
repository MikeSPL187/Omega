package ru.zona.cinema.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.compose.ui.unit.sp
import ru.zona.cinema.Navigator
import ru.zona.cinema.Screen
import ru.zona.cinema.data.Catalog
import ru.zona.cinema.data.Library
import ru.zona.cinema.theme.Zona
import ru.zona.cinema.theme.ZonaColors
import ru.zona.cinema.theme.ZonaShapes
import ru.zona.cinema.ui.Artwork
import ru.zona.cinema.ui.ErrorBlock
import ru.zona.cinema.ui.Load
import ru.zona.cinema.ui.LoadingBlock
import ru.zona.cinema.ui.TitleArt
import ru.zona.cinema.ui.rememberLoad
import ru.zona.cinema.ui.RoundIconButton
import ru.zona.cinema.ui.Tab
import ru.zona.cinema.ui.ZIcon
import ru.zona.cinema.ui.ZIcons

@Composable
fun SearchScreen(nav: Navigator, initialQuery: String = "") {
    var query by remember { mutableStateOf(initialQuery) }
    var recent by remember { mutableStateOf(Library.recentQueries) }
    val q = query.trim()
    // Search as the user types, once they pause for a moment.
    val search = rememberLoad(q) {
        if (q.length < 2) emptyList() else {
            delay(400)
            Catalog.search(q)
        }
    }
    val results = search.value.orEmpty()

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 112.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(span = { GridItemSpan(2) }) {
            Column(Modifier.statusBarsPadding()) {
                Box(Modifier.height(56.dp), contentAlignment = Alignment.CenterStart) { Text("Поиск", style = Zona.screenTitle) }
                Row(
                    Modifier.fillMaxWidth().height(52.dp).clip(ZonaShapes.Pill).background(ZonaColors.Surface2).padding(start = 18.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ZIcon(ZIcons.Search, size = 20.dp, tint = ZonaColors.TextMuted)
                    Box(Modifier.weight(1f).padding(horizontal = 10.dp), contentAlignment = Alignment.CenterStart) {
                        if (query.isEmpty()) Text("Фильмы, сериалы, актёры", color = ZonaColors.TextMuted, fontSize = 16.sp)
                        BasicTextField(
                            value = query,
                            onValueChange = { query = it },
                            singleLine = true,
                            textStyle = TextStyle(color = ZonaColors.Text, fontSize = 16.sp, fontFamily = Zona.fonts.body),
                            cursorBrush = SolidColor(ZonaColors.Blue),
                            modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Поиск фильмов, сериалов и актёров" },
                        )
                    }
                    if (query.isEmpty()) {
                        RoundIconButton(ZIcons.Mic, "Голосовой поиск", {}, glass = false, tint = ZonaColors.Text)
                    } else {
                        RoundIconButton(ZIcons.Close, "Очистить", { query = "" }, glass = false, tint = ZonaColors.TextMuted, iconSize = 18.dp)
                    }
                }
            }
        }

        if (q.isNotEmpty()) {
            item(span = { GridItemSpan(2) }) {
                when (val st = search.state) {
                    Load.Loading -> LoadingBlock(Modifier.fillMaxWidth().height(96.dp))
                    is Load.Failed -> ErrorBlock(st.message, search.retry, Modifier.fillMaxWidth())
                    is Load.Ready -> Text(
                        when {
                            q.length < 2 -> "Введите хотя бы две буквы"
                            results.isEmpty() -> "Ничего не нашлось по запросу «$q»"
                            else -> "Найдено: ${results.size}"
                        },
                        style = Zona.meta, modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
                    )
                }
            }
            items(results, span = { GridItemSpan(2) }) { t ->
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).clickable(role = Role.Button) {
                        recent = (listOf(query.trim()) + recent).distinct().take(5)
                        nav.open(Screen.Card(t.id))
                    }.padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.width(64.dp).height(96.dp).clip(RoundedCornerShape(10.dp))) { TitleArt(t, wide = false, modifier = Modifier.fillMaxSize()) }
                    Column(Modifier.weight(1f).padding(start = 14.dp)) {
                        Text(t.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(
                            listOf(t.original, t.year.takeIf { it > 0 }?.toString().orEmpty()).filter { it.isNotEmpty() }.joinToString(" · "),
                            style = Zona.meta, modifier = Modifier.padding(top = 3.dp), maxLines = 1, overflow = TextOverflow.Ellipsis,
                        )
                        Row(Modifier.padding(top = 6.dp)) {
                            if (t.kp.isNotEmpty()) Text("КП ${t.kp}  ·  ", color = ZonaColors.Amber, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(listOf(t.kind, t.genres).filter { it.isNotEmpty() }.joinToString(", "), style = Zona.meta, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        } else {
            if (recent.isNotEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    Row(Modifier.fillMaxWidth().padding(top = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Недавние", style = Zona.heading.copy(fontSize = 20.sp), modifier = Modifier.weight(1f))
                        Text(
                            "Очистить",
                            Modifier.clip(ZonaShapes.Pill).clickable(role = Role.Button) { recent = emptyList() }.padding(12.dp),
                            color = ZonaColors.Link, fontSize = 14.sp, fontWeight = FontWeight.Medium,
                        )
                    }
                }
                items(recent, span = { GridItemSpan(2) }) { r ->
                    Row(
                        Modifier.fillMaxWidth().height(44.dp).clip(ZonaShapes.Pill).clickable(role = Role.Button) { query = r },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ZIcon(ZIcons.History, size = 18.dp, tint = ZonaColors.TextMuted)
                        Text(r, fontSize = 15.sp, modifier = Modifier.weight(1f).padding(start = 12.dp))
                        RoundIconButton(ZIcons.Close, "Удалить запрос «$r»", { recent = recent - r }, glass = false, tint = ZonaColors.TextMuted, iconSize = 16.dp)
                    }
                }
            }
            item(span = { GridItemSpan(2) }) {
                Text("Жанры", style = Zona.heading.copy(fontSize = 20.sp), modifier = Modifier.padding(top = 14.dp, bottom = 4.dp))
            }
            items(Library.genres) { g ->
                Box(Modifier.height(84.dp).clip(RoundedCornerShape(16.dp)).clickable(role = Role.Button) { nav.tab(Tab.Catalog) }) {
                    Artwork(g.art.wide, Modifier.fillMaxSize(), alignX = g.alignX)
                    Box(Modifier.fillMaxSize().background(Brush.horizontalGradient(listOf(Color(0xD107090B), Color(0x0D07090B)))))
                    Text(g.name, style = Zona.heading.copy(fontSize = 19.sp), modifier = Modifier.align(Alignment.BottomStart).padding(14.dp))
                }
            }
        }
    }
}

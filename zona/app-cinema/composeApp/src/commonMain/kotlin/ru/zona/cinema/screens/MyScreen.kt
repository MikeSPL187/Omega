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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.zona.cinema.Navigator
import ru.zona.cinema.Screen
import ru.zona.cinema.data.Demo
import ru.zona.cinema.data.DownloadState
import ru.zona.cinema.data.Library
import ru.zona.cinema.theme.Zona
import ru.zona.cinema.theme.ZonaColors
import ru.zona.cinema.theme.ZonaShapes
import ru.zona.cinema.ui.Artwork
import ru.zona.cinema.ui.Poster
import ru.zona.cinema.ui.ProgressStill
import ru.zona.cinema.ui.RoundIconButton
import ru.zona.cinema.ui.SectionHeader
import ru.zona.cinema.ui.ZChip
import ru.zona.cinema.ui.ZIcon
import ru.zona.cinema.ui.ZIcons

private enum class MyFilter(val label: String) { All("Всё"), Favorites("Избранное"), Downloads("Загрузки"), History("История") }

@Composable
fun MyScreen(nav: Navigator) {
    var filter by remember { mutableStateOf(MyFilter.All) }
    val downloads = Library.downloads
    val showNow = filter == MyFilter.All || filter == MyFilter.History
    val showFav = filter == MyFilter.All || filter == MyFilter.Favorites
    val showDl = filter == MyFilter.All || filter == MyFilter.Downloads

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 112.dp)) {
        item {
            Column(Modifier.statusBarsPadding()) {
                Row(Modifier.fillMaxWidth().height(56.dp).padding(start = 16.dp, end = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Моё", style = Zona.screenTitle, modifier = Modifier.weight(1f))
                    RoundIconButton(ZIcons.Edit, "Редактировать списки", {}, glass = false)
                }
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(MyFilter.entries) { f ->
                        val label = if (f == MyFilter.Downloads) "${f.label} · ${downloads.size}" else f.label
                        ZChip(label, { filter = f }, selected = f == filter)
                    }
                }
            }
        }
        if (showNow) item {
            Column(Modifier.padding(top = 12.dp)) {
                SectionHeader(if (filter == MyFilter.History) "История" else "Смотрю сейчас")
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 4.dp)) {
                    items(Demo.continueWatching) { p ->
                        ProgressStill(
                            p.title.art.wide, p.title.name, p.label, p.fraction,
                            onClick = { nav.open(Screen.Player(p.title.id, p.season.coerceAtLeast(1), p.episode.coerceAtLeast(1))) },
                            modifier = Modifier.width(232.dp).height(130.dp),
                        )
                    }
                }
            }
        }
        if (showFav) item {
            Column(Modifier.padding(top = 14.dp)) {
                SectionHeader("Избранное", count = "${Library.favorites.size}", action = "Все", onAction = { filter = MyFilter.Favorites })
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 4.dp)) {
                    items(Library.favorites) { t ->
                        Poster(
                            t, { nav.open(Screen.Card(t.id)) }, Modifier.width(104.dp).height(156.dp), showRating = false, lettering = .8f,
                            badge = if (t.id in Library.freshEpisodes) {
                                {
                                    Box(
                                        Modifier.padding(6.dp).height(20.dp).clip(RoundedCornerShape(6.dp)).background(ZonaColors.Blue).padding(horizontal = 7.dp),
                                        contentAlignment = Alignment.Center,
                                    ) { Text("+1 серия", color = ZonaColors.OnBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                                }
                            } else null,
                        )
                    }
                }
            }
        }
        if (showDl) {
            item {
                SectionHeader("Загрузки", count = "2,4 ГБ", action = "Все", onAction = { nav.open(Screen.Downloads) }, modifier = Modifier.padding(top = 14.dp))
            }
            items(downloads.filter { it.state == DownloadState.Done || it.state == DownloadState.Running }) { d ->
                Row(
                    Modifier.fillMaxWidth().clickable(role = Role.Button) {
                        if (d.state == DownloadState.Done) nav.open(Screen.Player(d.title.id)) else nav.open(Screen.Downloads)
                    }.padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.width(88.dp).height(50.dp).clip(RoundedCornerShape(10.dp))) { Artwork(d.title.art.wide, Modifier.fillMaxSize(), alignX = d.alignX) }
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(d.label, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(
                            if (d.state == DownloadState.Done) "Готово · доступно офлайн" else d.detail,
                            fontSize = 12.sp, color = if (d.state == DownloadState.Running) ZonaColors.Link else ZonaColors.TextMuted,
                        )
                    }
                    Box(Modifier.size(36.dp).clip(CircleShape).background(ZonaColors.Surface2), contentAlignment = Alignment.Center) {
                        if (d.state == DownloadState.Done) ZIcon(ZIcons.Check, size = 18.dp, tint = ZonaColors.Success)
                        else ZIcon(ZIcons.Pause, size = 16.dp)
                    }
                }
            }
        }
    }
}

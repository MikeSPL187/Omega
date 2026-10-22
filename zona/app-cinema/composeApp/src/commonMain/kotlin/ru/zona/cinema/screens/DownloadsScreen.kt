package ru.zona.cinema.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import ru.zona.cinema.Navigator
import ru.zona.cinema.Screen
import ru.zona.cinema.data.Download
import ru.zona.cinema.data.DownloadState
import ru.zona.cinema.data.Library
import ru.zona.cinema.theme.Zona
import ru.zona.cinema.theme.ZonaColors
import ru.zona.cinema.theme.ZonaShapes
import ru.zona.cinema.ui.Artwork
import ru.zona.cinema.ui.ProgressStill
import ru.zona.cinema.ui.RoundIconButton
import ru.zona.cinema.ui.ZIcon
import ru.zona.cinema.ui.ZIcons

@Composable
fun DownloadsScreen(nav: Navigator) {
    val items = remember { mutableStateListOf<Download>().apply { addAll(Library.downloads) } }
    val active = items.filter { it.state != DownloadState.Done }
    val done = items.filter { it.state == DownloadState.Done }

    // Demo progress for the running download.
    LaunchedEffect(Unit) {
        while (true) {
            delay(700)
            val i = items.indexOfFirst { it.state == DownloadState.Running }
            if (i < 0) break
            val d = items[i]
            val f = (d.fraction + .01f).coerceAtMost(1f)
            items[i] = if (f >= 1f) d.copy(state = DownloadState.Done, fraction = 0f, detail = "Фильм · 1 ч 40 мин")
            else d.copy(fraction = f, detail = "${(f * 100).toInt()}% · ${(f * 1600).toInt()} МБ из 1,6 ГБ")
        }
    }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 32.dp)) {
        item {
            Row(Modifier.statusBarsPadding().fillMaxWidth().height(56.dp).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                RoundIconButton(ZIcons.Back, "Назад", { nav.back() }, glass = false, iconSize = 24.dp)
                Text("Загрузки", style = Zona.screenTitle.copy(fontSize = 26.sp), modifier = Modifier.weight(1f).padding(start = 4.dp))
            }
        }
        item {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 4.dp).clip(RoundedCornerShape(18.dp)).background(ZonaColors.Surface1).padding(16.dp)) {
                Row {
                    Text("Память телефона", fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text("свободно 12,4 ГБ из 64 ГБ", style = Zona.meta)
                }
                Row(Modifier.padding(top = 10.dp).fillMaxWidth().height(8.dp).clip(ZonaShapes.Pill), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    Box(Modifier.weight(.04f).fillMaxHeight().background(ZonaColors.Blue))
                    Box(Modifier.weight(.76f).fillMaxHeight().background(ZonaColors.Outline))
                    Box(Modifier.weight(.2f).fillMaxHeight().background(ZonaColors.Surface3))
                }
                Row(Modifier.padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Legend(ZonaColors.Blue, "Zona · 2,4 ГБ")
                    Spacer(Modifier.width(14.dp))
                    Legend(ZonaColors.Outline, "Другое")
                    Spacer(Modifier.weight(1f))
                    Text("1080p · только Wi‑Fi", style = Zona.meta.copy(fontSize = 12.sp))
                }
            }
        }
        if (active.isNotEmpty()) {
            item { Header("Загружается", "${active.size}") }
            items(active, key = { it.label }) { d ->
                ActiveRow(
                    d,
                    onRetry = { val i = items.indexOf(d); items[i] = d.copy(state = DownloadState.Running, detail = "Возобновляем…") },
                    onRemove = { items.remove(d) },
                )
            }
        }
        if (done.isNotEmpty()) {
            item { Header("Можно смотреть офлайн", "${done.size}") }
            items(done, key = { "done-" + it.label }) { d ->
                ProgressStill(
                    d.title, d.label, d.detail, d.fraction.takeIf { it > 0f },
                    onClick = { nav.open(Screen.Player(d.title.id)) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp).fillMaxWidth().height(170.dp),
                    alignX = d.alignX,
                )
            }
        }
    }
}

@Composable
private fun Legend(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(text, style = Zona.meta.copy(fontSize = 12.sp), modifier = Modifier.padding(start = 6.dp))
    }
}

@Composable
private fun Header(title: String, count: String) {
    Row(Modifier.padding(start = 16.dp, top = 20.dp, bottom = 4.dp), verticalAlignment = Alignment.Bottom) {
        Text(title, style = Zona.heading.copy(fontSize = 20.sp))
        Text(count, style = Zona.meta.copy(fontSize = 14.sp), modifier = Modifier.padding(start = 8.dp, bottom = 2.dp))
    }
}

@Composable
private fun ActiveRow(d: Download, onRetry: () -> Unit, onRemove: () -> Unit) {
    val failed = d.state == DownloadState.Failed
    Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(96.dp).height(54.dp).clip(RoundedCornerShape(10.dp))) {
            Artwork(d.title.art.wide, Modifier.fillMaxSize(), alignX = d.alignX, alpha = if (d.state == DownloadState.Running) 1f else .5f)
            if (failed) ZIcon(ZIcons.WifiOff, Modifier.align(Alignment.Center), tint = ZonaColors.Error)
        }
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text(d.label, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(
                d.detail, fontSize = 12.sp, lineHeight = 16.sp, modifier = Modifier.padding(top = 3.dp),
                color = when (d.state) { DownloadState.Running -> ZonaColors.Link; DownloadState.Failed -> ZonaColors.Error; else -> ZonaColors.TextMuted },
            )
            if (failed) {
                Box(
                    Modifier.padding(top = 6.dp).height(32.dp).clip(ZonaShapes.Pill).background(Color.White).clickable(role = Role.Button, onClick = onRetry).padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center,
                ) { Text("Повторить", color = ZonaColors.Bg, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
            }
        }
        when (d.state) {
            DownloadState.Running -> Box(Modifier.size(44.dp).clip(CircleShape).clickable(role = Role.Button, onClickLabel = "Отменить загрузку", onClick = onRemove), contentAlignment = Alignment.Center) {
                Canvas(Modifier.size(40.dp)) {
                    drawArc(ZonaColors.Surface3, 0f, 360f, false, style = Stroke(3.dp.toPx()))
                    drawArc(ZonaColors.Blue, -90f, 360f * d.fraction, false, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
                }
                ZIcon(ZIcons.Close, size = 14.dp)
            }
            DownloadState.Queued -> RoundIconButton(ZIcons.Close, "Убрать из очереди", onRemove, glass = false, tint = ZonaColors.TextMuted, iconSize = 16.dp)
            else -> RoundIconButton(ZIcons.Trash, "Удалить загрузку", onRemove, glass = false, tint = ZonaColors.TextMuted)
        }
    }
}

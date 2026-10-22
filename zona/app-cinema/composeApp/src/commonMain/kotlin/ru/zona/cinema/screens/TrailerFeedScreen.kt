package ru.zona.cinema.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import ru.zona.cinema.Navigator
import ru.zona.cinema.Screen
import ru.zona.cinema.data.Demo
import ru.zona.cinema.data.Title
import ru.zona.cinema.theme.Zona
import ru.zona.cinema.theme.ZonaColors
import ru.zona.cinema.ui.Artwork
import ru.zona.cinema.ui.GlassButton
import ru.zona.cinema.ui.PrimaryButton
import ru.zona.cinema.ui.ProgressLine
import ru.zona.cinema.ui.RoundIconButton
import ru.zona.cinema.ui.ZIcon
import ru.zona.cinema.ui.ZIcons
import ru.zona.cinema.ui.glass

private val feedKinds = listOf("Трейлер · 1:12", "Сцена · 0:42", "Тизер · 0:30", "За кадром · 1:05")

/** Full-screen vertical feed of trailers and scenes (swipe up for the next one). */
@Composable
fun TrailerFeedScreen(startTitleId: String?, nav: Navigator) {
    val feed = remember(startTitleId) {
        val start = Demo.all.indexOfFirst { it.id == startTitleId }.coerceAtLeast(0)
        Demo.all.drop(start) + Demo.all.take(start)
    }
    val pager = rememberPagerState { feed.size }
    var muted by remember { mutableStateOf(true) }
    val saved = remember { mutableStateOf(setOf<String>()) }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        VerticalPager(pager, Modifier.fillMaxSize()) { page ->
            FeedPage(
                title = feed[page],
                kind = feedKinds[page % feedKinds.size],
                active = pager.currentPage == page,
                saved = feed[page].id in saved.value,
                onSave = { id -> saved.value = if (id in saved.value) saved.value - id else saved.value + id },
                nav = nav,
            )
        }
        Row(
            Modifier.statusBarsPadding().padding(horizontal = 12.dp, vertical = 4.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RoundIconButton(ZIcons.Back, "Назад", { nav.back() })
            Text("Трейлеры и сцены", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f).padding(start = 10.dp))
            RoundIconButton(if (muted) ZIcons.VolumeOff else ZIcons.Volume, if (muted) "Включить звук" else "Выключить звук", { muted = !muted })
        }
    }
}

@Composable
private fun FeedPage(title: Title, kind: String, active: Boolean, saved: Boolean, onSave: (String) -> Unit, nav: Navigator) {
    var progress by remember { mutableFloatStateOf(0f) }
    // Stand-in for clip playback: the bar fills while the page is on screen.
    LaunchedEffect(active) {
        if (!active) { progress = 0f; return@LaunchedEffect }
        while (progress < 1f) { delay(100); progress += .01f }
    }
    Box(Modifier.fillMaxSize()) {
        Artwork(title.art.poster, Modifier.fillMaxSize())
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(0f to Color(0xA607090B), .18f to Color.Transparent, .5f to Color.Transparent, .88f to Color(0xEB07090B)),
            ),
        )
        Column(
            Modifier.align(Alignment.BottomEnd).navigationBarsPadding().padding(end = 12.dp, bottom = 250.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            RailButton(if (saved) ZIcons.Check else ZIcons.Plus, if (saved) "В Моём" else "В Моё") { onSave(title.id) }
            RailButton(ZIcons.Bell, "Напомнить") {}
            RailButton(ZIcons.Share, "Поделиться") {}
        }
        Column(Modifier.align(Alignment.BottomStart).navigationBarsPadding().padding(start = 16.dp, end = 16.dp, bottom = 28.dp)) {
            Box(Modifier.height(24.dp).glass().padding(horizontal = 10.dp), contentAlignment = Alignment.Center) {
                Text(kind.uppercase(), fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = .06.em)
            }
            Text(
                title.name,
                modifier = Modifier.padding(top = 12.dp).widthIn(max = 300.dp),
                style = TextStyle(
                    fontFamily = Zona.fonts.of(title.lettering.face), fontWeight = FontWeight.Bold,
                    fontSize = 44.sp, lineHeight = 46.sp, color = title.lettering.color,
                ),
            )
            Row(Modifier.padding(top = 10.dp)) {
                Text("КП ${title.kp}", color = ZonaColors.Amber, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("  ·  ${title.meta}", color = ZonaColors.TextSecondary, fontSize = 13.sp)
            }
            Row(Modifier.padding(top = 16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PrimaryButton(if (title.isSeries) "Смотреть сериал" else "Смотреть фильм", { nav.open(Screen.Player(title.id)) }, Modifier.weight(1f))
                GlassButton("Подробнее", { nav.open(Screen.Card(title.id)) })
            }
            ProgressLine(progress, Modifier.padding(top = 18.dp).fillMaxWidth(), track = Color(0x40FFFFFF))
            Text("Листайте вверх — следующий ролик", style = Zona.meta.copy(fontSize = 11.sp), modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 10.dp))
        }
    }
}

@Composable
private fun RailButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(role = Role.Button, onClick = onClick)) {
        Box(Modifier.padding(2.dp).glass().padding(13.dp)) { ZIcon(icon) }
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

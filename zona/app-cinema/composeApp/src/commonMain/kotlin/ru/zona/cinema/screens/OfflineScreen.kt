package ru.zona.cinema.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.zona.cinema.Navigator
import ru.zona.cinema.Screen
import ru.zona.cinema.data.DownloadState
import ru.zona.cinema.data.Library
import ru.zona.cinema.theme.Zona
import ru.zona.cinema.theme.ZonaColors
import ru.zona.cinema.ui.PrimaryButton
import ru.zona.cinema.ui.ProgressStill
import ru.zona.cinema.ui.ZIcon
import ru.zona.cinema.ui.ZIcons
import ru.zona.cinema.ui.glass

/** Shown when the API is unreachable; offers downloaded titles instead of a dead end. */
@Composable
fun OfflineScreen(nav: Navigator) {
    var attempts by remember { mutableIntStateOf(0) }
    val offline = Library.downloads.filter { it.state == DownloadState.Done }

    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier.align(Alignment.TopCenter).padding(top = 150.dp).size(420.dp)
                .background(Brush.radialGradient(listOf(Color(0x380094F6), Color.Transparent))),
        )
        Row(
            Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(top = 12.dp).height(32.dp).glass().padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ZIcon(ZIcons.WifiOff, size = 16.dp, tint = ZonaColors.Error)
            Text("Офлайн", fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
        }
        Column(
            Modifier.fillMaxWidth().statusBarsPadding().padding(start = 24.dp, end = 24.dp, top = 90.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Placeholder for the brand illustration (pirate dog) from the original app.
            Box(Modifier.size(180.dp).clip(CircleShape).border(1.dp, ZonaColors.Outline, CircleShape), contentAlignment = Alignment.Center) {
                ZIcon(ZIcons.WifiOff, size = 64.dp, tint = ZonaColors.Outline)
            }
            Text("Нет соединения", style = Zona.screenTitle.copy(fontSize = 30.sp), modifier = Modifier.padding(top = 28.dp))
            Text(
                "Проверьте интернет. Если с сетью всё в порядке — сервер Zona временно недоступен, мы уже чиним.",
                modifier = Modifier.padding(top = 10.dp), textAlign = TextAlign.Center,
                color = ZonaColors.TextSecondary, fontSize = 15.sp, lineHeight = 22.sp,
            )
            PrimaryButton(
                if (attempts == 0) "Повторить" else "Повторить ещё раз",
                { attempts++ }, Modifier.padding(top = 24.dp).fillMaxWidth(), icon = ZIcons.Retry, height = 54.dp,
            )
            Box(
                Modifier.padding(top = 10.dp).fillMaxWidth().height(54.dp).clip(CircleShape).background(ZonaColors.Surface2)
                    .clickable(role = Role.Button) { nav.open(Screen.Downloads) },
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ZIcon(ZIcons.Download, size = 18.dp)
                    Text("Смотреть загруженное", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(start = 8.dp))
                }
            }
            if (attempts > 0) {
                Text("Попыток: $attempts · по-прежнему нет связи", style = Zona.meta.copy(fontSize = 12.sp), modifier = Modifier.padding(top = 10.dp))
            }
        }
        Column(Modifier.align(Alignment.BottomStart).navigationBarsPadding().padding(bottom = 28.dp)) {
            Text("Доступно без интернета", style = Zona.heading.copy(fontSize = 18.sp), modifier = Modifier.padding(horizontal = 16.dp))
            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 12.dp)) {
                items(offline) { d ->
                    ProgressStill(
                        d.title.art.wide, d.title.name, d.detail, d.fraction.takeIf { it > 0f },
                        onClick = { nav.open(Screen.Player(d.title.id)) },
                        modifier = Modifier.width(220.dp).height(124.dp), alignX = d.alignX,
                    )
                }
            }
        }
    }
}

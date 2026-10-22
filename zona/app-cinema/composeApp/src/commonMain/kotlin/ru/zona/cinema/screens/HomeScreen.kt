package ru.zona.cinema.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import ru.zona.cinema.Navigator
import ru.zona.cinema.Screen
import ru.zona.cinema.data.Demo
import ru.zona.cinema.theme.Zona
import ru.zona.cinema.theme.ZonaColors
import ru.zona.cinema.theme.ZonaShapes
import ru.zona.cinema.ui.Artwork
import ru.zona.cinema.ui.Dot
import ru.zona.cinema.ui.GlassButton
import ru.zona.cinema.ui.Poster
import ru.zona.cinema.ui.PrimaryButton
import ru.zona.cinema.ui.ProgressStill
import ru.zona.cinema.ui.RoundIconButton
import ru.zona.cinema.ui.SectionHeader
import ru.zona.cinema.ui.Tab
import ru.zona.cinema.ui.ZChip
import ru.zona.cinema.ui.ZIcon
import ru.zona.cinema.ui.ZIcons
import ru.zona.cinema.ui.glass

@Composable
fun HomeScreen(nav: Navigator) {
    val hero = Demo.north
    var inMy by remember { mutableStateOf(false) }
    val rowPadding = PaddingValues(horizontal = 16.dp)

    Box(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 112.dp)) {
            item(key = "hero") {
                Box(Modifier.fillMaxWidth().height(580.dp)) {
                    Artwork(hero.art.poster, Modifier.fillMaxSize(), alignY = -.2f)
                    Box(
                        Modifier.fillMaxSize().background(
                            Brush.verticalGradient(
                                0f to Color(0xB307090B), .22f to Color.Transparent, .48f to Color.Transparent,
                                .82f to Color(0xE007090B), 1f to ZonaColors.Bg,
                            )
                        )
                    )
                    Column(Modifier.statusBarsPadding()) {
                        Row(Modifier.fillMaxWidth().height(52.dp).padding(start = 16.dp, end = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(ZIcons.Logo, contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(28.dp, 29.dp))
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "zona",
                                style = TextStyle(fontFamily = Zona.fonts.unbounded, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, letterSpacing = .02.em),
                                modifier = Modifier.weight(1f),
                            )
                            RoundIconButton(ZIcons.Cast, "Транслировать на ТВ", {}, glass = false, iconSize = 22.dp)
                            Box(
                                Modifier.size(44.dp).clip(CircleShape).clickable(role = Role.Button) { nav.tab(Tab.Profile) }.semantics { contentDescription = "Профиль" },
                                contentAlignment = Alignment.Center,
                            ) {
                                Box(Modifier.size(30.dp).clip(CircleShape).background(Color(0xFF0077CC)), contentAlignment = Alignment.Center) {
                                    Text("U", fontWeight = FontWeight.Black, fontSize = 13.sp)
                                }
                            }
                        }
                        LazyRow(contentPadding = rowPadding, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                            items(listOf("Фильмы", "Сериалы", "ТВ-каналы", "Спорт")) { ZChip(it, onClick = { nav.tab(Tab.Catalog) }, glass = true) }
                        }
                    }
                    RoundIconButton(
                        ZIcons.VolumeOff, "Включить звук трейлера", {},
                        Modifier.align(Alignment.TopEnd).offset(x = (-16).dp, y = 330.dp), size = 40.dp, iconSize = 18.dp,
                    )
                    Column(
                        Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(6.dp).clip(CircleShape).background(ZonaColors.Blue))
                            Spacer(Modifier.width(6.dp))
                            Text("НОВЫЙ СЕЗОН · ТРЕЙЛЕР", fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = .08.em)
                        }
                        Text(
                            hero.name.uppercase().replace(" ", "\n"),
                            modifier = Modifier.padding(top = 10.dp),
                            textAlign = TextAlign.Center,
                            style = TextStyle(
                                fontFamily = Zona.fonts.oswald, fontWeight = FontWeight.Bold, fontSize = 54.sp,
                                lineHeight = 50.sp, letterSpacing = .04.em, color = Color.White,
                            ),
                        )
                        Row(Modifier.padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("КП ${hero.kp}", color = ZonaColors.Amber, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("  ·  ${hero.meta}", color = ZonaColors.TextSecondary, fontSize = 13.sp)
                        }
                        Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            PrimaryButton("Смотреть", { nav.open(Screen.Player(hero.id)) }, Modifier.weight(1f))
                            GlassButton(
                                if (inMy) "В Моём" else "В Моё", { inMy = !inMy }, Modifier.weight(1f),
                                icon = if (inMy) ZIcons.Check else ZIcons.Plus,
                            )
                        }
                        Row(Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Dot(Color.White, 20.dp)
                            repeat(5) { Dot(Color(0x59FFFFFF)) }
                        }
                    }
                }
            }

            item(key = "continue") {
                Column(Modifier.padding(top = 12.dp)) {
                    SectionHeader("Продолжить просмотр", action = "Все", onAction = { nav.tab(Tab.My) })
                    LazyRow(contentPadding = rowPadding, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 4.dp)) {
                        items(Demo.continueWatching) { p ->
                            ProgressStill(
                                p.title.art.wide, p.title.name, p.label, p.fraction,
                                onClick = { nav.open(Screen.Player(p.title.id, p.season, p.episode)) },
                                modifier = Modifier.width(248.dp).height(140.dp),
                            )
                        }
                    }
                }
            }

            item(key = "clips") {
                Column(Modifier.padding(top = 22.dp)) {
                    SectionHeader("Трейлеры и сцены", action = "Смотреть ленту", onAction = { nav.open(Screen.Trailers()) })
                    LazyRow(contentPadding = rowPadding, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 4.dp)) {
                        items(Demo.clips) { c ->
                            Box(
                                Modifier.width(128.dp).height(228.dp).clip(RoundedCornerShape(14.dp))
                                    .clickable(role = Role.Button) { nav.open(Screen.Trailers(c.title.id)) },
                            ) {
                                Artwork(c.title.art.poster, Modifier.fillMaxSize(), alignX = c.alignX)
                                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(.5f to Color.Transparent, 1f to Color(0xE607090B))))
                                Row(
                                    Modifier.padding(8.dp).height(22.dp).glass().padding(horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    ZIcon(ZIcons.Play, size = 9.dp)
                                    Spacer(Modifier.width(4.dp))
                                    Text(c.duration, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Column(Modifier.align(Alignment.BottomStart).padding(10.dp)) {
                                    Text(c.kind, fontSize = 11.sp, color = ZonaColors.TextSecondary, fontWeight = FontWeight.Medium)
                                    Text(c.title.name, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            item(key = "top") {
                Column(Modifier.padding(top = 22.dp)) {
                    SectionHeader("Топ-10 сегодня")
                    LazyRow(contentPadding = rowPadding, horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 4.dp)) {
                        itemsIndexed(Demo.top10) { i, t ->
                            Row(verticalAlignment = Alignment.Bottom) {
                                Box {
                                    // Outlined rank numeral, partly tucked behind the poster.
                                    Text("${i + 1}", style = TextStyle(fontFamily = Zona.fonts.oswald, fontWeight = FontWeight.Bold, fontSize = 128.sp, lineHeight = 112.sp, color = ZonaColors.Bg))
                                    Text(
                                        "${i + 1}",
                                        style = TextStyle(
                                            fontFamily = Zona.fonts.oswald, fontWeight = FontWeight.Bold, fontSize = 128.sp, lineHeight = 112.sp,
                                            color = ZonaColors.Outline, drawStyle = Stroke(width = 4f),
                                        ),
                                    )
                                }
                                Poster(
                                    t, { nav.open(Screen.Card(t.id)) },
                                    Modifier.offset(x = (-14).dp).width(108.dp).height(162.dp).shadow(16.dp, ZonaShapes.Poster),
                                    showRating = false,
                                )
                            }
                        }
                    }
                }
            }

            item(key = "fresh") {
                Column(Modifier.padding(top = 22.dp)) {
                    SectionHeader("Новинки фильмов", action = "Все", onAction = { nav.tab(Tab.Catalog) })
                    LazyRow(contentPadding = rowPadding, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 4.dp)) {
                        items(Demo.freshMovies) { t ->
                            Column(Modifier.width(112.dp)) {
                                Poster(t, { nav.open(Screen.Card(t.id)) }, Modifier.fillMaxWidth().height(168.dp))
                                Text("${t.year} · ${t.genres}", style = Zona.meta.copy(fontSize = 12.sp), modifier = Modifier.padding(top = 6.dp), maxLines = 1)
                            }
                        }
                    }
                }
            }
        }

    }
}

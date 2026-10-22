package ru.zona.cinema.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import ru.zona.cinema.Navigator
import ru.zona.cinema.Screen
import ru.zona.cinema.theme.Zona
import ru.zona.cinema.theme.ZonaColors
import ru.zona.cinema.theme.ZonaShapes
import ru.zona.cinema.ui.ZIcon
import ru.zona.cinema.ui.ZIcons

@Composable
fun ProfileScreen(nav: Navigator) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 112.dp)) {
        item {
            Column(Modifier.statusBarsPadding().padding(horizontal = 16.dp)) {
                Box(Modifier.height(56.dp), contentAlignment = Alignment.CenterStart) { Text("Профиль", style = Zona.screenTitle) }
                Row(
                    Modifier.fillMaxWidth().clip(ZonaShapes.Card).clickable(role = Role.Button) { nav.open(Screen.Login) }.padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(60.dp).clip(CircleShape).background(Color(0xFF0077CC)), contentAlignment = Alignment.Center) {
                        Text("U", style = Zona.heading.copy(fontSize = 26.sp))
                    }
                    Column(Modifier.weight(1f).padding(start = 14.dp)) {
                        Text("user@mail.ru", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        Text("Бесплатный профиль · управление", style = Zona.meta, modifier = Modifier.padding(top = 3.dp))
                    }
                    ZIcon(ZIcons.ChevronRight, size = 20.dp, tint = ZonaColors.TextMuted)
                }
            }
        }
        item {
            Column(
                Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp).fillMaxWidth().clip(RoundedCornerShape(20.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFF2A2108), ZonaColors.Surface1)))
                    .border(1.dp, Color(0x4DFFB400), RoundedCornerShape(20.dp)).padding(18.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ZIcon(ZIcons.Crown, size = 18.dp, tint = ZonaColors.Amber)
                    Spacer(Modifier.width(8.dp))
                    Text("ZONA ПРЕМИУМ", color = ZonaColors.Amber, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = .08.em)
                }
                Text("Смотрите без рекламы", style = Zona.heading.copy(fontSize = 22.sp), modifier = Modifier.padding(top = 8.dp))
                Text("Новые возможности с премиум профилем.", color = ZonaColors.TextSecondary, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                Box(
                    Modifier.padding(top = 14.dp).fillMaxWidth().height(46.dp).clip(ZonaShapes.Pill).background(ZonaColors.Amber).clickable(role = Role.Button) {},
                    contentAlignment = Alignment.Center,
                ) { Text("Посмотреть тарифы", color = ZonaColors.Bg, fontWeight = FontWeight.Bold, fontSize = 15.sp) }
            }
        }
        item {
            Row(
                Modifier.padding(start = 16.dp, end = 16.dp, top = 10.dp).fillMaxWidth().height(64.dp).clip(RoundedCornerShape(18.dp))
                    .background(ZonaColors.Surface1).clickable(role = Role.Button) {}.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(ZonaColors.BlueTint), contentAlignment = Alignment.Center) {
                    ZIcon(ZIcons.Qr, tint = ZonaColors.Blue)
                }
                Column(Modifier.weight(1f).padding(start = 14.dp)) {
                    Text("Войти на телевизоре", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Наведите камеру на QR-код на ТВ", style = Zona.meta.copy(fontSize = 12.sp))
                }
                ZIcon(ZIcons.ChevronRight, size = 18.dp, tint = ZonaColors.TextMuted)
            }
        }
        item { MenuGroup(listOf(MenuItem(ZIcons.Settings, "Настройки"), MenuItem(ZIcons.Trailer, "Настройки плеера"))) }
        item {
            MenuGroup(
                listOf(
                    MenuItem(ZIcons.Flag, "Сообщить о проблеме"),
                    MenuItem(ZIcons.Share, "Рекомендовать Zona"),
                    MenuItem(ZIcons.WifiOff, "Экран «Нет сети» (превью)") { nav.open(Screen.Offline) },
                ),
            )
        }
        item {
            Text("Zona Cinema · превью интерфейса", style = Zona.meta.copy(fontSize = 12.sp), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 6.dp))
        }
    }
}

private class MenuItem(val icon: ImageVector, val label: String, val onClick: () -> Unit = {})

@Composable
private fun MenuGroup(items: List<MenuItem>) {
    Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 10.dp).clip(RoundedCornerShape(18.dp)).background(ZonaColors.Surface1)) {
        items.forEachIndexed { i, m ->
            if (i > 0) HorizontalDivider(color = Color(0x0FFFFFFF))
            Row(
                Modifier.fillMaxWidth().height(52.dp).clickable(role = Role.Button, onClick = m.onClick).padding(start = 16.dp, end = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ZIcon(m.icon, tint = ZonaColors.TextMuted)
                Text(m.label, fontSize = 15.sp, modifier = Modifier.weight(1f).padding(start = 14.dp))
                ZIcon(ZIcons.ChevronRight, size = 18.dp, tint = ZonaColors.TextMuted)
            }
        }
    }
}

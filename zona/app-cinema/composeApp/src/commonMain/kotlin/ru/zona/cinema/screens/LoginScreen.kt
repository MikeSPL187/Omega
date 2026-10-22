package ru.zona.cinema.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import ru.zona.cinema.Navigator
import ru.zona.cinema.data.Demo
import ru.zona.cinema.theme.Zona
import ru.zona.cinema.theme.ZonaColors
import ru.zona.cinema.theme.ZonaShapes
import ru.zona.cinema.ui.Artwork
import ru.zona.cinema.ui.GlassButton
import ru.zona.cinema.ui.RoundIconButton
import ru.zona.cinema.ui.ZIcon
import ru.zona.cinema.ui.ZIcons

/** Sign-in options from the original app (Yandex ID, Telegram, e-mail), restyled. */
@Composable
fun LoginScreen(nav: Navigator) {
    Box(Modifier.fillMaxSize()) {
        // Poster wall behind the header.
        Box(Modifier.fillMaxWidth().height(470.dp)) {
            Row(
                Modifier.requiredWidth(560.dp).align(Alignment.TopCenter).offset(y = (-40).dp).rotate(-8f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                val arts = Demo.all.map { it.art }
                repeat(4) { col ->
                    Column(Modifier.weight(1f).padding(top = if (col % 2 == 1) 40.dp else 0.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        repeat(3) { row ->
                            Box(Modifier.fillMaxWidth().height(190.dp).clip(RoundedCornerShape(12.dp))) {
                                Artwork(arts[(col * 3 + row * 5) % arts.size].poster, Modifier.fillMaxSize(), alpha = .75f)
                            }
                        }
                    }
                }
            }
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(0f to Color(0x5907090B), .35f to Color(0x3307090B), .92f to ZonaColors.Bg)))
        }

        Row(Modifier.statusBarsPadding().padding(horizontal = 12.dp, vertical = 4.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            RoundIconButton(ZIcons.Back, "Назад", { nav.back() })
            Spacer(Modifier.weight(1f))
            GlassButton("Не сейчас", { nav.back() }, height = 44.dp)
        }

        Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 300.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(ZIcons.Logo, null, tint = Color.Unspecified, modifier = Modifier.size(30.dp, 31.dp))
                Text("zona", style = TextStyle(fontFamily = Zona.fonts.unbounded, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp), modifier = Modifier.padding(start = 10.dp))
            }
            Text("Вход в профиль\nи регистрация", style = Zona.screenTitle.copy(fontSize = 30.sp, lineHeight = 33.sp), modifier = Modifier.padding(top = 16.dp))
            Column(Modifier.padding(top = 18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Benefit(ZIcons.Sync, "История и прогресс на всех устройствах")
                Benefit(ZIcons.BookmarkFilled, "«Моё» и уведомления о новых сериях")
                Benefit(ZIcons.Tv, "Вход на телевизоре за секунду")
            }
        }

        Column(
            Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            LoginButton("Войти с Яндекс ID", Color.White, ZonaColors.Bg, onClick = { nav.back() }) {
                Box(Modifier.size(24.dp).clip(CircleShape).background(Color(0xFFFC3F1D)), contentAlignment = Alignment.Center) {
                    Text("Я", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
            LoginButton("Войти через Telegram", ZonaColors.Surface2, ZonaColors.Text, onClick = { nav.back() }) {
                Box(Modifier.size(24.dp).clip(CircleShape).background(Color(0xFF2AABEE)), contentAlignment = Alignment.Center) {
                    ZIcon(ZIcons.Telegram, size = 13.dp, tint = Color.White)
                }
            }
            LoginButton("По почте", Color.Transparent, ZonaColors.Text, outlined = true, bold = false, onClick = { nav.back() }) {
                ZIcon(ZIcons.Mail, size = 20.dp)
            }
            Text(
                "Подойдёт аккаунт Zona для Windows.\nНет профиля — создадим при входе.",
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                textAlign = TextAlign.Center, style = Zona.meta.copy(fontSize = 12.sp, lineHeight = 17.sp),
            )
        }
    }
}

@Composable
private fun Benefit(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(32.dp).clip(CircleShape).background(ZonaColors.BlueTint), contentAlignment = Alignment.Center) {
            ZIcon(icon, size = 16.dp, tint = ZonaColors.Blue)
        }
        Text(text, color = ZonaColors.TextSecondary, fontSize = 14.sp, modifier = Modifier.padding(start = 12.dp))
    }
}

@Composable
private fun LoginButton(
    text: String,
    bg: Color,
    fg: Color,
    outlined: Boolean = false,
    bold: Boolean = true,
    onClick: () -> Unit,
    leading: @Composable () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().height(52.dp).clip(ZonaShapes.Pill).background(bg)
            .then(if (outlined) Modifier.border(1.dp, Color(0x2EFFFFFF), ZonaShapes.Pill) else Modifier)
            .clickable(role = Role.Button, onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leading()
        Spacer(Modifier.width(10.dp))
        Text(text, color = fg, fontSize = 16.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium, letterSpacing = 0.em)
    }
}

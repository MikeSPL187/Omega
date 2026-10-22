package ru.zona.cinema.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import ru.zona.cinema.res.Res
import ru.zona.cinema.res.oswald_bold
import ru.zona.cinema.res.playfair_extrabold
import ru.zona.cinema.res.roboto_black
import ru.zona.cinema.res.roboto_bold
import ru.zona.cinema.res.roboto_condensed_extrabold
import ru.zona.cinema.res.roboto_medium
import ru.zona.cinema.res.roboto_regular
import ru.zona.cinema.res.russo_one
import ru.zona.cinema.res.unbounded_extrabold

/** Zona Cinema palette. Contrast ratios are against [Bg] unless noted. */
object ZonaColors {
    val Bg = Color(0xFF07090B)
    val Surface1 = Color(0xFF111519)
    val Surface2 = Color(0xFF1A2026)
    val Surface3 = Color(0xFF252C34)
    val Outline = Color(0xFF3A4652)

    val Text = Color(0xFFF5F7FA)
    val TextSecondary = Color(0xFFC9D1DA) // 12.9:1
    val TextMuted = Color(0xFFA7B1BC) // 9.2:1, 7.5:1 on Surface2

    /** Brand blue from the original app: logo, progress, active tab, focus. */
    val Blue = Color(0xFF0094F6)
    val BlueTint = Color(0x2E0094F6)
    val OnBlue = Color(0xFF03101C) // 6.0:1 on Blue
    val Link = Color(0xFF3DA9FF) // 7.9:1
    val Amber = Color(0xFFFFB400)
    val Success = Color(0xFF26C442)
    val Error = Color(0xFFFF8A8A) // 8.8:1

    val Glass = Color(0x8C14191E)
    val GlassStrong = Color(0xC714191E)
    val GlassBorder = Color(0x1FFFFFFF)
    val Scrim = Color(0xE607090B)
}

object ZonaShapes {
    val Poster = RoundedCornerShape(12.dp)
    val Card = RoundedCornerShape(16.dp)
    val Panel = RoundedCornerShape(24.dp)
    val Pill = RoundedCornerShape(percent = 50)
}

/** Poster lettering faces: each title gets its own, like printed key art. */
enum class TitleFace { Oswald, Playfair, Russo, Unbounded }

@Immutable
class ZonaFonts(
    val body: FontFamily,
    val condensed: FontFamily,
    val oswald: FontFamily,
    val unbounded: FontFamily,
    val playfair: FontFamily,
    val russo: FontFamily,
) {
    fun of(face: TitleFace): FontFamily = when (face) {
        TitleFace.Oswald -> oswald
        TitleFace.Playfair -> playfair
        TitleFace.Russo -> russo
        TitleFace.Unbounded -> unbounded
    }
}

val LocalZonaFonts = staticCompositionLocalOf<ZonaFonts> { error("ZonaTheme is missing") }

object Zona {
    val fonts: ZonaFonts
        @Composable get() = LocalZonaFonts.current

    /** Section headings: Roboto Condensed 800. */
    val heading: TextStyle
        @Composable get() = TextStyle(fontFamily = fonts.condensed, fontWeight = FontWeight.ExtraBold, fontSize = 21.sp, color = ZonaColors.Text)

    val screenTitle: TextStyle
        @Composable get() = heading.copy(fontSize = 28.sp)

    val meta: TextStyle
        @Composable get() = TextStyle(fontFamily = fonts.body, fontSize = 13.sp, color = ZonaColors.TextMuted)
}

@Composable
fun ZonaTheme(content: @Composable () -> Unit) {
    val body = FontFamily(
        Font(Res.font.roboto_regular, FontWeight.Normal),
        Font(Res.font.roboto_medium, FontWeight.Medium),
        Font(Res.font.roboto_bold, FontWeight.Bold),
        Font(Res.font.roboto_black, FontWeight.Black),
    )
    val fonts = ZonaFonts(
        body = body,
        condensed = FontFamily(Font(Res.font.roboto_condensed_extrabold, FontWeight.ExtraBold)),
        oswald = FontFamily(Font(Res.font.oswald_bold, FontWeight.Bold)),
        unbounded = FontFamily(Font(Res.font.unbounded_extrabold, FontWeight.ExtraBold)),
        playfair = FontFamily(Font(Res.font.playfair_extrabold, FontWeight.ExtraBold)),
        russo = FontFamily(Font(Res.font.russo_one, FontWeight.Normal)),
    )
    val base = Typography()
    val typography = Typography(
        displayLarge = base.displayLarge.copy(fontFamily = body),
        displayMedium = base.displayMedium.copy(fontFamily = body),
        displaySmall = base.displaySmall.copy(fontFamily = body),
        headlineLarge = base.headlineLarge.copy(fontFamily = body),
        headlineMedium = base.headlineMedium.copy(fontFamily = body),
        headlineSmall = base.headlineSmall.copy(fontFamily = body),
        titleLarge = base.titleLarge.copy(fontFamily = body),
        titleMedium = base.titleMedium.copy(fontFamily = body),
        titleSmall = base.titleSmall.copy(fontFamily = body),
        bodyLarge = TextStyle(fontFamily = body, fontSize = 15.sp, lineHeight = 22.sp),
        bodyMedium = TextStyle(fontFamily = body, fontSize = 14.sp, lineHeight = 20.sp),
        bodySmall = TextStyle(fontFamily = body, fontSize = 12.sp, lineHeight = 16.sp),
        labelLarge = base.labelLarge.copy(fontFamily = body),
        labelMedium = base.labelMedium.copy(fontFamily = body),
        labelSmall = base.labelSmall.copy(fontFamily = body),
    )
    val scheme = darkColorScheme(
        primary = ZonaColors.Blue,
        onPrimary = ZonaColors.OnBlue,
        background = ZonaColors.Bg,
        onBackground = ZonaColors.Text,
        surface = ZonaColors.Surface1,
        onSurface = ZonaColors.Text,
        surfaceVariant = ZonaColors.Surface2,
        onSurfaceVariant = ZonaColors.TextMuted,
        outline = ZonaColors.Outline,
        error = ZonaColors.Error,
    )
    CompositionLocalProvider(LocalZonaFonts provides fonts) {
        MaterialTheme(colorScheme = scheme, typography = typography) {
            // Material's default content color is black outside a Surface; text sits on dark art here.
            CompositionLocalProvider(LocalContentColor provides ZonaColors.Text, content = content)
        }
    }
}

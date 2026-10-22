package ru.zona.cinema.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import ru.zona.cinema.data.Title
import ru.zona.cinema.theme.Zona
import ru.zona.cinema.theme.ZonaColors
import ru.zona.cinema.theme.ZonaShapes

/** Translucent panel used over video and key art. */
fun Modifier.glass(shape: Shape = ZonaShapes.Pill, strong: Boolean = false): Modifier =
    clip(shape)
        .background(if (strong) ZonaColors.GlassStrong else ZonaColors.Glass)
        .border(1.dp, ZonaColors.GlassBorder, shape)

/** Fades artwork into the page background from [startFraction] down. */
fun Modifier.bottomFade(startFraction: Float = .45f, color: Color = ZonaColors.Bg): Modifier =
    background(Brush.verticalGradient(startFraction to Color.Transparent, 1f to color))

@Composable
fun Artwork(res: DrawableResource, modifier: Modifier = Modifier, alignX: Float = 0f, alignY: Float = 0f, alpha: Float = 1f) {
    Image(
        painter = painterResource(res),
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Crop,
        alignment = BiasAlignment(alignX, alignY),
        alpha = alpha,
    )
}

@Composable
fun ZIcon(icon: ImageVector, modifier: Modifier = Modifier, size: Dp = 22.dp, tint: Color = ZonaColors.Text) {
    Icon(icon, contentDescription = null, modifier = modifier.size(size), tint = tint)
}

/** Primary action: white pill, 19.9:1 against the label. */
@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = ZIcons.Play, height: Dp = 50.dp, trailing: String? = null) {
    Row(
        modifier
            .height(height)
            .clip(ZonaShapes.Pill)
            .background(Color.White)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            ZIcon(icon, size = 16.dp, tint = ZonaColors.Bg)
            Spacer(Modifier.width(8.dp))
        }
        Text(text, color = ZonaColors.Bg, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1)
        if (trailing != null) {
            Text(" · $trailing", color = ZonaColors.Outline, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun GlassButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null, height: Dp = 50.dp) {
    Row(
        modifier.height(height).glass().clickable(role = Role.Button, onClick = onClick).padding(horizontal = 18.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            ZIcon(icon, size = 18.dp)
            Spacer(Modifier.width(8.dp))
        }
        Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ZonaColors.Text, maxLines = 1)
    }
}

/** Round icon-only button with a 44dp touch target. */
@Composable
fun RoundIconButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    iconSize: Dp = 20.dp,
    glass: Boolean = true,
    tint: Color = ZonaColors.Text,
) {
    Box(
        modifier
            .size(size)
            .then(if (glass) Modifier.glass(CircleShape) else Modifier.clip(CircleShape))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) { ZIcon(icon, size = iconSize, tint = tint) }
}

@Composable
fun ZChip(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, selected: Boolean = false, leading: ImageVector? = null, trailing: ImageVector? = null, glass: Boolean = false) {
    val shape = ZonaShapes.Pill
    val base = when {
        selected -> Modifier.clip(shape).background(Color.White)
        glass -> Modifier.glass(shape)
        else -> Modifier.clip(shape).background(ZonaColors.Surface2)
    }
    val fg = if (selected) ZonaColors.Bg else ZonaColors.Text
    Row(
        modifier
            .height(36.dp)
            .then(base)
            .clickable(role = Role.Tab, onClick = onClick)
            .semantics { this.selected = selected }
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (leading != null) ZIcon(leading, size = 15.dp, tint = if (selected) fg else ZonaColors.TextMuted)
        Text(text, color = fg, fontSize = 14.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, maxLines = 1)
        if (trailing != null) ZIcon(trailing, size = 14.dp, tint = if (selected) fg else ZonaColors.TextMuted)
    }
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier, action: String? = null, onAction: () -> Unit = {}, count: String? = null) {
    Row(
        modifier.fillMaxWidth().height(44.dp).padding(start = 16.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, style = Zona.heading, modifier = Modifier.weight(1f, fill = false))
        if (count != null) {
            Spacer(Modifier.width(8.dp))
            Text(count, color = ZonaColors.TextMuted, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.weight(1f))
        if (action != null) {
            Row(
                Modifier.height(44.dp).clip(ZonaShapes.Pill).clickable(role = Role.Button, onClick = onAction).padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(action, color = ZonaColors.Link, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                ZIcon(ZIcons.ChevronRight, size = 16.dp, tint = ZonaColors.Link)
            }
        }
    }
}

@Composable
fun ProgressLine(fraction: Float, modifier: Modifier = Modifier, track: Color = Color(0x40FFFFFF), height: Dp = 3.dp) {
    Box(modifier.height(height).clip(ZonaShapes.Pill).background(track)) {
        Box(Modifier.fillMaxHeight().fillMaxWidth(fraction.coerceIn(0f, 1f)).clip(ZonaShapes.Pill).background(ZonaColors.Blue))
    }
}

@Composable
fun RatingBadge(value: String, modifier: Modifier = Modifier) {
    Box(
        modifier.height(20.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xC707090B)).padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center,
    ) { Text(value, color = ZonaColors.Amber, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
}

/** Title lettered in its own face, as on printed key art. */
@Composable
fun PosterTitle(title: Title, modifier: Modifier = Modifier, scale: Float = 1f) {
    val l = title.lettering
    Text(
        title.name.uppercase(),
        modifier = modifier,
        textAlign = TextAlign.Center,
        style = TextStyle(
            fontFamily = Zona.fonts.of(l.face),
            fontSize = (l.sizeSp * scale).sp,
            lineHeight = (l.sizeSp * scale * .98f).sp,
            letterSpacing = l.trackingEm.em,
            color = l.color,
            shadow = Shadow(Color(0x99000000), Offset(0f, 2f), 12f),
        ),
    )
}

@Composable
fun Poster(
    title: Title,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showRating: Boolean = true,
    badge: (@Composable BoxScope.() -> Unit)? = null,
    lettering: Float = 1f,
) {
    Box(
        modifier.clip(ZonaShapes.Poster).clickable(role = Role.Button, onClick = onClick).semantics { contentDescription = title.name },
    ) {
        Artwork(title.art.poster, Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().bottomFade(.5f, Color(0xD907090B)))
        if (showRating) RatingBadge(title.kp, Modifier.align(Alignment.TopStart).padding(6.dp))
        badge?.invoke(this)
        PosterTitle(title, Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(start = 6.dp, end = 6.dp, bottom = 10.dp), lettering)
    }
}

/** A 16:9 still with play affordance, caption and watch progress. */
@Composable
fun ProgressStill(
    image: DrawableResource,
    title: String,
    caption: String,
    fraction: Float?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    alignX: Float = 0f,
) {
    Box(modifier.clip(RoundedCornerShape(14.dp)).clickable(role = Role.Button, onClick = onClick)) {
        Artwork(image, Modifier.fillMaxSize(), alignX = alignX)
        Box(Modifier.fillMaxSize().bottomFade(.4f, Color(0xCC07090B)))
        Row(
            Modifier.align(Alignment.BottomStart).padding(start = 12.dp, end = 12.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(36.dp).glass(CircleShape), contentAlignment = Alignment.Center) { ZIcon(ZIcons.Play, size = 13.dp) }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(caption, color = ZonaColors.TextSecondary, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        if (fraction != null) {
            ProgressLine(fraction, Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(start = 12.dp, end = 12.dp, bottom = 7.dp))
        }
    }
}

enum class Tab(val label: String, val icon: ImageVector, val active: ImageVector) {
    Home("Главная", ZIcons.HomeOutline, ZIcons.Home),
    Catalog("Каталог", ZIcons.Catalog, ZIcons.CatalogFilled),
    My("Моё", ZIcons.Bookmark, ZIcons.BookmarkFilled),
    Search("Поиск", ZIcons.Search, ZIcons.SearchBold),
    Profile("Профиль", ZIcons.Profile, ZIcons.ProfileFilled),
}

/** Floating glass tab bar; content stays visible beneath it. */
@Composable
fun FloatingNavBar(selected: Tab, onSelect: (Tab) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .navigationBarsPadding()
            .padding(start = 12.dp, end = 12.dp, bottom = 16.dp)
            .fillMaxWidth()
            .height(64.dp)
            .glass(strong = true)
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Tab.entries.forEach { tab ->
            val on = tab == selected
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(ZonaShapes.Pill)
                    .clickable(role = Role.Tab) { onSelect(tab) }
                    .semantics { this.selected = on },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                ZIcon(if (on) tab.active else tab.icon, size = 24.dp, tint = if (on) ZonaColors.Blue else ZonaColors.TextMuted)
                Spacer(Modifier.height(3.dp))
                Text(
                    tab.label,
                    fontSize = 11.sp,
                    fontWeight = if (on) FontWeight.Bold else FontWeight.Medium,
                    color = if (on) ZonaColors.Text else ZonaColors.TextMuted,
                )
            }
        }
    }
}

val ScreenPadding = PaddingValues(horizontal = 16.dp)

@Composable
fun RowScope.Dot(color: Color, width: Dp = 6.dp) {
    Box(Modifier.width(width).height(4.dp).clip(ZonaShapes.Pill).background(color))
}

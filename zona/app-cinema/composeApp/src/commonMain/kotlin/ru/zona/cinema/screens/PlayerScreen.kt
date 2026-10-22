package ru.zona.cinema.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import ru.zona.cinema.Navigator
import ru.zona.cinema.Screen
import ru.zona.cinema.data.Demo
import ru.zona.cinema.data.Voice
import ru.zona.cinema.theme.ZonaColors
import ru.zona.cinema.theme.ZonaShapes
import ru.zona.cinema.ui.Artwork
import ru.zona.cinema.ui.RoundIconButton
import ru.zona.cinema.ui.ZIcon
import ru.zona.cinema.ui.ZIcons
import ru.zona.cinema.ui.glass

private const val EPISODE_SECONDS = 50 * 60 + 4
private const val INTRO_END = 100

/**
 * Player chrome. There is no video pipeline here: a still stands in for the frame and the
 * clock advances while "playing", so the controls can be exercised. Hook up Media3/ExoPlayer
 * (as in the original app) to drive [position] and [playing] for real.
 */
@Composable
fun PlayerScreen(args: Screen.Player, nav: Navigator, requestLandscape: (Boolean) -> Unit) {
    val title = Demo.byId(args.titleId)
    DisposableEffect(Unit) {
        requestLandscape(true)
        onDispose { requestLandscape(false) }
    }

    var episode by remember { mutableIntStateOf(args.episode) }
    var position by remember(episode) { mutableIntStateOf(if (episode == args.episode) 65 else 0) }
    var playing by remember { mutableStateOf(!args.streamsOpen) }
    var chrome by remember { mutableStateOf(true) }
    var streamsOpen by remember { mutableStateOf(args.streamsOpen) }
    var voice by remember { mutableStateOf(Demo.voices.first()) }
    var quality by remember { mutableStateOf("1080p") }
    var subtitles by remember { mutableStateOf(false) }
    var speed by remember { mutableStateOf(1f) }

    LaunchedEffect(playing, episode) {
        while (playing && position < EPISODE_SECONDS) {
            delay(1000)
            position += 1
        }
    }
    LaunchedEffect(chrome, playing, streamsOpen) {
        if (chrome && playing && !streamsOpen) {
            delay(4000)
            chrome = false
        }
    }

    Box(
        Modifier.fillMaxSize().background(Color.Black).pointerInput(Unit) {
            detectTapGestures(onTap = { chrome = !chrome })
        },
    ) {
        Artwork(title.art.wide, Modifier.fillMaxSize(), alignX = -.3f)

        AnimatedVisibility(chrome && !streamsOpen, enter = fadeIn(), exit = fadeOut()) {
            Box(Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxWidth().height(90.dp).background(Brush.verticalGradient(listOf(Color(0x9907090B), Color.Transparent))))
                Box(
                    Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(120.dp)
                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xB307090B)))),
                )
                Box(Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = 24.dp, vertical = 12.dp)) {
                    TopBar(title.name, if (title.isSeries) "2 сезон · $episode серия" else title.kind, onBack = { nav.back() })
                    CenterControls(
                        playing = playing,
                        onToggle = { playing = !playing; chrome = true },
                        onSeek = { delta -> position = (position + delta).coerceIn(0, EPISODE_SECONDS); chrome = true },
                        modifier = Modifier.align(Alignment.Center),
                    )
                    if (position in 5 until INTRO_END) {
                        Row(
                            Modifier.align(Alignment.BottomEnd).padding(bottom = 92.dp).height(40.dp).clip(ZonaShapes.Pill)
                                .background(Color.White).clickable(role = Role.Button) { position = INTRO_END }.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Пропустить заставку", color = ZonaColors.Bg, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(6.dp))
                            ZIcon(ZIcons.Skip, size = 14.dp, tint = ZonaColors.Bg)
                        }
                    }
                    Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth()) {
                        Scrubber(position, buffered = (position + 600).coerceAtMost(EPISODE_SECONDS), onSeek = { position = it; chrome = true })
                        Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            PlayerChip("${voice.name} · $quality", ZIcons.Mic) { streamsOpen = true }
                            PlayerChip(if (subtitles) "Субтитры: вкл" else "Субтитры", ZIcons.Subtitles, active = subtitles) { subtitles = !subtitles }
                            PlayerChip(formatSpeed(speed), null) { speed = nextSpeed(speed) }
                            if (title.isSeries) PlayerChip("Серии", ZIcons.Episodes) { nav.back() }
                            Spacer(Modifier.weight(1f))
                            if (title.isSeries) {
                                PlayerChip("Следующая серия", ZIcons.Next, trailingIcon = true, bold = true) { episode += 1; playing = true }
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            streamsOpen,
            modifier = Modifier.align(Alignment.CenterEnd),
            enter = slideInHorizontally { it } + fadeIn(),
            exit = slideOutHorizontally { it } + fadeOut(),
        ) {
            Box(Modifier.fillMaxSize().background(Color(0x8007090B)).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { streamsOpen = false }) {
                StreamsPanel(
                    voice = voice,
                    quality = quality,
                    onVoice = { v -> voice = v; if (quality !in v.qualities && quality != "Авто") quality = v.qualities.first() },
                    onQuality = { quality = it },
                    onClose = { streamsOpen = false },
                    modifier = Modifier.align(Alignment.CenterEnd).safeDrawingPadding().padding(12.dp),
                )
                Text(
                    "Видео продолжает идти · ${formatTime(position)}",
                    modifier = Modifier.align(Alignment.BottomStart).padding(24.dp),
                    color = ZonaColors.TextSecondary, fontSize = 13.sp,
                )
            }
        }
    }
}

@Composable
private fun TopBar(name: String, subtitle: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        RoundIconButton(ZIcons.Back, "Назад", onBack)
        Column(Modifier.weight(1f).padding(start = 10.dp)) {
            Text(name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, fontSize = 13.sp, color = ZonaColors.TextSecondary)
        }
        Row(Modifier.glass()) {
            RoundIconButton(ZIcons.Cast, "Транслировать на ТВ", {}, glass = false)
            RoundIconButton(ZIcons.Pip, "Картинка в картинке", {}, glass = false)
            RoundIconButton(ZIcons.Lock, "Заблокировать экран", {}, glass = false)
        }
    }
}

@Composable
private fun CenterControls(playing: Boolean, onToggle: () -> Unit, onSeek: (Int) -> Unit, modifier: Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(44.dp), verticalAlignment = Alignment.CenterVertically) {
        SeekButton(ZIcons.Rewind, "Назад на 10 секунд") { onSeek(-10) }
        Box(
            Modifier.size(80.dp).glass(CircleShape).clickable(role = Role.Button, onClick = onToggle)
                .semantics { contentDescription = if (playing) "Пауза" else "Воспроизвести" },
            contentAlignment = Alignment.Center,
        ) { ZIcon(if (playing) ZIcons.Pause else ZIcons.Play, size = 30.dp) }
        SeekButton(ZIcons.Forward, "Вперёд на 10 секунд") { onSeek(10) }
    }
}

@Composable
private fun SeekButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Box(
        Modifier.size(58.dp).glass(CircleShape).clickable(role = Role.Button, onClick = onClick).semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        ZIcon(icon, size = 32.dp)
        Text("10", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.offset(y = 1.dp))
    }
}

@Composable
private fun Scrubber(position: Int, buffered: Int, onSeek: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(formatTime(position), fontSize = 13.sp, fontWeight = FontWeight.Medium)
        BoxWithConstraints(
            Modifier.weight(1f).height(32.dp).padding(horizontal = 14.dp)
                .semantics {
                    contentDescription = "Позиция просмотра"
                    progressBarRangeInfo = ProgressBarRangeInfo(position.toFloat(), 0f..EPISODE_SECONDS.toFloat())
                }
                .pointerInput(Unit) {
                    detectTapGestures { o -> onSeek((o.x / size.width * EPISODE_SECONDS).toInt()) }
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { change, _ -> onSeek((change.position.x / size.width * EPISODE_SECONDS).toInt().coerceIn(0, EPISODE_SECONDS)) }
                },
            contentAlignment = Alignment.CenterStart,
        ) {
            val played = position.toFloat() / EPISODE_SECONDS
            Box(Modifier.fillMaxWidth().height(4.dp).clip(ZonaShapes.Pill).background(Color(0x47FFFFFF)))
            Box(Modifier.fillMaxWidth(buffered.toFloat() / EPISODE_SECONDS).height(4.dp).clip(ZonaShapes.Pill).background(Color(0x80FFFFFF)))
            Box(Modifier.fillMaxWidth(played).height(4.dp).clip(ZonaShapes.Pill).background(ZonaColors.Blue))
            Box(
                Modifier.offset(x = maxWidth * played - 8.dp).size(16.dp).clip(CircleShape).background(Color(0x800094F6)).padding(3.dp)
                    .clip(CircleShape).background(Color.White),
            )
        }
        Text("−" + formatTime(EPISODE_SECONDS - position), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = ZonaColors.TextSecondary)
    }
}

@Composable
private fun PlayerChip(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector?,
    active: Boolean = false,
    trailingIcon: Boolean = false,
    bold: Boolean = false,
    onClick: () -> Unit,
) {
    Row(
        Modifier.height(36.dp).then(if (active) Modifier.clip(ZonaShapes.Pill).background(Color.White) else Modifier.glass())
            .clickable(role = Role.Button, onClick = onClick).padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val fg = if (active) ZonaColors.Bg else ZonaColors.Text
        if (icon != null && !trailingIcon) ZIcon(icon, size = 16.dp, tint = fg)
        Text(text, fontSize = 13.sp, fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium, color = fg, maxLines = 1)
        if (icon != null && trailingIcon) ZIcon(icon, size = 14.dp, tint = fg)
    }
}

@Composable
fun StreamsPanel(
    voice: Voice,
    quality: String,
    onVoice: (Voice) -> Unit,
    onQuality: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .width(404.dp)
            .fillMaxHeight()
            .clip(ZonaShapes.Panel)
            .background(Color(0xE0111519))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
            .padding(start = 16.dp, end = 6.dp, top = 6.dp, bottom = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Озвучка и качество", fontSize = 18.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
            RoundIconButton(ZIcons.Close, "Закрыть", onClose, glass = false)
        }
        Label("ОЗВУЧКА")
        Column(Modifier.padding(end = 10.dp, top = 6.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Demo.voices.forEach { v ->
                val on = v == voice
                Row(
                    Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(12.dp))
                        .background(if (on) ZonaColors.BlueTint else Color.Transparent)
                        .clickable(role = Role.RadioButton) { onVoice(v) }
                        .semantics { selected = on }
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier.size(20.dp).clip(CircleShape).background(if (on) ZonaColors.Blue else ZonaColors.Outline).padding(2.dp)
                            .clip(CircleShape).background(if (on) Color(0xFF111519) else Color(0xFF111519)).padding(3.dp)
                            .clip(CircleShape).background(if (on) ZonaColors.Blue else Color(0xFF111519)),
                    )
                    Text(v.name, fontSize = 15.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f).padding(start = 12.dp))
                    if (v.subtitles) {
                        Box(Modifier.padding(end = 8.dp).height(20.dp).clip(RoundedCornerShape(6.dp)).background(ZonaColors.Surface3).padding(horizontal = 6.dp), contentAlignment = Alignment.Center) {
                            Text("СУБ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ZonaColors.TextSecondary)
                        }
                    }
                    Text(v.qualities.joinToString(" · "), fontSize = 12.sp, color = ZonaColors.TextMuted)
                }
            }
        }
        Label("КАЧЕСТВО · ${voice.name.uppercase()}", Modifier.padding(top = 10.dp))
        Row(Modifier.padding(top = 8.dp, end = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (voice.qualities + "Авто").forEach { q ->
                val on = q == quality
                Column(
                    Modifier.weight(1f).height(52.dp).clip(RoundedCornerShape(14.dp))
                        .background(if (on) Color.White else ZonaColors.Surface2)
                        .clickable(role = Role.RadioButton) { onQuality(q) }
                        .semantics { selected = on },
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(q, fontSize = 17.sp, fontWeight = FontWeight.Black, color = if (on) ZonaColors.Bg else ZonaColors.Text)
                    Text(qualityHint(q), fontSize = 11.sp, color = if (on) ZonaColors.Bg else ZonaColors.TextMuted)
                }
            }
        }
    }
}

@Composable
private fun Label(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier = modifier.padding(top = 4.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ZonaColors.TextMuted, letterSpacing = 0.7.sp)
}

private fun qualityHint(q: String) = when (q) {
    "1080p" -> "FullHD"
    "720p" -> "HD"
    "480p" -> "SD"
    else -> "по сети"
}

private val speeds = listOf(1f, 1.25f, 1.5f, 2f, .75f)
private fun nextSpeed(s: Float) = speeds[(speeds.indexOf(s) + 1) % speeds.size]
private fun formatSpeed(s: Float) = (if (s % 1f == 0f) s.toInt().toString() else s.toString().replace('.', ',')) + "×"

fun formatTime(seconds: Int): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    val mm = if (h > 0) m.toString().padStart(2, '0') else m.toString()
    return (if (h > 0) "$h:" else "") + "$mm:" + s.toString().padStart(2, '0')
}

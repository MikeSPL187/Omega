package ru.zona.cinema.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
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
import ru.zona.cinema.player.VideoSource
import ru.zona.cinema.player.VideoSurface
import ru.zona.cinema.player.rememberVideoPlayer
import ru.zona.cinema.theme.ZonaColors
import ru.zona.cinema.theme.ZonaShapes
import ru.zona.cinema.ui.Artwork
import ru.zona.cinema.ui.PrimaryButton
import ru.zona.cinema.ui.RoundIconButton
import ru.zona.cinema.ui.ZIcon
import ru.zona.cinema.ui.ZIcons
import ru.zona.cinema.ui.focusRing
import ru.zona.cinema.ui.glass

private const val INTRO_END = 100
private const val START_SECONDS = 65

/**
 * Player screen. Playback runs through [VideoPlayer] (Media3 ExoPlayer on Android, a clock on
 * desktop); position, pause, speed, subtitles and quality are read from and sent to it.
 */
@Composable
fun PlayerScreen(args: Screen.Player, nav: Navigator, requestLandscape: (Boolean) -> Unit) {
    val title = Demo.byId(args.titleId)
    DisposableEffect(Unit) {
        requestLandscape(true)
        onDispose { requestLandscape(false) }
    }

    val player = rememberVideoPlayer()
    var episode by remember { mutableIntStateOf(args.episode) }
    var chrome by remember { mutableStateOf(true) }
    var streamsOpen by remember { mutableStateOf(args.streamsOpen) }
    var voice by remember { mutableStateOf(Demo.voices.first()) }
    var quality by remember { mutableStateOf("1080p") }
    var subtitles by remember { mutableStateOf(false) }
    var speed by remember { mutableStateOf(1f) }
    // Remote / keyboard: the first key reveals the controls and focuses play; `poke` restarts auto-hide.
    val rootFocus = remember { FocusRequester() }
    val playFocus = remember { FocusRequester() }
    val panelFocus = remember { FocusRequester() }
    var keyMode by remember { mutableStateOf(false) }
    var poke by remember { mutableIntStateOf(0) }

    LaunchedEffect(episode) {
        val resume = episode == args.episode
        player.load(
            VideoSource(Demo.streamFor(title, args.season, episode), title.name),
            startMs = if (resume) START_SECONDS * 1000L else 0L,
            play = !(resume && args.streamsOpen),
        )
    }
    LaunchedEffect(speed) { player.setPlaybackSpeed(speed) }
    LaunchedEffect(subtitles) { player.setSubtitles(subtitles) }
    LaunchedEffect(quality) { player.setMaxHeight(quality.removeSuffix("p").toIntOrNull()) }

    val position = (player.positionMs / 1000).toInt()
    val duration = (player.durationMs / 1000).toInt()
    val seek = { seconds: Int -> player.seekTo(seconds * 1000L); chrome = true }

    LaunchedEffect(chrome, streamsOpen, keyMode) {
        runCatching {
            when {
                streamsOpen -> if (keyMode) panelFocus.requestFocus()
                chrome && keyMode -> playFocus.requestFocus()
                else -> rootFocus.requestFocus()
            }
        }
    }
    LaunchedEffect(chrome, player.playing, streamsOpen, poke) {
        if (chrome && player.playing && !streamsOpen) {
            delay(4000)
            chrome = false
        }
    }

    Box(
        Modifier.fillMaxSize().background(Color.Black)
            .focusRequester(rootFocus)
            .onPreviewKeyEvent { e ->
                if (e.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                poke++
                when (e.key) {
                    Key.MediaPlayPause -> { player.setPlayWhenReady(!player.playing); chrome = true; true }
                    Key.MediaPlay -> { player.setPlayWhenReady(true); true }
                    Key.MediaPause -> { player.setPlayWhenReady(false); chrome = true; true }
                    Key.MediaFastForward -> { seek((position + 10).coerceAtMost(duration)); true }
                    Key.MediaRewind -> { seek((position - 10).coerceAtLeast(0)); true }
                    Key.Back, Key.Escape -> if (streamsOpen) { streamsOpen = false; true } else false
                    else -> {
                        val wake = (!chrome && !streamsOpen) || !keyMode
                        keyMode = true
                        chrome = true
                        wake
                    }
                }
            }
            .focusable(),
    ) {
        VideoSurface(player, liftSubtitles = chrome && !streamsOpen, modifier = Modifier.fillMaxSize())
        if (!player.hasFrame) Artwork(title.art.wide, Modifier.fillMaxSize(), alignX = -.3f)
        // Taps on the picture toggle the controls; buttons drawn above take their own taps.
        Box(Modifier.fillMaxSize().pointerInput(Unit) { detectTapGestures(onTap = { chrome = !chrome }) })
        if (player.buffering && player.error == null) {
            CircularProgressIndicator(
                Modifier.align(Alignment.Center).size(92.dp).semantics { contentDescription = "Загрузка видео" },
                color = ZonaColors.Blue, strokeWidth = 3.dp, trackColor = Color.Transparent,
            )
        }

        AnimatedVisibility(chrome && !streamsOpen, enter = fadeIn(), exit = fadeOut()) {
            Box(Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxWidth().height(90.dp).background(Brush.verticalGradient(listOf(Color(0x9907090B), Color.Transparent))))
                Box(
                    Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(120.dp)
                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xB307090B)))),
                )
                Box(Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = 24.dp, vertical = 12.dp)) {
                    TopBar(title.name, if (title.isSeries) "${args.season} сезон · $episode серия" else title.kind, onBack = { nav.back() })
                    CenterControls(
                        playing = player.playing,
                        playFocus = playFocus,
                        onToggle = { player.setPlayWhenReady(!player.playing); chrome = true },
                        onSeek = { delta -> seek((position + delta).coerceIn(0, duration)) },
                        modifier = Modifier.align(Alignment.Center),
                    )
                    if (position in 5 until INTRO_END && duration > INTRO_END) {
                        Row(
                            Modifier.align(Alignment.BottomEnd).padding(bottom = 92.dp).height(44.dp).focusRing(ZonaShapes.Pill).clip(ZonaShapes.Pill)
                                .background(Color.White).clickable(role = Role.Button) { seek(INTRO_END) }.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Пропустить заставку", color = ZonaColors.Bg, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(6.dp))
                            ZIcon(ZIcons.Skip, size = 14.dp, tint = ZonaColors.Bg)
                        }
                    }
                    Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth()) {
                        Scrubber(position, duration, buffered = (player.bufferedMs / 1000).toInt(), onSeek = seek)
                        Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            PlayerChip("${voice.name} · $quality", ZIcons.Mic) { streamsOpen = true }
                            PlayerChip(if (subtitles) "Субтитры: вкл" else "Субтитры", ZIcons.Subtitles, active = subtitles) { subtitles = !subtitles }
                            PlayerChip(formatSpeed(speed), null) { speed = nextSpeed(speed) }
                            if (title.isSeries) PlayerChip("Серии", ZIcons.Episodes) { nav.back() }
                            Spacer(Modifier.weight(1f))
                            if (title.isSeries) {
                                PlayerChip("Следующая серия", ZIcons.Next, trailingIcon = true, bold = true) { episode += 1 }
                            }
                        }
                    }
                }
            }
        }

        player.error?.let { message ->
            ErrorOverlay(message, onRetry = { player.retry() }, onBack = { nav.back() })
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
                    firstFocus = panelFocus,
                    modifier = Modifier.align(Alignment.CenterEnd).safeDrawingPadding().padding(12.dp),
                )
                Text(
                    (if (player.playing) "Видео продолжает идти · " else "На паузе · ") + formatTime(position),
                    modifier = Modifier.align(Alignment.BottomStart).padding(24.dp),
                    color = ZonaColors.TextSecondary, fontSize = 13.sp,
                )
            }
        }
    }
}

@Composable
private fun ErrorOverlay(message: String, onRetry: () -> Unit, onBack: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color(0xCC07090B)).safeDrawingPadding().padding(24.dp)) {
        RoundIconButton(ZIcons.Back, "Назад", onBack)
        Column(Modifier.align(Alignment.Center).width(360.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Видео не загрузилось", fontSize = 20.sp, fontWeight = FontWeight.Black)
            Text(message, fontSize = 14.sp, color = ZonaColors.TextSecondary, modifier = Modifier.padding(top = 6.dp, bottom = 18.dp))
            PrimaryButton("Повторить", onRetry, icon = null)
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
private fun CenterControls(playing: Boolean, playFocus: FocusRequester, onToggle: () -> Unit, onSeek: (Int) -> Unit, modifier: Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(44.dp), verticalAlignment = Alignment.CenterVertically) {
        SeekButton(ZIcons.Rewind, "Назад на 10 секунд") { onSeek(-10) }
        Box(
            Modifier.size(80.dp).focusRequester(playFocus).focusRing(CircleShape).glass(CircleShape).clickable(role = Role.Button, onClick = onToggle)
                .semantics { contentDescription = if (playing) "Пауза" else "Воспроизвести" },
            contentAlignment = Alignment.Center,
        ) { ZIcon(if (playing) ZIcons.Pause else ZIcons.Play, size = 30.dp) }
        SeekButton(ZIcons.Forward, "Вперёд на 10 секунд") { onSeek(10) }
    }
}

@Composable
private fun SeekButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Box(
        Modifier.size(58.dp).focusRing(CircleShape).glass(CircleShape).clickable(role = Role.Button, onClick = onClick).semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        ZIcon(icon, size = 32.dp)
        Text("10", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.offset(y = 1.dp))
    }
}

@Composable
private fun Scrubber(position: Int, duration: Int, buffered: Int, onSeek: (Int) -> Unit) {
    // While dragging, the thumb follows the finger and the player seeks once on release.
    var dragged by remember { mutableStateOf<Int?>(null) }
    val shown = dragged ?: position
    val span = duration.coerceAtLeast(1)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(formatTime(shown), fontSize = 13.sp, fontWeight = FontWeight.Medium)
        BoxWithConstraints(
            Modifier.weight(1f).height(44.dp).padding(horizontal = 14.dp)
                .semantics {
                    contentDescription = "Позиция просмотра"
                    progressBarRangeInfo = ProgressBarRangeInfo(shown.toFloat(), 0f..span.toFloat())
                }
                .pointerInput(duration) {
                    detectTapGestures { o -> if (duration > 0) onSeek((o.x / size.width * duration).toInt().coerceIn(0, duration)) }
                }
                .pointerInput(duration) {
                    fun at(x: Float) = (x / size.width * duration).toInt().coerceIn(0, duration)
                    detectHorizontalDragGestures(
                        onDragStart = { o -> if (duration > 0) dragged = at(o.x) },
                        onDragEnd = { dragged?.let(onSeek); dragged = null },
                        onDragCancel = { dragged = null },
                    ) { change, _ -> if (duration > 0) dragged = at(change.position.x) }
                },
            contentAlignment = Alignment.CenterStart,
        ) {
            val played = (shown.toFloat() / span).coerceIn(0f, 1f)
            Box(Modifier.fillMaxWidth().height(4.dp).clip(ZonaShapes.Pill).background(Color(0x47FFFFFF)))
            Box(Modifier.fillMaxWidth((buffered.toFloat() / span).coerceIn(0f, 1f)).height(4.dp).clip(ZonaShapes.Pill).background(Color(0x80FFFFFF)))
            Box(Modifier.fillMaxWidth(played).height(4.dp).clip(ZonaShapes.Pill).background(ZonaColors.Blue))
            Box(
                Modifier.offset(x = maxWidth * played - 8.dp).size(16.dp).clip(CircleShape).background(Color(0x800094F6)).padding(3.dp)
                    .clip(CircleShape).background(Color.White),
            )
        }
        Text(if (duration > 0) "−" + formatTime(duration - shown) else "−:−−", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = ZonaColors.TextSecondary)
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
        Modifier.height(36.dp).focusRing(ZonaShapes.Pill).then(if (active) Modifier.clip(ZonaShapes.Pill).background(Color.White) else Modifier.glass())
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
    firstFocus: FocusRequester? = null,
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
                    Modifier.fillMaxWidth().height(44.dp)
                        .then(if (on && firstFocus != null) Modifier.focusRequester(firstFocus) else Modifier)
                        .focusRing(RoundedCornerShape(12.dp)).clip(RoundedCornerShape(12.dp))
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
                    Modifier.weight(1f).height(52.dp).focusRing(RoundedCornerShape(14.dp), color = ZonaColors.Blue).clip(RoundedCornerShape(14.dp))
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

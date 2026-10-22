package ru.zona.cinema.player

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.CaptionStyleCompat
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay

private const val POLL_MS = 250L

/** Media3 ExoPlayer behind [VideoPlayer]; positions are polled, everything else comes from listener callbacks. */
internal class ExoVideoPlayer(context: Context) : VideoPlayer {
    val exo: ExoPlayer = ExoPlayer.Builder(context)
        .setAudioAttributes(
            AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_MOVIE).build(),
            /* handleAudioFocus = */ true,
        )
        .setHandleAudioBecomingNoisy(true)
        .build()

    override var positionMs by mutableLongStateOf(0L)
        private set
    override var durationMs by mutableLongStateOf(0L)
        private set
    override var bufferedMs by mutableLongStateOf(0L)
        private set
    override var playing by mutableStateOf(false)
        private set
    override var buffering by mutableStateOf(false)
        private set
    override var hasFrame by mutableStateOf(false)
        private set
    override var error by mutableStateOf<String?>(null)
        private set

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) = sync()

        override fun onRenderedFirstFrame() { hasFrame = true }

        override fun onPlayerError(e: PlaybackException) {
            error = when (e.errorCode) {
                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED,
                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT -> "Нет соединения с сервером"
                PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS,
                PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND -> "Видео недоступно"
                else -> "Не удалось воспроизвести видео"
            } + " (${e.errorCodeName})"
        }
    }

    init { exo.addListener(listener) }

    /** Copies the player's state into Compose state; called on events and every [POLL_MS]. */
    fun sync() {
        val ended = exo.playbackState == Player.STATE_ENDED
        positionMs = exo.currentPosition.coerceAtLeast(0)
        bufferedMs = exo.bufferedPosition.coerceAtLeast(0)
        durationMs = exo.duration.takeIf { it != C.TIME_UNSET }?.coerceAtLeast(0) ?: 0
        playing = exo.playWhenReady && !ended
        buffering = exo.playbackState == Player.STATE_BUFFERING && exo.playWhenReady
    }

    override fun load(source: VideoSource, startMs: Long, play: Boolean) {
        val item = MediaItem.Builder()
            .setUri(source.url)
            .apply { if (source.url.substringBefore('?').endsWith(".m3u8")) setMimeType(MimeTypes.APPLICATION_M3U8) }
            .setMediaMetadata(MediaMetadata.Builder().setTitle(source.title).build())
            .build()
        hasFrame = false
        error = null
        exo.setMediaItem(item, startMs)
        exo.prepare()
        exo.playWhenReady = play
        sync()
    }

    override fun setPlayWhenReady(play: Boolean) {
        if (play && exo.playbackState == Player.STATE_ENDED) exo.seekTo(0)
        exo.playWhenReady = play
        sync()
    }

    override fun seekTo(ms: Long) {
        exo.seekTo(ms.coerceAtLeast(0))
        sync()
    }

    override fun setPlaybackSpeed(speed: Float) = exo.setPlaybackSpeed(speed)

    override fun setSubtitles(enabled: Boolean) {
        exo.trackSelectionParameters = exo.trackSelectionParameters.buildUpon()
            .setTrackTypeDisabled(C.TRACK_TYPE_TEXT, !enabled)
            .setPreferredTextLanguages("ru", "en")
            .setSelectUndeterminedTextLanguage(true)
            .build()
    }

    override fun setMaxHeight(height: Int?) {
        exo.trackSelectionParameters = exo.trackSelectionParameters.buildUpon()
            .apply { if (height == null) clearVideoSizeConstraints() else setMaxVideoSize(Int.MAX_VALUE, height) }
            .build()
    }

    override fun retry() {
        error = null
        exo.prepare()
    }

    fun release() {
        exo.removeListener(listener)
        exo.release()
    }
}

@Composable
actual fun rememberVideoPlayer(): VideoPlayer {
    val context = LocalContext.current
    val player = remember { ExoVideoPlayer(context.applicationContext) }
    DisposableEffect(player) { onDispose { player.release() } }

    // Pause when the app goes to the background; the user resumes with the play button.
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, player) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) player.setPlayWhenReady(false)
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(player) {
        while (true) {
            player.sync()
            delay(POLL_MS)
        }
    }
    return player
}

@OptIn(UnstableApi::class)
@Composable
actual fun VideoSurface(player: VideoPlayer, liftSubtitles: Boolean, modifier: Modifier) {
    val exo = (player as? ExoVideoPlayer)?.exo ?: return
    AndroidView(
        factory = { context ->
            PlayerView(context).apply {
                useController = false
                isClickable = false
                isFocusable = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                setShutterBackgroundColor(Color.BLACK)
                setKeepContentOnPlayerReset(true)
                subtitleView?.apply {
                    setStyle(CaptionStyleCompat(Color.WHITE, 0x99000000.toInt(), Color.TRANSPARENT, CaptionStyleCompat.EDGE_TYPE_NONE, Color.WHITE, null))
                    setFixedTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                }
                this.player = exo
            }
        },
        update = { view ->
            view.keepScreenOn = player.playing
            view.subtitleView?.setBottomPaddingFraction(if (liftSubtitles) .3f else .08f)
        },
        onRelease = { it.player = null },
        modifier = modifier,
    )
}

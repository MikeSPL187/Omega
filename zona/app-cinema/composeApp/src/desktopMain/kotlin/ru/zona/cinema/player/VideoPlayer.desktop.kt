package ru.zona.cinema.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

private const val PREVIEW_DURATION_MS = (50 * 60 + 4) * 1000L
private const val TICK_MS = 250L

/** Desktop preview: no decoder, just a clock so the controls can be exercised. */
private class PreviewPlayer : VideoPlayer {
    override var positionMs by mutableLongStateOf(0L)
        private set
    override var durationMs by mutableLongStateOf(0L)
        private set
    override val bufferedMs get() = (positionMs + 600_000).coerceAtMost(durationMs)
    override var playing by mutableStateOf(false)
        private set
    override val buffering = false
    override val hasFrame = false
    override val error: String? = null
    private var speed by mutableFloatStateOf(1f)

    override fun load(source: VideoSource, startMs: Long, play: Boolean) {
        durationMs = PREVIEW_DURATION_MS
        positionMs = startMs.coerceIn(0, durationMs)
        playing = play
    }

    override fun setPlayWhenReady(play: Boolean) {
        if (play && positionMs >= durationMs) positionMs = 0
        playing = play
    }

    override fun seekTo(ms: Long) { positionMs = ms.coerceIn(0, durationMs) }
    override fun setPlaybackSpeed(speed: Float) { this.speed = speed }
    override fun setSubtitles(enabled: Boolean) {}
    override fun setMaxHeight(height: Int?) {}
    override fun retry() {}

    fun tick() {
        if (!playing) return
        positionMs = (positionMs + (TICK_MS * speed).toLong()).coerceAtMost(durationMs)
        if (positionMs == durationMs) playing = false
    }
}

@Composable
actual fun rememberVideoPlayer(): VideoPlayer {
    val player = remember { PreviewPlayer() }
    LaunchedEffect(player) {
        while (true) {
            delay(TICK_MS)
            player.tick()
        }
    }
    return player
}

@Composable
actual fun VideoSurface(player: VideoPlayer, liftSubtitles: Boolean, modifier: Modifier) {
    // Nothing to draw: the screen shows the artwork while hasFrame is false.
}

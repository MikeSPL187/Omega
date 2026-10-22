package ru.zona.cinema.player

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** A stream to play; [url] is an HLS playlist or a progressive file. */
data class VideoSource(val url: String, val title: String)

/**
 * What the player screen needs from a video engine. Properties are Compose state, so the
 * chrome recomposes as playback advances. Android drives it with Media3 ExoPlayer; the desktop
 * preview uses a clock without video.
 */
interface VideoPlayer {
    val positionMs: Long
    /** 0 while the duration is not known yet. */
    val durationMs: Long
    val bufferedMs: Long
    /** The user wants playback (play/pause button state); stays true while buffering. */
    val playing: Boolean
    val buffering: Boolean
    /** True once a video frame is on screen; until then the screen shows the title artwork. */
    val hasFrame: Boolean
    /** Human-readable reason playback stopped, or null. */
    val error: String?

    fun load(source: VideoSource, startMs: Long, play: Boolean)
    fun setPlayWhenReady(play: Boolean)
    fun seekTo(ms: Long)
    fun setPlaybackSpeed(speed: Float)
    fun setSubtitles(enabled: Boolean)
    /** Caps the video height (1080, 720, 480…); null lets the player adapt to the network. */
    fun setMaxHeight(height: Int?)
    fun retry()
}

/** A player tied to the composition: created once and released when it leaves. */
@Composable
expect fun rememberVideoPlayer(): VideoPlayer

/**
 * Renders the video and its subtitles. [liftSubtitles] moves subtitles above the player
 * controls while they are shown.
 */
@Composable
expect fun VideoSurface(player: VideoPlayer, liftSubtitles: Boolean, modifier: Modifier = Modifier)

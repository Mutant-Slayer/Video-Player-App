package com.example.movies.player

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.annotation.OptIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaItem.DrmConfiguration
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.PlayerSurface
import com.example.movies.R
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

private const val REWIND_MS = 10000
private const val UPDATE_INTERVAL = 1000L

@OptIn(UnstableApi::class)
@Composable
fun rememberExoPlayer(onVideoSizeChanged: (Float) -> Unit): ExoPlayer {
    val context = LocalContext.current
    return remember {
        val mediaItem = MediaItem.Builder().apply {
            setUri(Utils.STREAM_URL)
            setDrmConfiguration(
                DrmConfiguration.Builder(C.WIDEVINE_UUID)
                    .setLicenseUri(Utils.LICENSE_URL)
                    .build()
            )
        }
        ExoPlayer.Builder(context)
            .setSeekBackIncrementMs(REWIND_MS.toLong())
            .setSeekForwardIncrementMs(REWIND_MS.toLong()).build().apply {
                playbackParameters = playbackParameters.withSpeed(1.0f)
                addListener(object : Player.Listener {
                    override fun onVideoSizeChanged(videoSize: VideoSize) {
                        super.onVideoSizeChanged(videoSize)
                        if (videoSize != VideoSize.UNKNOWN) {
                            val videoWidth = videoSize.width.toFloat()
                            val videoHeight = videoSize.height.toFloat()
                            val aspectRatio = videoWidth / videoHeight
                            onVideoSizeChanged(aspectRatio)
                        }
                    }
                })
                setMediaItem(mediaItem.build())
                prepare()
                play()
            }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var isFullScreen by remember { mutableStateOf(false) }
    var videoAspectRatio by remember { mutableFloatStateOf(16f / 9f) }
    var showControls by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(false) }
    val lifecycleEvent = rememberLifecycleEvent()

    var currentPosition by remember { mutableLongStateOf(0L) }
    var duration by remember { mutableLongStateOf(0L) }
    var isSeeking by remember { mutableStateOf(false) }
    var seekPosition by remember { mutableFloatStateOf(0f) }

    val player = rememberExoPlayer { aspectRatio ->
        videoAspectRatio = aspectRatio
    }

    LaunchedEffect(player) {
        while (true) {
            if (!isSeeking && player.isPlaying) {
                currentPosition = player.currentPosition
                if (duration == 0L) {
                    duration = player.duration.coerceAtLeast(0L)
                }
            }
            delay(UPDATE_INTERVAL)
        }
    }

    LaunchedEffect(player) {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                isBuffering = playbackState == Player.STATE_BUFFERING

                if (playbackState == Player.STATE_READY) {
                    duration = player.duration.coerceAtLeast(0L)
                }
            }
        })
    }

    LaunchedEffect(showControls) {
        if (showControls) {
            delay(3000)
            showControls = false
        }
    }

    BackHandler(enabled = isFullScreen) {
        isFullScreen = false
        setOrientation(context, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
    }

    LaunchedEffect(isFullScreen) {
        val window = (context as Activity).window
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        if (isFullScreen) {
            setOrientation(context, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            setOrientation(context, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    Box {
        PlayerSurface(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(videoAspectRatio)
                .clickable {
                    showControls = !showControls
                },
            player = player
        )

        if (isBuffering) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color(0x40000000)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        if (showControls) {
            VideoOverlay(
                modifier = Modifier.matchParentSize(),
                isFullScreen = isFullScreen,
                isPlaying = isPlaying,
                isBuffering = isBuffering,
                currentPosition = currentPosition,
                duration = duration,
                onPositionChange = { position ->
                    isSeeking = true
                    seekPosition = position
                },
                onSeekCompleted = {
                    isSeeking = false
                    player.seekTo((seekPosition * duration).toLong())
                },
                onExpandClick = {
                    isFullScreen = !isFullScreen
                    showControls = true
                },
                onPlayPauseClick = {
                    if (isPlaying) {
                        player.pause()
                    } else {
                        player.play()
                    }
                    showControls = true
                },
                onForwardClick = {
                    player.seekForward()
                    showControls = true
                },
                onRewindClick = {
                    player.seekBack()
                    showControls = true
                }
            )
        }
    }

    DisposableEffect(player) {
        onDispose {
            if (isFullScreen) {
                setOrientation(context, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
            }
            player.release()
        }
    }

    LaunchedEffect(lifecycleEvent) {
        if (lifecycleEvent == Lifecycle.Event.ON_PAUSE || lifecycleEvent == Lifecycle.Event.ON_STOP) {
            player.pause()
        } else if (lifecycleEvent == Lifecycle.Event.ON_RESUME) {
            player.play()
        }
    }
}

@Composable
fun VideoOverlay(
    modifier: Modifier = Modifier,
    isFullScreen: Boolean,
    isPlaying: Boolean,
    isBuffering: Boolean,
    currentPosition: Long,
    duration: Long,
    onPositionChange: (Float) -> Unit,
    onSeekCompleted: () -> Unit,
    onExpandClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onForwardClick: () -> Unit,
    onRewindClick: () -> Unit
) {
    Box(modifier = modifier) {
        PlaybackControls(
            modifier = Modifier.matchParentSize(),
            isFullScreen = isFullScreen,
            isPlaying = isPlaying,
            isBuffering = isBuffering,
            currentPosition = currentPosition,
            duration = duration,
            onPositionChange = onPositionChange,
            onSeekCompleted = onSeekCompleted,
            onExpandClick = onExpandClick,
            onPlayPauseClick = onPlayPauseClick,
            onForwardClick = onForwardClick,
            onRewindClick = onRewindClick
        )
    }
}

@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaybackControls(
    modifier: Modifier = Modifier,
    isFullScreen: Boolean = false,
    isPlaying: Boolean,
    isBuffering: Boolean,
    currentPosition: Long,
    duration: Long,
    onPositionChange: (Float) -> Unit,
    onSeekCompleted: () -> Unit,
    onExpandClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onForwardClick: () -> Unit,
    onRewindClick: () -> Unit
) {
    val progress = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f

    Box(
        modifier = modifier
            .background(Color(0xA0000000))
            .padding(8.dp)
    ) {
        Row(modifier = Modifier.align(Alignment.TopEnd)) {
            PlaybackButton(
                resourceId = if (isFullScreen) R.drawable.ic_collapse else R.drawable.ic_expand,
                description = if (isFullScreen) "Exit Fullscreen" else "Enter Fullscreen"
            ) {
                onExpandClick()
            }
        }

        Row(
            modifier = Modifier.align(Alignment.Center),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PlaybackButton(
                resourceId = R.drawable.ic_forward,
                description = "Rewind 10 seconds",
                modifier = Modifier
                    .size(40.dp)
                    .graphicsLayer(scaleX = -1f),
                enabled = !isBuffering
            ) {
                onRewindClick()
            }

            PlaybackButton(
                resourceId = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play,
                description = if (isPlaying) "Pause" else "Play",
                modifier = Modifier.size(40.dp),
                enabled = !isBuffering
            ) {
                if (!isBuffering) {
                    onPlayPauseClick()
                }
            }

            PlaybackButton(
                resourceId = R.drawable.ic_forward,
                description = "Fast Forward 10 seconds",
                modifier = Modifier.size(40.dp),
                enabled = !isBuffering
            ) {
                onForwardClick()
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatDuration(currentPosition),
                    color = Color.White,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )

                Slider(
                    value = progress,
                    onValueChange = onPositionChange,
                    onValueChangeFinished = onSeekCompleted,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                    ),
                    enabled = !isBuffering && duration > 0
                )
                Text(
                    text = formatDuration(duration),
                    color = Color.White,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun PlaybackButton(
    modifier: Modifier = Modifier,
    @DrawableRes resourceId: Int,
    description: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Image(
        modifier = modifier
            .size(32.dp)
            .clickable(enabled = enabled) {
                onClick()
            }
            .let {
                if (!enabled) it.graphicsLayer(alpha = 0.5f) else it
            },
        painter = painterResource(resourceId),
        contentDescription = description
    )
}

@Composable
fun rememberLifecycleEvent(lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current): Lifecycle.Event {
    var state by remember { mutableStateOf(Lifecycle.Event.ON_ANY) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            state = event
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    return state
}

fun formatDuration(milliseconds: Long): String {
    if (milliseconds <= 0) return "0:00"

    val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60

    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}

private fun setOrientation(context: Context, orientation: Int) {
    val activity = context as? Activity
    activity?.requestedOrientation = orientation
}
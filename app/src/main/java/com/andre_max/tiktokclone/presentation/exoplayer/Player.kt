package com.andre_max.tiktokclone.presentation.exoplayer

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.view.View
import android.widget.ImageView
import com.andre_max.tiktokclone.R
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import timber.log.Timber

class Player(
    private val simpleExoplayerView: PlayerView,
    private val context: Context,
    private val url: String?,
    private val onVideoEnded: () -> Unit
) {

    private var currentWindow = 0
    private var playbackPosition = 0L
    private var isPlaying = false
    private var simpleExoPlayer: SimpleExoPlayer? = null

    fun startOrResumePlayer() {
        simpleExoplayerView.setControllerVisibilityListener { visibility ->
            simpleExoplayerView.hideController()
            Timber.d("visibility is $visibility")
            simpleExoplayerView.controllerAutoShow = false
            simpleExoplayerView.controllerShowTimeoutMs = 1
            simpleExoplayerView.controllerHideOnTouch = true
        }

        if (simpleExoPlayer == null) {
            Timber.d("Player is null")
            Timber.d("current window is $currentWindow and playback position is $playbackPosition")
            isPlaying = true
            initPlayer()
        } else {
            Timber.d("player is not null")
            isPlaying = true
            simpleExoPlayer?.seekTo(0, playbackPosition)
            simpleExoPlayer?.playWhenReady = true
        }
    }

    fun restartPlayer() {
        playbackPosition = 0
        startOrResumePlayer()
    }

    fun setUpPlayer(playBtn: ImageView) {
        startOrResumePlayer()

        simpleExoplayerView.setOnClickListener {
            doPlayerChange(playBtn)
        }
    }

    fun doPlayerChange(playBtn: ImageView) {
        Timber.d("IsPlaying is $isPlaying")
        if (isPlaying) {
            pausePlayer()
            playBtn.visibility = View.VISIBLE
        } else {
            startOrResumePlayer()
            playBtn.visibility = View.GONE
        }
        simpleExoplayerView.hideController()
    }


    private val playerListener = object : Player.EventListener {
        override fun onPlayerError(error: ExoPlaybackException) {
            Timber.e(error)
        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            simpleExoplayerView.hideController()
            when (playbackState) {
                Player.STATE_BUFFERING -> {
                    Timber.d("State is buffering")
                }
                Player.STATE_READY -> {
                    Timber.d("State is ready")
                }
                Player.STATE_ENDED -> {
                    Timber.d("State is ended")
                    onVideoEnded()
                }
                Player.STATE_IDLE -> {
                    Timber.d("State is idle")
                }

            }
        }
    }

    private fun initPlayer() {
        simpleExoPlayer = SimpleExoPlayer.Builder(context)
            .setUseLazyPreparation(true)
            .build()
        val mediaDataSourceFactory = DefaultDataSourceFactory(
            context,
            Util.getUserAgent(context, context.resources.getString(R.string.app_name))
        )


        val mediaSource = ProgressiveMediaSource.Factory(mediaDataSourceFactory)
            .createMediaSource( MediaItem.fromUri(Uri.parse(url)))

        simpleExoplayerView.player = simpleExoPlayer

        with(simpleExoPlayer ?: return) {
            setMediaSource(mediaSource, playbackPosition)
            playWhenReady = true
//            repeatMode = SimpleExoPlayer.REPEAT_MODE_ALL
            addListener(playerListener)
        }

        simpleExoplayerView.also {
            it.setShutterBackgroundColor(Color.TRANSPARENT)
            it.requestFocus()
            it.player = simpleExoPlayer
        }
        Timber.d("After simpleExoplayerView.requestFocus called.")
    }

    fun pausePlayer() {
        if (simpleExoPlayer != null) {
            isPlaying = false
            playbackPosition = simpleExoPlayer!!.currentPosition
            currentWindow = simpleExoPlayer!!.currentWindowIndex
            simpleExoPlayer?.playWhenReady = false
        }
    }

    fun stopPlayer() {
        pausePlayer()
        simpleExoPlayer?.release()
        simpleExoPlayer = null
    }

}
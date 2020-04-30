package com.github.luoyemyy.aclin.app.files

import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.get
import com.github.luoyemyy.aclin.app.R
import com.github.luoyemyy.aclin.app.databinding.FragmentPlayerBinding
import com.github.luoyemyy.aclin.ext.uri
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

class FilesPlayerFragment : AppCompatActivity() {

    private lateinit var mBinding: FragmentPlayerBinding
    private lateinit var mPresenter: Presenter
    private lateinit var mFullscreenButton: ImageButton
    private lateinit var mFullscreenExitButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.fragment_player)
        mPresenter = ViewModelProvider(this).get()
        val videoTrackSelectionFactory = AdaptiveTrackSelection.Factory()
        val trackSelector = DefaultTrackSelector(this, videoTrackSelectionFactory)
        val player = SimpleExoPlayer.Builder(this).setTrackSelector(trackSelector).build()
        mBinding.playerView.also {
            it.player = player
            mFullscreenButton = it.findViewById(R.id.exo_fullscreen)
            mFullscreenButton.setOnClickListener {
                fullscreen()
            }
            mFullscreenExitButton = it.findViewById(R.id.exo_fullscreen_exit)
            mFullscreenExitButton.setOnClickListener {
                fullscreenExit()
            }
        }
        visible()
        val dataSourceFactory = DefaultDataSourceFactory(this, Util.getUserAgent(this, "player"), DefaultBandwidthMeter.Builder(this).build())
        val videoSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(intent?.getStringExtra("url")))
        player.prepare(videoSource)
        player.playWhenReady = true
        player.seekTo(mPresenter.position)
    }

    override fun onDestroy() {
        mBinding.playerView.player?.apply {
            mPresenter.position = currentPosition
            stop()
            release()
        }
        super.onDestroy()
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun fullscreen() {
        if (mPresenter.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            mPresenter.orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private fun fullscreenExit() {
        if (mPresenter.orientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            mPresenter.orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    private fun visible() {
        if (mPresenter.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            mFullscreenButton.visibility = View.VISIBLE
            mFullscreenExitButton.visibility = View.GONE
        } else {
            mFullscreenButton.visibility = View.GONE
            mFullscreenExitButton.visibility = View.VISIBLE
        }
    }

    class Presenter(app: Application) : AndroidViewModel(app) {
        var position: Long = 0
        var orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }
}
package com.example.movies.player

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.movies.ui.theme.MoviesTheme

class PlaybackActivity : ComponentActivity() {

    companion object {
        const val VIDEO_URL = "video_url"
        const val LICENSE_URL = "license_url"

        fun newInstance(context: Context, videoUrl: String, licenseUrl: String): Intent {
            return Intent(context, PlaybackActivity::class.java).apply {
                putExtra(VIDEO_URL, videoUrl)
                putExtra(LICENSE_URL, licenseUrl)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MoviesTheme {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    PlayerScreen(
                        videoUrl = intent.getStringExtra(VIDEO_URL)!!,
                        licenseUrl = intent.getStringExtra(LICENSE_URL)!!
                    )
                }
            }
        }
    }
}
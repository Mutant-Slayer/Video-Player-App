package com.example.movies.home

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.movies.player.PlaybackActivity
import com.example.movies.ui.theme.MoviesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MoviesTheme {
                Surface {
                    HomeScreen(
                        startActivity = { videoUrl, licenseUrl ->
                            startActivity(
                                PlaybackActivity.newInstance(
                                    this@MainActivity,
                                    videoUrl,
                                    licenseUrl
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HomeScreen(startActivity: (videoUrl: String, licenseUrl: String) -> Unit) {
    val (videoUrl, setVideoUrl) = remember { mutableStateOf("https://bitmovin-a.akamaihd.net/content/art-of-motion_drm/mpds/11331.mpd") }
    val (licenseUrl, setLicenseUrl) = remember { mutableStateOf("https://cwip-shaka-proxy.appspot.com/no_auth") }

    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        TextField(
            label = { Text(text = "Enter video url") },
            value = videoUrl,
            onValueChange = setVideoUrl,
        )
        TextField(
            label = { Text(text = "Enter license url") },
            value = licenseUrl,
            onValueChange = setLicenseUrl,
        )
        Button(
            onClick = { startActivity.invoke(videoUrl, licenseUrl) }
        ) {
            Text(text = "Play Video")
        }
    }
}
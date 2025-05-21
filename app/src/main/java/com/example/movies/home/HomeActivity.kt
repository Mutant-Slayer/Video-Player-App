package com.example.movies.home

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.movies.R
import com.example.movies.player.PlaybackActivity
import com.example.movies.ui.theme.MoviesTheme

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MoviesTheme {
                Surface {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        Box(modifier = Modifier.clickable {
                            startActivity(PlaybackActivity.newInstance(this@HomeActivity))
                        }) {
                            CoverImage(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                image = R.drawable.ic_thumbnail
                            )
                            Image(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(10.dp),
                                painter = painterResource(id = R.drawable.ic_play),
                                contentDescription = "Play Video"
                            )
                        }
                        Text(
                            text = "Play Video",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CoverImage(modifier: Modifier = Modifier, image: Int) {
    Image(
        modifier = modifier,
        painter = painterResource(id = image),
        contentScale = ContentScale.Crop,
        contentDescription = "Play Video"
    )
}
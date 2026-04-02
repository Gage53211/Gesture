package com.example.button_application

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import coil3.compose.AsyncImage
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import com.example.button_application.ui.theme.Button_applicationTheme
import androidx.core.net.toUri
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    var picture by mutableStateOf<String?>("None")
    var title by mutableStateOf<String?>("None")
    var author by mutableStateOf<String?>("None")
    var album by mutableStateOf<String?>("None")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val metaFilter = IntentFilter("ACTION_METADATA")
        registerReceiver(metaReceiver, metaFilter, RECEIVER_EXPORTED)

        // Prompt user for permission if not already granted
        if (!isNotificationServiceEnabled()) {
            startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
        }

        setContent {
            Button_applicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MediaControlScreen(
                        onSkipClick = { sendSkipBroadcast() },
                        onVolUpClick = { sendVolUpBroadcast() },
                        onVolDownClick = { sendVolDownBroadcast() },
                        onPrevClick = { sendPreviousBroadcast() },
                        onPauseClick = { sendPauseBroadcast() },
                        onPlayClick = { sendPlayBroadcast() },
                        onNextAppClick = { sendNextAppBroadcast() },
                        onPrevAppClick = { sendPrevAppBroadcast() },
                        onLikeClick = { sendLikeBroadcast() },
                        onDislikeClick = { sendDislikeBroadcast() },
                        URI = picture,
                        title = title,
                        author = author,
                        album = album,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        unregisterReceiver(metaReceiver)
        super.onDestroy()
    }

    private fun sendSkipBroadcast() {
        val intent = Intent("ACTION_SKIP")
        sendBroadcast(intent)
    }

    private fun sendPreviousBroadcast() {
        val intent = Intent("ACTION_PREV")
        sendBroadcast(intent)
    }

    private fun sendPauseBroadcast() {
        val intent = Intent("ACTION_PAUSE")
        sendBroadcast(intent)
    }

    private fun sendPlayBroadcast() {
        val intent = Intent("ACTION_PLAY")
        sendBroadcast(intent)
    }

    private fun sendVolUpBroadcast() {
        val intent = Intent("ACTION_VOLUME_UP")
        sendBroadcast(intent)
    }

    private fun sendVolDownBroadcast() {
        val intent = Intent("ACTION_VOLUME_DOWN")
        sendBroadcast(intent)
    }

    private fun sendNextAppBroadcast() {
        val intent = Intent("ACTION_NEXT_APP")
        sendBroadcast(intent)
    }

    private fun sendPrevAppBroadcast() {
        val intent = Intent("ACTION_PREV_APP")
        sendBroadcast(intent)
    }

    private fun sendLikeBroadcast() {
        val intent = Intent("ACTION_LIKE")
        sendBroadcast(intent)
    }

    private fun sendDislikeBroadcast() {
        val intent = Intent("ACTION_DISLIKE")
        sendBroadcast(intent)
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val pkgName = packageName
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return flat?.contains(pkgName) == true
    }

    //metadata receiver
    private val metaReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            println("--------------------------------------------------")
            println("Sessions Tracked: ${intent?.getIntExtra("SESSIONS_TRACKED", 0)}")
            println("--------------------------------------------------")

            picture = intent?.getStringExtra("URI") ?: "None"
            title = intent?.getStringExtra("TITLE")
            album = intent?.getStringExtra("ALBUM_NAME")
            author = intent?.getStringExtra("AUTHOR")

        }
    }
}

@Composable
fun MediaControlScreen(onSkipClick: () -> Unit,
                       onVolUpClick: () -> Unit,
                       onVolDownClick: () -> Unit,
                       onPrevClick: () -> Unit,
                       onPauseClick: () -> Unit,
                       onPlayClick: () -> Unit,
                       onNextAppClick: () -> Unit,
                       onPrevAppClick: () -> Unit,
                       onLikeClick: () -> Unit,
                       onDislikeClick: () -> Unit,
                       URI: String?,
                       title: String?,
                       album: String?,
                       author: String?,
                       modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = onSkipClick) {
            Text(text = "Skip Current Track")
        }
        Button(onClick = onPrevClick) {
            Text(text = "Previous Track")
        }
        Button(onClick = onPauseClick) {
            Text(text = "Pause Track")
        }
        Button(onClick = onPlayClick) {
            Text(text = "Play Track")
        }
        Button(onClick = onVolUpClick) {
            Text(text = "Volume Up")
        }
        Button(onClick = onVolDownClick) {
            Text(text = "Volume Down")
        }
        Button(onClick = onNextAppClick) {
            Text(text = "Next App")
        }
        Button(onClick = onPrevAppClick) {
            Text(text = "Prev App")
        }
        Button(onClick = onLikeClick) {
            Text(text = "Like")
        }
        Button(onClick = onDislikeClick) {
            Text(text = "Dislike")
        }
        AsyncImage(
            model = (URI ?: "").toUri(),
            contentDescription = "Album Art",
            modifier = Modifier.size(200.dp),
            onState = { state ->
                if (state is AsyncImagePainter.State.Error) {
                    println("Error: ${state.result.throwable}")
                }
            }
        )
        if (title != null) {
            Text(
                text=title
            )
        }
        if (author != null) {
            Text(
                text=author
            )
        }
        if (album != null) {
            Text(
                text=album
            )
        }
    }
}
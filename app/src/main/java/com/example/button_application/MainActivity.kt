package com.example.button_application

import android.content.Intent
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.button_application.ui.theme.Button_applicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
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

    private fun isNotificationServiceEnabled(): Boolean {
        val pkgName = packageName
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return flat?.contains(pkgName) == true
    }
}

@Composable
fun MediaControlScreen(onSkipClick: () -> Unit,
                       onVolUpClick: () -> Unit,
                       onVolDownClick: () -> Unit,
                       onPrevClick: () -> Unit,
                       onPauseClick: () -> Unit,
                       onPlayClick: () -> Unit,
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
    }
}
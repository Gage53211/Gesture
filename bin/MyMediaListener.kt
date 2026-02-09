/***********************************************
 *                  MyMediaListener
 *
 * Description: uses the NotificationListener service
 *              to obtain a media session token and
 *              send commands to said media session
 *              via a media controller.
 *
 * Usage: To send actions, you must specify an Intent() with
 *        the desired action and then Use that intent
 *        with the sendBroadcast() function.
 *
 * Current Actions: "ACTION_SKIP"
 *                  "ACTION_PLAY"
 *                  "ACTION_PAUSE"
 *                  "ACTION_PREV"
 ***********************************************/

package com.example.myapplication

import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.session.MediaController
import android.media.session.MediaSession
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

// TODO: think of a way to store and cycle through media sessions.
// TODO: get audio controls working using AudioManager
class MyMediaListener : NotificationListenerService() {
    private var activeController: MediaController? = null
    var audioManager: AudioManager? = getSystemService(AUDIO_SERVICE) as AudioManager?
    val maxVolume = audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

    private val skipReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            activeController?.transportControls?.skipToNext()
        }
    }
    private val goBackReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            activeController?.transportControls?.skipToPrevious()
        }
    }
    private val pauseReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            activeController?.transportControls?.pause()
        }
    }
    private val playReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            activeController?.transportControls?.play()
        }
    }

    // register all receivers
    override fun onCreate() {
        super.onCreate()

        val skipFilter = IntentFilter("ACTION_SKIP")
        val backFilter = IntentFilter("ACTION_PREV")
        val pauseFilter = IntentFilter("ACTION_PAUSE")
        val playFilter = IntentFilter("ACTION_PLAY")

        // exclude "RECEIVER_EXPORTED" if android version is below 14
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(skipReceiver, skipFilter, Context.RECEIVER_EXPORTED)
            registerReceiver(goBackReceiver, backFilter, Context.RECEIVER_EXPORTED)
            registerReceiver(pauseReceiver, pauseFilter, Context.RECEIVER_EXPORTED)
            registerReceiver(playReceiver, playFilter, Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(skipReceiver, skipFilter)
            registerReceiver(goBackReceiver, backFilter)
            registerReceiver(pauseReceiver, pauseFilter)
            registerReceiver(playReceiver, playFilter)
        }
    }

    // upon receiving a notification that music is playing from some application
    // we get the token with that notification and use it to create a media controller
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val extras = sbn.notification.extras
        val token = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            extras.getParcelable(Notification.EXTRA_MEDIA_SESSION, MediaSession.Token::class.java)
        } else {
            @Suppress("DEPRECATION")
            extras.getParcelable<MediaSession.Token>(Notification.EXTRA_MEDIA_SESSION)
        }

        println("${sbn.notification.extras}")
        if (token != null) {
            activeController = MediaController(applicationContext, token)
        }
    }

    //free resources
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(skipReceiver)
        unregisterReceiver(goBackReceiver)
        unregisterReceiver(pauseReceiver)
        unregisterReceiver(playReceiver)
    }
}
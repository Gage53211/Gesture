package com.google.mediapipe.examples.gesturerecognizer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.session.MediaController
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.app.Notification
import android.media.session.MediaSession

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
 *                  "ACTION_VOL_UP"
 *                  "ACTION_VOL_DOWN"
 ***********************************************/
// TODO: think of a way to store and cycle through media sessions.
class MyMediaListener : NotificationListenerService() {
    private var activeController: MediaController? = null
    var volOffset: Int = 1

    private val audioManager: AudioManager? by lazy {
        getSystemService(AUDIO_SERVICE) as? AudioManager
    }
    private val maxVolume: Int by lazy {
        audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 100
    }

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

    private val volUpReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC,
                newVolLevel(volOffset, "VOL_UP") ?: 50, 0)
        }
    }

    private val volDownReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC,
                newVolLevel(volOffset, "VOL_DOWN") ?: 50, 0)
        }
    }

    // Returns new volume level
    fun newVolLevel (offset: Int?, direction: String): Int? {
        val currentVol: Int? = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC)
        val volDown = (currentVol ?: 0) - (offset ?: 0)
        val volUp = (currentVol ?: 0) + (offset ?: 0)

        if (direction == "VOL_UP") {
            if (volUp <= maxVolume) {
                return volUp
            }
            return maxVolume
        }
        if (volDown >= 0) {
            return volDown
        }
        return currentVol
    }

    // register all receivers
    override fun onCreate() {
        super.onCreate()

        val skipFilter = IntentFilter("ACTION_SKIP")
        val backFilter = IntentFilter("ACTION_PREV")
        val pauseFilter = IntentFilter("ACTION_PAUSE")
        val playFilter = IntentFilter("ACTION_PLAY")
        val volUpFilter = IntentFilter("ACTION_VOLUME_UP")
        val volDownFilter = IntentFilter("ACTION_VOLUME_DOWN")

    }

    // upon receiving a notification that music is playing from some application
    // we get the token with that notification and use it to create a media controller
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val extras = sbn.notification.extras
        val token = extras.getParcelable(Notification.EXTRA_MEDIA_SESSION, MediaSession.Token::class.java)

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
        unregisterReceiver(volUpReceiver)
        unregisterReceiver(volDownReceiver)
    }
}
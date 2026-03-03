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
 *                  "ACTION_NEXT_APP"
 *                  "ACTION_PREV_APP"
 ***********************************************/

package com.example.button_application

import android.os.Build
import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.session.MediaController
import android.media.session.MediaSession
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification


// TODO: re-write so that this service uses GetActiveSessions upon creation so that
//       the user doesn't have to play music in order to enumerate an active media session.

// TODO: set up the "onBind" function to return information
class MyMediaListener : NotificationListenerService() {
    private var activeController: MediaController? = null
    var volOffset: Int = 1
    private var appPos = 0
    private var tokens: Array<MediaSession.Token?> = arrayOfNulls(10)

    private val audioManager: AudioManager? by lazy {
        getSystemService(AUDIO_SERVICE) as? AudioManager
    }
    private val maxVolume: Int by lazy {
        audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 100
    }
    private val skipReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            checkValidity()
            activeController?.transportControls?.skipToNext()
        }
    }
    private val goBackReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            checkValidity()
            activeController?.transportControls?.skipToPrevious()
        }
    }
    private val pauseReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            checkValidity()
            activeController?.transportControls?.pause()
        }
    }
    private val playReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            checkValidity()
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

    private val nextApplicationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            appPos += 1
            if ((appPos > (tokens.size - 1))  || (appPos < 0) || tokens[appPos] == null) {
                appPos -= 1
                println("APP POS SET BACK 1")
            }
            println("App Position: " + appPos + " Media Session: " + tokens[appPos])
            activeController = tokens[appPos]?.let { MediaController(applicationContext, it) }
            checkValidity()
        }
    }

    private val prevApplicationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            appPos -= 1
            if (appPos < 0 || appPos > (tokens.size - 1)) {
                appPos = 0
                println("RESET APP POS FROM PREV")
            }
            println("App Position: " + appPos + " Media Session: " + tokens[appPos])
            activeController = tokens[appPos]?.let { MediaController(applicationContext, it) }
            checkValidity()
        }
    }

    //TODO: Could maybe be cleaned up?
    //checks for invalid tokens in tokens list
    private fun checkValidity() {
        var invalidTokenPresent = false
        if (activeController != null) {
            for (i in 0 until tokens.size ) {
                activeController = tokens[i]?.let { MediaController(applicationContext, it) }
                if (tokens[i] != null && activeController?.playbackState == null) { // validity check
                    tokens[i] = null
                    invalidTokenPresent = true
                }
            }
            println(tokens.contentToString())
            if (invalidTokenPresent) {
                appPos = 0 // set to last position in list
                tokens = shiftTokens(tokens)
                activeController = tokens[appPos]?.let { MediaController(applicationContext, it) }
                return
            }
            activeController = tokens[appPos]?.let { MediaController(applicationContext, it) }
        }
    }

    //writes all non null elements to front of new array
    private fun shiftTokens (arr: Array<MediaSession.Token?>): Array<MediaSession.Token?> {
        val newArray: Array<MediaSession.Token?> = arrayOfNulls(arr.size)
        var pos = 0
        for (i in 0 until arr.size) {
            if (arr[i] != null) {
                newArray[pos] = arr[i]
                pos += 1
            }
        }
        return newArray
    }

    // Returns new volume level
    private fun newVolLevel (offset: Int?, direction: String): Int? {
        val currentVol: Int? = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC)
        val volDown = (currentVol ?: 0) - (offset ?: 0)
        val volUp = (currentVol ?: 0) + (offset ?: 0)

        if (direction == "VOL_UP") {
            if (volUp <= maxVolume) {
                return volUp
            }
            return currentVol
        }
        if (volDown >= 0) {
            return volDown
        }
        return currentVol
    }

    // register all receivers
    override fun onCreate() {
        super.onCreate()
        appPos = 0

        val skipFilter = IntentFilter("ACTION_SKIP")
        val backFilter = IntentFilter("ACTION_PREV")
        val pauseFilter = IntentFilter("ACTION_PAUSE")
        val playFilter = IntentFilter("ACTION_PLAY")
        val volUpFilter = IntentFilter("ACTION_VOLUME_UP")
        val volDownFilter = IntentFilter("ACTION_VOLUME_DOWN")
        val nextAppFiler = IntentFilter("ACTION_NEXT_APP")
        val prevAppFilter = IntentFilter("ACTION_PREV_APP")

        registerReceiver(skipReceiver, skipFilter, RECEIVER_EXPORTED)
        registerReceiver(goBackReceiver, backFilter, RECEIVER_EXPORTED)
        registerReceiver(pauseReceiver, pauseFilter, RECEIVER_EXPORTED)
        registerReceiver(playReceiver, playFilter, RECEIVER_EXPORTED)
        registerReceiver(volUpReceiver, volUpFilter, RECEIVER_EXPORTED)
        registerReceiver(volDownReceiver, volDownFilter, RECEIVER_EXPORTED)
        registerReceiver(nextApplicationReceiver, nextAppFiler, RECEIVER_EXPORTED)
        registerReceiver(prevApplicationReceiver, prevAppFilter, RECEIVER_EXPORTED)

    }


    // upon receiving a notification that music is playing from some application
    // we get the token with that notification and use it to create a media controller
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val extras = sbn.notification.extras
        val token = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            extras.getParcelable(Notification.EXTRA_MEDIA_SESSION, MediaSession.Token::class.java)
        } else {
            @Suppress("DEPRECATION")
            extras.getParcelable(Notification.EXTRA_MEDIA_SESSION)
        }
        // loop until either null is found or token is found
        // if end of list is reached, replace end with token
        if (token != null) {
            for (i in 0 until tokens.size) {
                if (token == tokens[i]) {
                    break
                }
                if (tokens[i] == null || i == tokens.size - 1) {
                    tokens[i] = token
                    appPos = i
                    break
                }
            }
            println(tokens.contentToString())
            if (tokens[appPos] != null) {
                activeController = tokens[appPos]?.let { MediaController(applicationContext, it) }
            }
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
        unregisterReceiver(nextApplicationReceiver)
        unregisterReceiver(prevApplicationReceiver)

        println("SERVICE HAS BEEN DESTROYED")
    }
}
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
 *        with the sendBroadcast() function. This service
 *        will also emmit metadata and playback data whenever
 *        either change. This data can be obtained by setting up
 *        two broadcast receivers listening for "ACTION_METADATA"
 *        and "ACTION_PLAYBACK_DATA" and then using the
 *        get__datatype__Extra functions of the Intent class to read the data.
 *
 * Current Actions: "ACTION_SKIP"
 *                  "ACTION_PLAY"
 *                  "ACTION_PAUSE"
 *                  "ACTION_PREV"
 *                  "ACTION_VOL_UP"
 *                  "ACTION_VOL_DOWN"
 *                  "ACTION_NEXT_APP"
 *                  "ACTION_PREV_APP"
 *                  "ACTION_LIKE_DISLIKE"
 *
 * Sends:           "ACTION_METADATA"
 *                  "ACTION_PLAYBACK_DATA"
 ***********************************************/

package com.google.mediapipe.examples.gesturerecognizer

import android.app.Notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.graphics.drawable.toBitmap
import kotlin.concurrent.thread

class MyMediaListener : NotificationListenerService() {
    private var activeController: MediaController? = null
    private var threadController: MediaController? = null
    var volOffset: Int = 1
    val HAPTIC_EFFECT: VibrationEffect = VibrationEffect.createOneShot(100L, 200)
    private var appPos = 0
    @Volatile var isWakeUpThreadRunning: Boolean = true
    private var vibrator: Vibrator? = null
    private var tokens: Array<MediaSession.Token?> = arrayOfNulls(10)
    private val audioManager: AudioManager? by lazy {
        getSystemService(AUDIO_SERVICE) as? AudioManager
    }

    private val vibratorManager: VibratorManager? by lazy {
        getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager?
    }

    private val maxVolume: Int by lazy {
        audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 100
    }

    private val skipReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            vibrator?.vibrate(HAPTIC_EFFECT)
            checkValidity()
            activeController?.transportControls?.skipToNext()
        }
    }

    private val goBackReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            vibrator?.vibrate(HAPTIC_EFFECT)
            checkValidity()
            activeController?.transportControls?.skipToPrevious()
        }
    }

    private val pauseReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            vibrator?.vibrate(HAPTIC_EFFECT)
            checkValidity()
            activeController?.transportControls?.pause()
        }
    }

    private val playReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            vibrator?.vibrate(HAPTIC_EFFECT)
            checkValidity()
            activeController?.transportControls?.play()
        }
    }

    private val volUpReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            vibrator?.vibrate(HAPTIC_EFFECT)
            audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC,
                newVolLevel(volOffset, "VOL_UP") ?: 50, 0)
        }
    }

    private val volDownReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            vibrator?.vibrate(HAPTIC_EFFECT)
            audioManager?.setStreamVolume(AudioManager.STREAM_MUSIC,
                newVolLevel(volOffset, "VOL_DOWN") ?: 50, 0)
        }
    }

    private val likeDislikeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            vibrator?.vibrate(HAPTIC_EFFECT)
            val customControllerActions = activeController?.playbackState?.customActions
            val likeRegex = "like|add|remove|undo".toRegex(RegexOption.IGNORE_CASE)
            if (customControllerActions != null && customControllerActions.isNotEmpty()) {
                for (action in customControllerActions) {
                    if (action.name?.contains(likeRegex) == true ) {
                        val actionString: String = action.action
                        activeController?.transportControls?.sendCustomAction(actionString, null)
                        break
                    }
                }
            }
        }
    }

    private val mediaControllerCB = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            sendPlayBackDataBroadcast()
        }
        override fun onMetadataChanged(metadata: MediaMetadata?) {
            sendMetaDataBroadcast()
        }
    }

    private val nextApplicationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            vibrator?.vibrate(HAPTIC_EFFECT)
            appPos += 1
            if ((appPos > (tokens.size - 1))  || (appPos < 0) || tokens[appPos] == null) {
                appPos = 0
                println("RESET APP POS FROM NEXT")
            }
            println("App Position: " + appPos + " Media Session: " + tokens[appPos])
            activeController?.unregisterCallback(mediaControllerCB)
            activeController = tokens[appPos]?.let { MediaController(applicationContext, it) }
            activeController?.registerCallback(mediaControllerCB)
            checkValidity()
            sendMetaDataBroadcast()
            sendPlayBackDataBroadcast()
        }
    }

    private val prevApplicationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            vibrator?.vibrate(HAPTIC_EFFECT)
            appPos -= 1
            val endOfTokens = countActiveTokens()
            if (appPos < 0 || appPos > (tokens.size - 1)) {
                when {
                    endOfTokens <= 0 -> appPos = 0
                    else -> appPos = endOfTokens - 1
                }
                println("APP POS NOW AT FRONT OF TOKENS")
            }
            println("App Position: " + appPos + " Media Session: " + tokens[appPos])
            activeController?.unregisterCallback(mediaControllerCB)
            activeController = tokens[appPos]?.let { MediaController(applicationContext, it) }
            activeController?.registerCallback(mediaControllerCB)
            checkValidity()
            sendMetaDataBroadcast()
            sendPlayBackDataBroadcast()
        }
    }

    private fun wakeUpThread () {
        // when 10 seconds pass, wake all sessions
        thread {
            while (isWakeUpThreadRunning) {
                println("WAKING SESSIONS-------------------------")
                for (i in 0 until tokens.size) {
                    if (tokens[i] != null && threadController?.playbackState != null) {
                        threadController =
                            tokens[i]?.let { MediaController(applicationContext, it) }
                        threadController?.transportControls?.prepare()
                    }
                }
                Thread.sleep(20000L)
            }
        }
    }

    // register all receivers
    override fun onCreate() {
        super.onCreate()
        vibrator = vibratorManager?.defaultVibrator
        wakeUpThread()
        appPos = 0

        val skipFilter = IntentFilter("ACTION_SKIP")
        val backFilter = IntentFilter("ACTION_PREV")
        val pauseFilter = IntentFilter("ACTION_PAUSE")
        val playFilter = IntentFilter("ACTION_PLAY")
        val volUpFilter = IntentFilter("ACTION_VOLUME_UP")
        val volDownFilter = IntentFilter("ACTION_VOLUME_DOWN")
        val nextAppFiler = IntentFilter("ACTION_NEXT_APP")
        val prevAppFilter = IntentFilter("ACTION_PREV_APP")
        val likeFilter = IntentFilter("ACTION_LIKE_DISLIKE")

        registerReceiver(skipReceiver, skipFilter, RECEIVER_EXPORTED)
        registerReceiver(goBackReceiver, backFilter, RECEIVER_EXPORTED)
        registerReceiver(pauseReceiver, pauseFilter, RECEIVER_EXPORTED)
        registerReceiver(playReceiver, playFilter, RECEIVER_EXPORTED)
        registerReceiver(volUpReceiver, volUpFilter, RECEIVER_EXPORTED)
        registerReceiver(volDownReceiver, volDownFilter, RECEIVER_EXPORTED)
        registerReceiver(nextApplicationReceiver, nextAppFiler, RECEIVER_EXPORTED)
        registerReceiver(prevApplicationReceiver, prevAppFilter, RECEIVER_EXPORTED)
        registerReceiver(likeDislikeReceiver, likeFilter, RECEIVER_EXPORTED)

    }

    // checks if any currently posted notifications have media session tokens upon startup
    override fun onListenerConnected() {
        val notifications = activeNotifications
        var position = 0
        for (i in 0 until notifications.size) {
            val extras = notifications[i].notification.extras
            val token = extras.getParcelable(Notification.EXTRA_MEDIA_SESSION, MediaSession.Token::class.java)
            if (token != null) {
                tokens[position] = token
                if (position < tokens.size) {
                    position++
                }
            }
        }
        println("INITIAL TOKENS-----------------------:")
        println(tokens.contentToString())
        if (tokens[0] != null) {
            activeController = tokens[appPos]?.let { MediaController(applicationContext, it) }
            activeController?.registerCallback(mediaControllerCB)
            sendMetaDataBroadcast()
            sendPlayBackDataBroadcast()
        }
    }

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
            if (invalidTokenPresent) {
                appPos = 0 // set to last position in list
                tokens = shiftTokens(tokens)
                activeController?.unregisterCallback(mediaControllerCB)
                activeController = tokens[appPos]?.let { MediaController(applicationContext, it) }
                activeController?.registerCallback(mediaControllerCB)
                sendMetaDataBroadcast()
                sendPlayBackDataBroadcast()
                return
            }
            activeController?.unregisterCallback(mediaControllerCB)
            activeController = tokens[appPos]?.let { MediaController(applicationContext, it) }
            activeController?.registerCallback(mediaControllerCB)
            sendMetaDataBroadcast()
            sendPlayBackDataBroadcast()
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

    private fun countActiveTokens (): Int {
        var count = 0
        for (t in tokens) {
            if (t != null) {
                count++
            }
        }
        return count
    }

    private fun getActiveMCNotification(): Notification? {
        val notifications = activeNotifications
        if (activeController != null) {
            val activeToken = activeController?.sessionToken
            for (notification in notifications) {
                val extras = notification.notification.extras
                val token = extras.getParcelable(
                    Notification.EXTRA_MEDIA_SESSION,
                    MediaSession.Token::class.java
                )
                if (token != null && token == activeToken) {
                    return notification.notification
                }
            }
        }
        return null
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

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val extras = sbn.notification.extras
        val token = extras.getParcelable(Notification.EXTRA_MEDIA_SESSION, MediaSession.Token::class.java)
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
                activeController?.unregisterCallback(mediaControllerCB)
                activeController = tokens[appPos]?.let { MediaController(applicationContext, it) }
                activeController?.registerCallback(mediaControllerCB)
                sendMetaDataBroadcast()
                sendPlayBackDataBroadcast()
            }
        }
    }
    private fun sendMetaDataBroadcast() {
        val intent = Intent("ACTION_METADATA")
        if (activeController != null) {
            val metadata = activeController?.metadata
            val activeNotif = getActiveMCNotification()
            val bitmapImg = activeNotif?.getLargeIcon()
            val drawable = bitmapImg?.loadDrawable(this)
            val bitmap = drawable?.toBitmap()

            // spotify uri workaround
            val spotifyRegex = "%3A[a-z0-9]*\\?".toRegex()
            var uri = metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI)
            if (uri?.contains("spotify") == true) {
                val hash = spotifyRegex.find(uri)?.value
                if (hash != null ) {
                    uri = "https://i.scdn.co/image/" + hash.substring(3, hash.length - 1) // ignore "3A" and "?"
                }
            }

            intent.putExtra("TITLE", metadata?.getString(MediaMetadata.METADATA_KEY_TITLE) ?: "No Title Available")
            intent.putExtra("AUTHOR", metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: "No Artist")
            intent.putExtra("ALBUM_NAME", metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM) ?: "No Album Name Available")
            intent.putExtra("URI", uri ?: "No URI")
            intent.putExtra("BITMAP", bitmap)
            intent.putExtra("SESSIONS_TRACKED", countActiveTokens())
            sendBroadcast(intent)
        }
    }

    private fun sendPlayBackDataBroadcast() {
        val intent = Intent("ACTION_PLAYBACK_DATA")
        if (activeController != null) {
            val metadata = activeController?.metadata
            val playBackInfo = activeController?.playbackState

            intent.putExtra("DURATION", metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: -1)
            intent.putExtra("CURRENT_POSITION", playBackInfo?.position ?: -1)
            intent.putExtra("SESSIONS_TRACKED", countActiveTokens())
            intent.putExtra("STATE", playBackInfo?.state ?: "State Not Available")
            sendBroadcast(intent)
        }
    }

    //free resources
    override fun onDestroy() {
        super.onDestroy()

        isWakeUpThreadRunning = false

        unregisterReceiver(skipReceiver)
        unregisterReceiver(goBackReceiver)
        unregisterReceiver(pauseReceiver)
        unregisterReceiver(playReceiver)
        unregisterReceiver(volUpReceiver)
        unregisterReceiver(volDownReceiver)
        unregisterReceiver(nextApplicationReceiver)
        unregisterReceiver(prevApplicationReceiver)
        unregisterReceiver(likeDislikeReceiver)

        println("SERVICE HAS BEEN DESTROYED")
    }
}

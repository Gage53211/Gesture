/*
 * Copyright 2022 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.mediapipe.examples.gesturerecognizer

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.mediapipe.examples.gesturerecognizer.databinding.ActivityMainBinding
import com.google.mediapipe.examples.gesturerecognizer.fragment.PermissionsFragment
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.graphics.Bitmap
import android.util.Log
import java.util.Locale
import android.os.Handler
import android.os.Looper


class MainActivity : AppCompatActivity() {
    private lateinit var activityMainBinding: ActivityMainBinding
    private var isDialogShowing = false
    private val handler = Handler(Looper.getMainLooper())
    private val updateSeekBarTask = object : Runnable {
        override fun run() {
            // Increment progress by 1 second
            activityMainBinding.musicSeekBar.progress += 1
            activityMainBinding.currentTime.text = formatTime(activityMainBinding.musicSeekBar.progress)

            // Repeat every second
            handler.postDelayed(this, 1000)
        }
    }

    private fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        //return String.format("%02d:%02d", minutes, remainingSeconds)
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, remainingSeconds)
    }

    private val musicReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null) return
            when (intent.action) {
                "ACTION_METADATA" -> {
                    val title = intent.getStringExtra("TITLE") ?: "No Title Available"
                    val author = intent.getStringExtra("AUTHOR") ?: "Unknown Artist"
                    val bitmap = intent.getParcelableExtra<Bitmap>("BITMAP")

                    if (bitmap != null) {
                        // handle case for no bitmap
                    }
                    Log.d("MainActivity", "Title: $title, Author: $author")
                }
                "ACTION_PLAYBACK_DATA" -> {
                    val durationMs = intent.getLongExtra("DURATION", 0L)
                    val currentPosMs = intent.getLongExtra("CURRENT_POSITION", 0L)
                    val isPlaying = intent.getBooleanExtra("IS_PLAYING", false)

                    // Convert ms to seconds
                    val durationSec = (durationMs / 1000).toInt()
                    val currentSec = (currentPosMs / 1000).toInt()

                    handler.removeCallbacks(updateSeekBarTask)
                    if (isPlaying) {
                        handler.postDelayed(updateSeekBarTask, 1000)
                    }

                    // Update SeekBar
                    activityMainBinding.musicSeekBar.max = durationSec
                    activityMainBinding.musicSeekBar.progress = currentSec

                    activityMainBinding.totalTime.text = formatTime(durationSec)
                    activityMainBinding.currentTime.text = formatTime(currentSec)

                    handler.removeCallbacks(updateSeekBarTask)
                    if (isPlaying) {
                        handler.postDelayed(updateSeekBarTask, 1000)
                    }
                    Log.d("MainActivity", "Duration: $durationMs, Current Position: $currentPosMs")
                }

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        activityMainBinding.musicSeekBar.setOnTouchListener { _, _ -> true }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        WindowCompat.setDecorFitsSystemWindows(window, true)

        checkPermissionsAndTutorial()

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        navHostFragment.navController

        val toolbar = findViewById<MaterialToolbar>(R.id.dropdown_menu)
        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.settings -> { navigatetoSettings(); true }
                R.id.help -> { navigatetoTutorial(); true }
                else -> false
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter().apply {
            addAction("ACTION_METADATA")
            addAction("ACTION_PLAYBACK_DATA")
        }
        registerReceiver(musicReceiver, filter, RECEIVER_EXPORTED)
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(musicReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateSeekBarTask)
    }


    override fun onResume() {
        super.onResume()
        checkPermissionsAndTutorial()
    }

    fun checkPermissionsAndTutorial() {
        val hasCamera = PermissionsFragment.hasPermissions(this)
        val hasNotification = isNotificationServiceEnabled()

        if (!hasCamera || !hasNotification) {
            if (!isDialogShowing) {
                showMandatoryDialog(!hasNotification)
            }
        } else {
            val sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            val isFirstTime = sharedPref.getBoolean("isFirstTimeTutorial", true)
            if (isFirstTime) {
                with(sharedPref.edit()) {
                    putBoolean("isFirstTimeTutorial", false)
                    apply()
                }
                navigatetoTutorial()
            }
        }
    }

    // needs to be kept
    private fun showMandatoryDialog(missingNotification: Boolean) {
        isDialogShowing = true
        AlertDialog.Builder(this)
            .setTitle("Permission Required")
            .setMessage("Gesture does not collect users data. To continue, please enable camera and notification permissions in settings.")
            .setCancelable(false)
            .setPositiveButton("Continue") { _, _ ->
                isDialogShowing = false
                if (missingNotification) {
                    startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
                } else {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }
            }
            .show()
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return flat?.contains(packageName) == true
    }

    private fun navigatetoSettings() = startActivity(Intent(this, SettingsActivity::class.java))
    private fun navigatetoTutorial() = startActivity(Intent(this, TutorialActivity::class.java))
}
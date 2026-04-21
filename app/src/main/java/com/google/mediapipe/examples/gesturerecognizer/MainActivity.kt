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

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.mediapipe.examples.gesturerecognizer.databinding.ActivityMainBinding
import androidx.core.view.WindowCompat
import android.widget.SeekBar

class MainActivity : AppCompatActivity() {
    private lateinit var activityMainBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (!isNotificationServiceEnabled()) {
            startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
        }
        WindowCompat.setDecorFitsSystemWindows(window, true)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        navHostFragment.navController

        val toolbar = findViewById<MaterialToolbar>(R.id.dropdown_menu)
        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.settings -> {
                    navigatetoSettings()
                    true
                }
                R.id.help -> {
                    navigatetoTutorial()
                    true
                }
                else -> false
            }
        }

    }
    private fun navigatetoSettings(){
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
        // Dummy Vars
        val totalSeconds = 210 // 3 minutes and 30 seconds

        activityMainBinding.musicSeekBar.max = totalSeconds
        activityMainBinding.musicSeekBar.progress = 45

        activityMainBinding.totalTime.text = formatTime(totalSeconds)
        activityMainBinding.currentTime.text = formatTime(45)

    }
    //This is the function called to change the intent to the tutorial activity
    private fun navigatetoTutorial(){
        val intent = Intent(this, TutorialActivity::class.java)
        startActivity(intent)
    }

    private fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val pkgName = packageName
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        return flat?.contains(pkgName) == true
    }
}
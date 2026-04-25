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

class MainActivity : AppCompatActivity() {
    private lateinit var activityMainBinding: ActivityMainBinding
    private var isDialogShowing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        WindowCompat.setDecorFitsSystemWindows(window, true)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        navHostFragment.navController

        checkPermissionsAndTutorial()

        val toolbar = findViewById<MaterialToolbar>(R.id.dropdown_menu)
        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.settings -> { navigatetoSettings(); true }
                R.id.help -> { navigatetoTutorial(); true }
                else -> false
            }
        }
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
package com.dechow.owen.bible_reminder

import android.content.Intent
import android.os.Bundle
import android.widget.Switch
import androidx.activity.ComponentActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        updateSwitches()
        updateServiceSwitch()
    }

    override fun onResume() {
        super.onResume()
        updateSwitches()
        updateServiceSwitch()
    }

    private fun updateServiceSwitch() {
        val serviceSwitch: Switch = findViewById(R.id.service_switch)

        serviceSwitch.setOnClickListener {
            if (hasAllPermissions(this)) {
                CoroutineScope(Dispatchers.IO).launch {
                    val data = Data(this@MainActivity)
                    data.load()

                    data.running = !data.running
                    withContext(Dispatchers.Main) {
                        serviceSwitch.isChecked = data.running
                    }
                    data.savePrefs()
                    sendBroadcast(Intent(UPDATED_PREFS_SINGLE))
                }
            }
        }

        if (hasAllPermissions(this)) {
            serviceSwitch.isEnabled = true
            CoroutineScope(Dispatchers.IO).launch {
                val data = Data(this@MainActivity)
                data.load()

                withContext(Dispatchers.Main) {
                    serviceSwitch.isChecked = data.running
                }
            }
        } else {
            serviceSwitch.isEnabled = false
            serviceSwitch.isChecked = false
        }
    }

    private fun updateSwitches() {
        val accessPermission: Switch = findViewById(R.id.accessibility_switch)
        accessPermission.isChecked = hasAccessibilityPermissions(this)
        accessPermission.setOnClickListener {
            requestAccessibilityPermissions(this)
        }

        val overlayPermission: Switch = findViewById(R.id.overlay_switch)
        overlayPermission.isChecked = hasDrawOverlayPermission(this)
        overlayPermission.setOnClickListener {
            requestDrawOverlayPermission(this)
        }

        val usagePermission: Switch = findViewById(R.id.usage_switch)
        usagePermission.isChecked = hasUsagePermission(this)
        usagePermission.setOnClickListener {
            requestUsagePermission(this)
        }

        val notificationPermission: Switch = findViewById(R.id.notification_switch)
        notificationPermission.isChecked = hasNotificationPermission(this)
        notificationPermission.setOnClickListener {
            requestNotificationPermission(this)
        }
    }
}
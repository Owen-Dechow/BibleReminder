package com.dechow.owen.bible_reminder

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

const val MaxRatio = 10.0

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        updateSwitches()
        updateServiceSwitch()
        setupButtons()
        setupTimeSlider()
    }

    override fun onResume() {
        super.onResume()
        updateSwitches()
        updateServiceSwitch()
    }

    private fun sendUpdateBroadcast() {
        val intent = Intent(UPDATED_PREFS_SINGLE)
        intent.setPackage(this.packageName)
        sendBroadcast(intent)
    }

    private fun sendReloadDataBroadcast() {
        val intent = Intent(RELOAD_DATA_SINGLE)
        intent.setPackage(this.packageName)
        sendBroadcast(intent)
    }

    private fun setSliderNum(num: Double) {
        val number: TextView = findViewById(R.id.bible_app_view_number)
        number.text = ((100 * num).roundToInt() / 100).toString()
    }

    private fun setupTimeSlider() {
        val slider: SeekBar = findViewById(R.id.bible_app_time_seek)

        dataUserFn { data ->
            slider.progress = (data.timeRatio / MaxRatio * 100).roundToInt()
            setSliderNum(data.timeRatio)
        }

        slider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(bar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (bar == null) return

                val num = (bar.progress / 100.0) * MaxRatio
                setSliderNum(num)
            }

            override fun onStartTrackingTouch(bar: SeekBar?) = Unit

            override fun onStopTrackingTouch(bar: SeekBar?) {
                if (bar == null) return

                val num = (bar.progress / 100.0) * MaxRatio
                setSliderNum(num)

                dataUserFn { data ->
                    data.timeRatio = num
                    data.savePrefs()
                    sendUpdateBroadcast()
                }
            }

        })
    }

    private fun setupButtons() {
        val bibleAppsButton: Button = findViewById(R.id.bible_apps_btn)
        bibleAppsButton.setOnClickListener {
            dataUserFn { data ->
                showAppSelectionDialog(data.bibles) {
                    data.savePrefs()
                    sendUpdateBroadcast()
                }
            }
        }

        val limitedAppsButton: Button = findViewById(R.id.limited_apps_btn)
        limitedAppsButton.setOnClickListener {
            dataUserFn { data ->
                showAppSelectionDialog(data.limitedApps) {
                    data.savePrefs()
                    sendUpdateBroadcast()
                }
            }
        }

        val timeResetButton: Button = findViewById(R.id.time_reset_btn)
        timeResetButton.setOnClickListener {
            sendReloadDataBroadcast()
        }
    }

    private fun updateServiceSwitch() {
        val serviceSwitch: Switch = findViewById(R.id.service_switch)

        serviceSwitch.setOnClickListener {
            if (hasAllPermissions(this)) {
                dataUserFn { data ->
                    data.running = !data.running
                    withContext(Dispatchers.Main) {
                        serviceSwitch.isChecked = data.running
                    }
                    data.savePrefs()
                    sendUpdateBroadcast()
                }
            }
        }

        if (hasAllPermissions(this)) {
            serviceSwitch.isEnabled = true
            dataUserFn { data ->
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

        val bibleInstalled: Switch = findViewById(R.id.bible_installed_switch)
        bibleInstalled.isChecked = isYouVersionInstalled(this)
        bibleInstalled.setOnClickListener {
            openPlayStoreForYouVersion(this)
        }
    }

    private suspend fun showAppSelectionDialog(
        selected: MutableList<String>, onSave: suspend () -> Unit
    ) {
        val dialogView = layoutInflater.inflate(R.layout.app_select_dialog, null)
        val recyclerView: RecyclerView = dialogView.findViewById(R.id.app_list)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val pm = packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = pm.queryIntentActivities(intent, 0)

        val appList = resolveInfos.map {
            AppItem(
                label = it.loadLabel(pm).toString(),
                packageName = it.activityInfo.packageName,
                icon = it.loadIcon(pm),
                isChecked = it.activityInfo.packageName in selected,
                onToggle = { isChecked ->
                    if (isChecked) {
                        if (it.activityInfo.packageName !in selected) selected.add(it.activityInfo.packageName)
                    } else {
                        selected.remove(it.activityInfo.packageName)
                    }
                })
        }

        val adapter = AppSelectAdapter(appList)
        recyclerView.adapter = adapter

        withContext(Dispatchers.Main) {
            AlertDialog.Builder(this@MainActivity).setTitle("Select Apps").setView(dialogView)
                .setPositiveButton("Done") { _, _ ->
                    dataUserFn { onSave() }
                }.show()
        }
    }

    private fun dataUserFn(fn: suspend (data: Data) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            val data = Data(this@MainActivity)
            data.load()
            fn(data)
        }
    }
}

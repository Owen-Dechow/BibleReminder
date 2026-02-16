package com.dechow.owen.bible_reminder

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToLong

const val UPDATED_PREFS_SINGLE = "UPDATED_PREFS_SINGLE"
const val RELOAD_DATA_SINGLE = "RELOAD_DATA"

class AppUsageService : AccessibilityService() {
    private var currentPackage: String? = null
    private var packageStart: Long? = null
    private var handler = Handler(Looper.getMainLooper())
    private lateinit var data: Data
    private lateinit var overlay: OverlayManager
    private var hasPermissions: Boolean = false

    override fun onServiceConnected() {
        overlay = OverlayManager(this)

        registerReceivers(
            arrayOf(
                Triple(UPDATED_PREFS_SINGLE, applicationUpdates, true),
                Triple(RELOAD_DATA_SINGLE, reloadData, true),
                Triple(Intent.ACTION_SCREEN_OFF, screenOff, false)
            )
        )

        CoroutineScope(Dispatchers.IO).launch {
            data = Data(this@AppUsageService)
            data.load()
            hasPermissions = hasAllPermissions(application)
        }
    }

    private fun registerReceivers(receivers: Array<Triple<String, BroadcastReceiver, Boolean>>) {
        for (receiver in receivers) {
            val filter = IntentFilter(receiver.first)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(
                    receiver.second,
                    filter,
                    if (receiver.third) Context.RECEIVER_NOT_EXPORTED
                    else Context.RECEIVER_EXPORTED
                )
            } else {
                @Suppress("UnspecifiedRegisterReceiverFlag")
                registerReceiver(
                    receiver.second,
                    filter
                )
            }
        }
    }

    private fun shouldRun(event: AccessibilityEvent?): Boolean {
        if (event == null) return false
        if (!::data.isInitialized) return false
        if (!data.loaded) return false
        if (!data.running) return false
        if (!hasPermissions) return false

        return true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!shouldRun(event)) return
        onAppChange(event!!.packageName.toString())
    }

    private fun onAppChange(pack: String) {
        val newTime = System.currentTimeMillis()

        if (pack == currentPackage) return

        if (packageStart != null) {
            val timeChange = newTime - packageStart!!

            if (currentPackage in data.limitedApps) {
                data.limitedTime += timeChange
            } else if (currentPackage in data.bibles) {
                data.bibleTime += newTime - packageStart!!
            }
        }

        overlay.hideOverlay()
        currentPackage = pack
        packageStart = newTime
        handler.removeCallbacksAndMessages(null)

        if (currentPackage in data.limitedApps) {
            handler.post(limitedRunner)
        } else if (currentPackage in data.bibles) {
            handler.post(bibleRunnable)
        }
    }

    private val bibleRunnable = object : Runnable {
        override fun run() {
            val now = System.currentTimeMillis()
            val addedTime = (now - packageStart!!) * data.timeRatio
            val allowedTime = data.timeRemaining + addedTime.roundToLong()

            overlay.showBottomText(allowedTime.toTimeString())
            handler.postDelayed(this, 1000)
        }
    }

    private val limitedRunner = object : Runnable {
        override fun run() {
            val now = System.currentTimeMillis()
            val removedTime = now - packageStart!!
            val allowedTime = data.timeRemaining - removedTime

            if (allowedTime > 0) {
                overlay.showBottomText(allowedTime.toTimeString())
                handler.postDelayed(this, 1000)
            } else {
                overlay.showFullScreen("Time to read your Bible!", "Open YouVersion") {
                    launchYouVersion(application)
                }
            }
        }
    }

    override fun onInterrupt() {}

    private val screenOff = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            onAppChange("")
        }
    }

    private val reloadData = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (!::data.isInitialized) return
            data.loaded = false

            CoroutineScope(Dispatchers.IO).launch {
                data.load()
                hasPermissions = hasAllPermissions(application)
            }
        }
    }

    private val applicationUpdates = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (!::data.isInitialized) return

            data.saveTimes()
            data.loaded = false

            CoroutineScope(Dispatchers.IO).launch {
                data = Data(this@AppUsageService)
                data.load()
                hasPermissions = hasAllPermissions(application)
            }
        }
    }
}


fun Long.toTimeString(): String {
    var ms = this

    val hours = ms / (1000 * 60 * 60)
    ms %= (1000 * 60 * 60)

    val minutes = ms / (1000 * 60)
    ms %= (1000 * 60)

    val seconds = ms / 1000

    return buildString {
        if (hours > 0) append("${hours}h ")
        if (minutes > 0) append("${minutes}m ")
        append("${seconds}s")
    }.trim()
}
package com.dechow.owen.bible_reminder

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView

class OverlayManager(private val context: Context) {

    private val windowManager =
        context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    private var overlayView: View? = null

    fun showBottomText(text: String) {
        if (overlayView == null) {
            val inflater = LayoutInflater.from(context)
            val dummyParent = FrameLayout(context)

            overlayView = inflater.inflate(R.layout.overlay_bottom_text, dummyParent, false)

            val windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
            }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                windowType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT
            )

            params.gravity = Gravity.BOTTOM

            windowManager.addView(overlayView, params)
        }

        overlayView?.findViewById<TextView>(R.id.overlayText)?.text = text
    }

    fun showFullScreen(
        message: String,
        buttonText: String,
        onButtonClick: () -> Unit
    ) {
        if (overlayView != null) {
            hideOverlay()
        }

        val inflater = LayoutInflater.from(context)
        val dummyParent = FrameLayout(context)

        overlayView = inflater.inflate(R.layout.overlay_break_time, dummyParent, false)

        val windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            windowType,
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.CENTER

        overlayView?.findViewById<TextView>(R.id.overlayText)?.text = message
        overlayView?.findViewById<Button>(R.id.overlayButton)?.apply {
            text = buttonText
            setOnClickListener { onButtonClick() }
        }

        windowManager.addView(overlayView, params)
    }

    fun hideOverlay() {
        overlayView?.let {
            windowManager.removeView(it)
            overlayView = null
        }
    }
}

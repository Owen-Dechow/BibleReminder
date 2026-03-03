package com.dechow.owen.bible_reminder

import android.os.Bundle
import android.text.Html
import android.text.method.ScrollingMovementMethod
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.core.text.HtmlCompat

class AccessInfoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.access_info_activity)

        val textView: TextView = findViewById(R.id.access_info_txt)
        val html = """
            <p>
                <strong>Bible Reminder uses Accessibility Services solely to enforce your chosen app limits.</strong>
            </p>
            
            <p>
                To track and temporarily block access to the apps you’ve selected as “limited,” the app must be able
                to detect when those apps are opened and apply your time‑based restrictions. This requires the
                Accessibility permission, which allows Bible Reminder to:
                <br><br><strong>(1) Identify when a limited app is in use</strong> so your screen‑time budget can be applied accurately.
                <br><br><strong>(2) Ensure limits remain consistent</strong> even if apps are opened from notifications, search, or multitasking.
            </p>

            <p> 
                <strong>Bible Reminder does <em>not</em> use Accessibility Services to read your personal information,
                collect data, or control your device outside of enforcing your chosen limits.</strong>
            </p>

            <p>The permission is used only for app‑usage monitoring and blocking, and all processing stays on your device.</p>

            <p>
                <strong>Additional notes users may find helpful:</strong>
                <br><br><strong>(1)</strong> Accessibility Services are the only Android‑approved way for an app to detect and limit usage of other apps.</li>
                <br><br><strong>(2)</strong> The app cannot see the content inside your apps, only which app is currently active.</li>
                <br><br><strong>(3)</strong> You can disable the permission at any time, but the app will no longer function.</li>
            </p>
        """.trimIndent()

        textView.text = Html.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
        textView.movementMethod = ScrollingMovementMethod()

        val closeButton: Button = findViewById(R.id.access_info_back_btn)
        closeButton.setOnClickListener { finish() }
    }
}
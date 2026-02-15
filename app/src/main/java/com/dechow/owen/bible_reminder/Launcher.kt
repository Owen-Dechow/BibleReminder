package com.dechow.owen.bible_reminder

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

fun launchYouVersion(context: Context) {
    val intent = Intent(Intent.ACTION_VIEW, "youversion://home".toUri())
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

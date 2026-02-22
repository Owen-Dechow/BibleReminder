package com.dechow.owen.bible_reminder

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.net.toUri

const val YOU_VERSION_PACKAGE = "com.sirma.mobile.bible.android"

fun launchYouVersion(context: Context) {
    val intent = Intent(Intent.ACTION_VIEW, "youversion://home".toUri())
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

fun isYouVersionInstalled(context: Context): Boolean {
    return try {
        context.packageManager.getApplicationInfo(YOU_VERSION_PACKAGE, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}

fun openPlayStoreForYouVersion(context: Context) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, "market://details?id=$YOU_VERSION_PACKAGE".toUri())
        intent.setPackage("com.android.vending") // Directs intent to only use the Play Store app
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            "https://play.google.com".toUri()
        )
        webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(webIntent)
        } catch (e: ActivityNotFoundException) {
            sendNotification(
                context,
                "Can't Open/Install Bible",
                "The You Version Bible can not be opened or installed"
            )
        }
    }
}
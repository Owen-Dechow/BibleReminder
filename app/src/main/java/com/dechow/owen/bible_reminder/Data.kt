package com.dechow.owen.bible_reminder

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlin.math.roundToLong

val Context.limitsDataStore by preferencesDataStore(name = "limits_prefs")

class Data(context: Context) {
    private val dataStore = context.limitsDataStore

    var bibles: List<String> = emptyList()
    var limitedApps: List<String> = emptyList()
    var running: Boolean = false
    var loaded: Boolean = false

    var bibleTime: Long = 0
        set(value) {
            field = value.coerceAtLeast(0)
            saveTimes()
        }

    var limitedTime: Long = 0
        set(value) {
            field = value.coerceAtMost(timeLimit)
            saveTimes()
        }

    var timeRatio: Double = 4.0
        set(value) {
            field = value
            saveTimes()
        }

    private val timeLimit: Long
        get() = (bibleTime * timeRatio).roundToLong()

    val timeRemaining: Long
        get() = (timeLimit - limitedTime).coerceAtLeast(0)

    suspend fun load() {
        val prefs = dataStore.data.first()

        bibles = prefs[Keys.BIBLES]?.toList() ?: listOf("com.sirma.mobile.bible.android")

        limitedApps = prefs[Keys.LIMITED_APPS]?.toList() ?: listOf()

        bibleTime = prefs[Keys.BIBLE_TIME] ?: 0
        limitedTime = prefs[Keys.LIMITED_TIME] ?: 0
        timeRatio = prefs[Keys.TIME_RATIO] ?: 4.0
        running = prefs[Keys.RUNNING] ?: false

        val lastReset = prefs[Keys.LAST_RESET_DAY] ?: 0L
        val today = currentDay()

        if (today != lastReset) {
            resetDaily()
        }

        loaded = true
    }

    fun savePrefs() {
        CoroutineScope(Dispatchers.IO).launch {
            dataStore.edit { prefs ->
                prefs[Keys.BIBLES] = bibles.toSet()
                prefs[Keys.LIMITED_APPS] = limitedApps.toSet()
                prefs[Keys.TIME_RATIO] = timeRatio
                prefs[Keys.RUNNING] = running
            }
        }
    }

    fun saveTimes() {
        CoroutineScope(Dispatchers.IO).launch {
            dataStore.edit { prefs ->
                prefs[Keys.BIBLE_TIME] = bibleTime
                prefs[Keys.LIMITED_TIME] = limitedTime
                prefs[Keys.RUNNING] = running
            }
        }
    }

    private fun resetDaily() {
        bibleTime = 0
        limitedTime = 0

        CoroutineScope(Dispatchers.IO).launch {
            dataStore.edit { prefs ->
                prefs[Keys.BIBLE_TIME] = 0
                prefs[Keys.LIMITED_TIME] = 0
                prefs[Keys.LAST_RESET_DAY] = currentDay()
            }
        }
    }

    private fun currentDay(): Long {
        return System.currentTimeMillis() / (24 * 60 * 60 * 1000)
    }

    private object Keys {
        val BIBLES = stringSetPreferencesKey("bibles")
        val LIMITED_APPS = stringSetPreferencesKey("limited_apps")
        val BIBLE_TIME = longPreferencesKey("bible_time")
        val LIMITED_TIME = longPreferencesKey("limited_time")
        val TIME_RATIO = doublePreferencesKey("time_ratio")
        val RUNNING = booleanPreferencesKey("running")
        val LAST_RESET_DAY = longPreferencesKey("last_reset_day")
    }
}
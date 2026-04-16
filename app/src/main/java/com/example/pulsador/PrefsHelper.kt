package com.example.pulsador

import android.content.Context
import android.content.SharedPreferences

object PrefsHelper {
    private const val PREFS_NAME = "pulsador_prefs"
    private const val KEY_LAST_CLICK_TIME = "last_click_time"
    private const val KEY_IS_GREEN = "is_green"

    fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getLastClickTime(context: Context): Long {
        return getPrefs(context).getLong(KEY_LAST_CLICK_TIME, 0L)
    }

    fun setLastClickTime(context: Context, time: Long) {
        getPrefs(context).edit().putLong(KEY_LAST_CLICK_TIME, time).apply()
    }

    fun isGreen(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_IS_GREEN, false)
    }

    fun setIsGreen(context: Context, isGreen: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_IS_GREEN, isGreen).apply()
    }
}

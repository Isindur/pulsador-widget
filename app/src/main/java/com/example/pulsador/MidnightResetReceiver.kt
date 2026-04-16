package com.example.pulsador

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log

class MidnightResetReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("MidnightResetReceiver", "Action received: ${intent.action}")
        
        // Reset state to red
        PrefsHelper.setIsGreen(context, false)
        
        // Schedule next midnight alarm
        PulsadorWidgetProvider.scheduleMidnightReset(context)
        
        // Update widgets
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, PulsadorWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        
        val updateIntent = Intent(context, PulsadorWidgetProvider::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
        }
        context.sendBroadcast(updateIntent)
    }
}

package com.example.pulsador

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.widget.RemoteViews
import java.util.Calendar

class PulsadorWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
        scheduleMidnightReset(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (ACTION_WIDGET_CLICK == intent.action) {
            val now = System.currentTimeMillis()
            val lastClickTime = PrefsHelper.getLastClickTime(context)
            
            // Allow click only if 3 seconds have passed
            if (now - lastClickTime >= 3000L) {
                // Vibrate
                vibrate(context)
                
                // Toggle state
                val currentState = PrefsHelper.isGreen(context)
                PrefsHelper.setIsGreen(context, !currentState)
                PrefsHelper.setLastClickTime(context, now)
                
                // Update all widgets
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, PulsadorWidgetProvider::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                for (id in appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, id)
                }
            } else {
                Log.d("Pulsador", "Ignored click, 3 seconds haven't passed")
            }
        }
    }

    override fun onEnabled(context: Context) {
        scheduleMidnightReset(context)
    }

    private fun vibrate(context: Context) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(100)
        }
    }

    companion object {
        const val ACTION_WIDGET_CLICK = "com.example.pulsador.ACTION_WIDGET_CLICK"

        internal fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val isGreen = PrefsHelper.isGreen(context)
            
            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            
            // Set background color based on state
            if (isGreen) {
                views.setImageViewResource(R.id.widget_button, R.drawable.bg_circle_green)
            } else {
                views.setImageViewResource(R.id.widget_button, R.drawable.bg_circle_red)
            }

            // Setup click pending intent
            val intent = Intent(context, PulsadorWidgetProvider::class.java).apply {
                action = ACTION_WIDGET_CLICK
            }
            
            val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                pendingIntentFlags
            )
            
            views.setOnClickPendingIntent(R.id.widget_button, pendingIntent)

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun scheduleMidnightReset(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, MidnightResetReceiver::class.java).apply {
                action = "com.example.pulsador.ACTION_MIDNIGHT_RESET"
            }
            
            val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                1,
                intent,
                pendingIntentFlags
            )

            // Setup calendar for next midnight
            val calendar = Calendar.getInstance().apply {
                timeInMillis = System.currentTimeMillis()
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    try {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            calendar.timeInMillis,
                            pendingIntent
                        )
                    } catch (e: SecurityException) {
                        e.printStackTrace()
                    }
                } else {
                    alarmManager.setWindow(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        1000 * 60 * 10, // 10 minutes window
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        }
    }
}

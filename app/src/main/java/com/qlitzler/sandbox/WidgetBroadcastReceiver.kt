package com.qlitzler.sandbox

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class WidgetBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val widgetId = intent.extras
                ?.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS)
                ?.firstOrNull()
                ?: intent.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)

            if (widgetId != null && widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                val remoteView = RemoteViews(context.packageName, R.layout.widget)
                val configurationIntent = WidgetConfigurationActivity.getIntent(context, widgetId)

                val activity = PendingIntent.getActivity(
                    context,
                    1,
                    configurationIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                remoteView.setOnClickPendingIntent(R.id.open, activity)
                AppWidgetManager.getInstance(context).updateAppWidget(widgetId, remoteView)
            }
        }
    }
}
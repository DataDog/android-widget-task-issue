package com.qlitzler.sandbox

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class WidgetConfigurationActivity : AppCompatActivity(R.layout.widget_configuration_activity) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("[Configuration]: $taskId")
        val intent = OAuthActivity.getIntent(this)

        startActivityForResult(intent, 10)
    }

    companion object {

        fun getIntent(context: Context, widgetId: Int): Intent {
            return Intent(context, WidgetConfigurationActivity::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_CONFIGURE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            }
        }
    }
}
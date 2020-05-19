package com.cooper.wordcard

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import androidx.appcompat.app.AppCompatActivity
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_widget_configure.*


class WidgetConfigureActivity : AppCompatActivity() {

    var sheetId: String = ""
    var widgetId: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget_configure)
        sheetId = intent.getStringExtra("sheetKey")
        widgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        Log.e("cooper", "WidgetConfigureActivity set $widgetId $sheetId")
        button.setOnClickListener {
            var widgetModel = WidgetModel()
            widgetModel.widgetId = widgetId
            widgetModel.sheetId = sheetId
            var realm = Realm.getDefaultInstance()
            realm.beginTransaction()
            realm.copyToRealmOrUpdate(widgetModel)
            realm.commitTransaction()
            realm.close()

            val appWidgetManager = AppWidgetManager.getInstance(this)
            RemoteViews(
                this@WidgetConfigureActivity.packageName,
                R.layout.widget_main
            ).also { views ->
                appWidgetManager.updateAppWidget(widgetId, views)
                Log.e("cooper", "WidgetConfigureActivity update $widgetId")
            }

            val result: Intent = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            setResult(Activity.RESULT_OK, result)
            finish()

        }


    }
}

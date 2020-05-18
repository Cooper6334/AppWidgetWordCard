package com.cooper.wordcard

import android.R.id
import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_widget_configure.*


class WidgetConfigureActivity : AppCompatActivity() {

    var sheetId: String? = null
    var widgetId: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget_configure)
        sheetId = intent.getStringExtra("sheetId")
        widgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        Log.e("cooper", "Get widget $widgetId $sheetId")
        button.setOnClickListener {
            var widgetModel = WidgetModel()
            widgetModel.widgetId = widgetId
            var realm = Realm.getDefaultInstance()
            realm.beginTransaction()
            realm.copyToRealmOrUpdate(widgetModel)
            realm.commitTransaction()
            realm.close()

            val result: Intent = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            setResult(Activity.RESULT_OK, result)
            finish()

        }


    }
}

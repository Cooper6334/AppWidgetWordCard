package com.cooper.wordcard

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import io.realm.Realm
import io.realm.RealmResults

const val ACTION_WIDGET_CLICK_SELF = "com.cooper.wordcard.widget.self"
var index = 0;
const val REQUEST_CODE = 6334;
var strings: ArrayList<String> = ArrayList()

class MyWidgetProvider : AppWidgetProvider() {

    //    val strings = arrayOf("January", "February", "March")

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.e("cooper", "onUpdate")
        if(strings.size == 0){
            Log.e("cooper", "reload data")

            var realm = Realm.getDefaultInstance()
            val query = realm.where(WordCardModel::class.java)
            val result: RealmResults<WordCardModel> = query.findAll()
            result.forEach{
                it.wordList[0]?.let { it1 -> strings.add(it1)
                    Log.e("cooper","Get ${it1}")}
                it.wordList[1]?.let { it1 -> strings.add(it1)
                    Log.e("cooper","Get ${it1}")}
            }
        }
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        //handle click
        if (ACTION_WIDGET_CLICK_SELF == intent.action) {
            if (strings.size == 0) {
                return
            }
            index = (index + 1) % strings.size
            val widgetId = intent.getIntExtra("widgetId", -1)
            val appWidgetManager = AppWidgetManager.getInstance(context)
            if (widgetId != -1) {
                updateAppWidget(context, appWidgetManager, widgetId)
            }
/*
val thisAppWidget = ComponentName(
    context.packageName,
    MyWidgetProvider::class.simpleName
)
Log.e("cooper", "MyWidgetProvider name" + MyWidgetProvider::class.simpleName)
val appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget)
onUpdate(context, appWidgetManager, appWidgetIds)

 */
        }
    }

    fun updateAppWidget(
        context: Context, appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val view = RemoteViews(context.packageName, R.layout.widget_main)

        if (strings.size <= 0) {
            view.setTextViewText(R.id.text_center, "word not found")
        } else {
            //set text
            view.setTextViewText(R.id.text_center, strings.elementAt(index))
        }
        //set onclick
        val intent = Intent(ACTION_WIDGET_CLICK_SELF)
        val componentName = ComponentName(context, MyWidgetProvider::class.java)
        intent.component = componentName
        intent.putExtra("widgetId", appWidgetId)
        val sentPI = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        view.setOnClickPendingIntent(R.id.widget_layout, sentPI)

        //set view
//val manager = AppWidgetManager.getInstance(context)
        appWidgetManager.updateAppWidget(appWidgetId, view)
    }
}


package com.cooper.wordcard

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.RemoteViews
import io.realm.Realm

const val ACTION_WIDGET_CLICK_SELF = "com.cooper.wordcard.widget.self"

const val REQUEST_CODE = 6334;
//var strings: ArrayList<String> = ArrayList()

class MyWidgetProvider : AppWidgetProvider() {

    override fun onEnabled(context: Context) {
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.e("cooper", "onUpdate")
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        Log.e("cooper", "onReceive")
        //handle click
        if (ACTION_WIDGET_CLICK_SELF == intent.action) {
            val widgetId = intent.data.toString().substring(1).toInt()
            val appWidgetManager = AppWidgetManager.getInstance(context)
            if (widgetId != -1) {
                updateAppWidget(context, appWidgetManager, widgetId)
            }
        }
    }

    fun updateAppWidget(
        context: Context, appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        Log.e("cooper", "updateAppWidget $appWidgetId")
        val view = RemoteViews(context.packageName, R.layout.widget_main)
        //set onclick
        val componentName = ComponentName(context, MyWidgetProvider::class.java)
        val intent = Intent(ACTION_WIDGET_CLICK_SELF)
        intent.component = componentName
        intent.data = ContentUris.withAppendedId(Uri.EMPTY, appWidgetId.toLong())
        val sentPI = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        view.setOnClickPendingIntent(R.id.widget_layout, sentPI)

        var realm = Realm.getDefaultInstance()
        var widgetModel: WidgetModel? =
            realm.where(WidgetModel::class.java).equalTo("widgetId", appWidgetId).findFirst()
        if (widgetModel == null) {
            Log.e("cooper", "updateAppWidget failed no widgetModel")
            appWidgetManager.updateAppWidget(appWidgetId, view)
            return
        }
        var sheetModel: SheetModel? =
            realm.where(SheetModel::class.java).equalTo("key", widgetModel.sheetId)
                .findFirst()
        if (sheetModel == null) {
            Log.e("cooper", "updateAppWidget failed no sheetModel")
            appWidgetManager.updateAppWidget(appWidgetId, view)
            return
        }
        realm.beginTransaction()
        widgetModel.lastColumn = widgetModel.lastColumn + 1
        var cl = sheetModel.cardList[widgetModel.lastRow]?.wordList?.size

        if (widgetModel.lastColumn >= sheetModel.cardList[widgetModel.lastRow]?.wordList?.size!!) {
            widgetModel.lastColumn = 0
            widgetModel.lastRow = widgetModel.lastRow + 1
            if (widgetModel.lastRow >= sheetModel.cardList.size) {
                widgetModel.lastRow = 0
            }
        }
        realm.copyToRealmOrUpdate(widgetModel)
        realm.commitTransaction()
        Log.e("cooper", "Get card ${widgetModel.lastRow},${widgetModel.lastColumn}")
        var currentWord =
            sheetModel.cardList[widgetModel.lastRow]!!.wordList[widgetModel.lastColumn]!!
        if (currentWord == null) {
            currentWord = "Word not found"
        }

        view.setTextViewText(R.id.text_center, currentWord)

        //set view
        appWidgetManager.updateAppWidget(appWidgetId, view)
        Log.e("cooper", "updateAppWidget finish")
    }
}


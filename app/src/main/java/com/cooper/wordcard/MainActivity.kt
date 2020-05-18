package com.cooper.wordcard

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    var sheetId: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            if (sheetId.isEmpty()) {
                val intent = Intent(this@MainActivity, LoadSheetActivity::class.java)
                startActivity(intent)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                pinWidget()
            }
        }

        var realm = Realm.getDefaultInstance()
        val query = realm.where(SheetModel::class.java)
        val result: RealmResults<SheetModel> = query.findAll()
        result.forEach {
            sheetId = it.key
            Log.e("cooper", "Get ${it.key}${it.sheetName})${it.tabName}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun pinWidget() {
        val appWidgetManager: AppWidgetManager =
            this@MainActivity.getSystemService(AppWidgetManager::class.java)
        val myProvider = ComponentName(this@MainActivity, MyWidgetProvider::class.java)

        val successCallback: PendingIntent? =
            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                Intent(this@MainActivity, WidgetConfigureActivity::class.java)
                    .let { intent ->
                        intent.putExtra("sheetId", sheetId)
                        PendingIntent.getActivity(
                            this@MainActivity,
                            0,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                        )
                    }
            } else {
                null
            }
        successCallback?.also { pendingIntent ->
            appWidgetManager.requestPinAppWidget(myProvider, null, pendingIntent)
        }
    }


}

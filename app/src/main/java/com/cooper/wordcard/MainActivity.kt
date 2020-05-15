package com.cooper.wordcard

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            val intent = Intent(this@MainActivity, LoadSheetActivity::class.java)
            startActivity(intent)
            /*
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                pinWidget()
            }*/
        }

        /*
        var realm = Realm.getDefaultInstance()
        val query = realm.where(WordCardModel::class.java)
        val result: RealmResults<WordCardModel> = query.findAll()
        result.forEach{
            Log.e("cooper","Get ${it.wordList.first()}")
        }
         */
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun pinWidget(){
        val appWidgetManager: AppWidgetManager =
            this@MainActivity.getSystemService(AppWidgetManager::class.java)
        val myProvider = ComponentName(this, MyWidgetProvider::class.java)
        var pinnedWidgetCallbackIntent:Intent = Intent();
        var  successCallback:PendingIntent = PendingIntent.getBroadcast(this, 0,
            pinnedWidgetCallbackIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        appWidgetManager.requestPinAppWidget(myProvider, null, successCallback);
    }


}

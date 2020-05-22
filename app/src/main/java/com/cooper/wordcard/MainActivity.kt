package com.cooper.wordcard

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            val intent = Intent(this@MainActivity, LoadSheetActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        var realm = Realm.getDefaultInstance()
        val query = realm.where(SheetModel::class.java)
        val result: RealmResults<SheetModel> = query.findAll()
        var nameList = ArrayList<String>()
        var keyList = ArrayList<String>()
        result.forEach {
            //Log.e("cooper", "Get ${it.key}${it.sheetName})${it.tabName}")
            nameList.add("${it.sheetName}\n${it.tabName}")
            keyList.add(it.key)
        }

        mainRecycler.layoutManager = LinearLayoutManager(this);
        var adapter = SheetAdapter(nameList, keyList)
        mainRecycler.adapter = adapter
    }

    fun pinWidget(sheetKey: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            //TODO show message
            return
        } else {
            val appWidgetManager: AppWidgetManager =
                this@MainActivity.getSystemService(AppWidgetManager::class.java)
            val myProvider = ComponentName(this@MainActivity, MyWidgetProvider::class.java)

            val successCallback: PendingIntent? =
                if (appWidgetManager.isRequestPinAppWidgetSupported) {
                    Intent(this@MainActivity, WidgetConfigureActivity::class.java)
                        .let { intent ->
                            intent.putExtra("sheetKey", sheetKey)
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

    inner class SheetAdapter(var nameList: ArrayList<String>, var keyList: ArrayList<String>) :
        RecyclerView.Adapter<SheetAdapter.Holder>() {
        inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            init {
                itemView.setOnClickListener {
                    this@MainActivity.pinWidget(keyList[adapterPosition])
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val v: View =
                LayoutInflater.from(parent.context)
                    .inflate(android.R.layout.simple_list_item_1, parent, false)
            return Holder(v)
        }

        override fun getItemCount(): Int {
            return nameList.size
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            val tv: TextView = holder.itemView as TextView
            tv.text = nameList[position]
        }
    }

}

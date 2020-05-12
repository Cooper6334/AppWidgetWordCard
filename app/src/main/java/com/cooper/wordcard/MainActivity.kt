package com.cooper.wordcard

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.api.services.drive.model.User
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            val intent = Intent(this@MainActivity, LoadSheetActivity::class.java)
            startActivity(intent)
        }
        var realm = Realm.getDefaultInstance()
        val query = realm.where(WordCardModel::class.java)
        val result: RealmResults<WordCardModel> = query.findAll()
        result.forEach{
            Log.e("cooper","Get ${it.wordList.first()}")
        }
    }


}

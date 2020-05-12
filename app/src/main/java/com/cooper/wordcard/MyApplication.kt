package com.cooper.wordcard

import android.util.Log
import io.realm.Realm
import io.realm.RealmConfiguration

class MyApplication : androidx.multidex.MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
        var mConfiguration = RealmConfiguration.Builder()
            .schemaVersion(1)
            .migration(MyMigration())
            .build()
        Realm.setDefaultConfiguration(mConfiguration)
        val realm = Realm.getDefaultInstance()
        Log.e("Cooper", "init realm")
    }
}

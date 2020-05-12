package com.cooper.wordcard

import android.util.Log
import io.realm.DynamicRealm
import io.realm.FieldAttribute
import io.realm.RealmMigration

open class MyMigration: RealmMigration {

    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        Log.e("Cooper","try migrate ${oldVersion} ${newVersion}")
        var oldVersion = oldVersion
        val schema = realm.schema
        if (oldVersion == 0L) {
            oldVersion++
        }
    }
}
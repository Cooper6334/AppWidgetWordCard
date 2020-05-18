package com.cooper.wordcard

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class WidgetModel(
    @PrimaryKey var widgetId: Int = 0,
    var sheetId: String = "",
    var lastRow: Int = 0,
    var random: Boolean = false
) : RealmObject()
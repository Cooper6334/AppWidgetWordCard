package com.cooper.wordcard

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class SheetModel(
    @PrimaryKey
    var key: String = "",
    var sheetId: String = "",
    var tabId: Int = 0,
    var sheetName: String = "",
    var tabName: String = "",
    var wordList: RealmList<WordCardModel> = RealmList<WordCardModel>()
) : RealmObject()
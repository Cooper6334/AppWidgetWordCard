package com.cooper.wordcard

import io.realm.RealmList
import io.realm.RealmObject

open class SheelModel(var id: Int = 0,
                      var wordList: RealmList<WordCardModel> = RealmList<WordCardModel>()): RealmObject()
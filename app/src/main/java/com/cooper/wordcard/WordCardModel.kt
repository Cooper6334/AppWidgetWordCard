package com.cooper.wordcard

import io.realm.RealmList
import io.realm.RealmObject


open class WordCardModel(var id: Int = 0, var wordList:RealmList<String> = RealmList<String>()): RealmObject()
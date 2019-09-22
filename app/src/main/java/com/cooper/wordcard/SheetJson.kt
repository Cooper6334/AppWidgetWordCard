package com.cooper.wordcard

data class SheetJson(
    val majorDimension: String,
    val range: String,
    val values: Array<Array<String>>
)
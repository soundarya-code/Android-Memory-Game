package com.example.gamy.models

data class Memorycard(
    val identifier:Int,
    val imageUrl:String?=null,
    var isFaceUp:Boolean=false,
    var isMatched:Boolean=false
)


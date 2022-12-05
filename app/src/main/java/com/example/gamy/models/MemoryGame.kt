package com.example.gamy.models

import com.example.gamy.utils.DEFAULT__iCONS

class MemoryGame(private val boardSize: BoardSize,private  val customGameImages: List<String>?) {
    private var numCardFlips=0
    private var indexOfsingleSelectedCard:Int?=null
    var cards:List<Memorycard>
    var numPairsfound=0

    init{
        if(customGameImages==null){

        val choosen_image= DEFAULT__iCONS.shuffled().take(boardSize.getNumPairs())
        val randomImage=(choosen_image+choosen_image).shuffled()
        cards=randomImage.map{Memorycard(it)}
    }else{
        val randomImage=(customGameImages+customGameImages).shuffled()
            cards=randomImage.map {
                Memorycard(it.hashCode(),it)
            }
    }}
    fun flipCard(position: Int):Boolean {
val card:Memorycard=cards[position]
        numCardFlips++

        var foundMatch=false
        if(indexOfsingleSelectedCard==null){
            restoreCards()
            indexOfsingleSelectedCard=position
        }else{
            foundMatch=checkForMatch(indexOfsingleSelectedCard!!,position)
            indexOfsingleSelectedCard=null
        }
        card.isFaceUp=!card.isFaceUp
        return foundMatch
    }

    private fun checkForMatch(position1: Int, position2: Int):Boolean {
if(cards[position1].identifier!=cards[position2].identifier){
    return false
}
        cards[position1].isMatched=true
        cards[position2].isMatched=true
        numPairsfound++
        return true
    }


    private fun restoreCards() {
     for(card in cards){
         if(!card.isMatched){
             card.isFaceUp=false
         }
     }
    }

    fun haveWonGame(): Boolean {
return  numPairsfound==boardSize.getNumPairs()
    }

    fun isCardFaceUp(position: Int): Boolean {
return cards[position].isFaceUp
    }

    fun getNumMoves1():Int {
return numCardFlips/2
    }


}

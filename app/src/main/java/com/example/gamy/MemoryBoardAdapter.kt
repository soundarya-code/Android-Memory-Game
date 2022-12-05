package com.example.gamy

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.gamy.models.BoardSize
import com.example.gamy.models.Memorycard
import com.squareup.picasso.Picasso
import kotlin.math.min

private const val TAG="MemoryBoardAdapter"
class MemoryBoardAdapter(
    private val context: Context,
    private val boardSize: BoardSize,
    private val CardImage: List<Int>,
    private val cards: List<Memorycard>,
    private val cardClickListener: CardClickListener
) : RecyclerView.Adapter<MemoryBoardAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView) {
        private val imageButton=itemView.findViewById<ImageButton>(R.id.imageButton)

        fun bind(position: Int) {
            val memoryCard = cards[position]
            if (memoryCard.isFaceUp) {
                if (memoryCard.imageUrl != null) {
                    Picasso.get().load(memoryCard.imageUrl).into(imageButton)
                    Picasso.get().load(memoryCard.imageUrl).placeholder(R.drawable.ic_image).into(imageButton)

                } else {
                    imageButton.setImageResource(memoryCard.identifier)
                }
            } else {
                imageButton.setImageResource(R.drawable.ic_launcher_background)

            }
            imageButton.alpha = if (memoryCard.isMatched) .4f else 1f
            val colorStateList = if (memoryCard.isMatched) ContextCompat.getColorStateList(context, R.color.color_gray) else null

            imageButton.setOnClickListener {
    Log.i(TAG,"clicked on position $position")
    cardClickListener.onClickPosition(position)
}
        }
    }
    companion object{
        private const val TAG="MemoryBoardAdapter"
private const val MARGIN_SIZE=10
    }

    interface CardClickListener{
        fun onClickPosition(position:Int)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
         val Cardwidth:Int=parent.width/boardSize.getWidth()-(2* MARGIN_SIZE)
      val Cardheight:Int=parent.height/boardSize.getHeight()-(2* MARGIN_SIZE)
        val CardSideLength=min( Cardwidth,Cardheight)
       val view=LayoutInflater.from(context).inflate(R.layout.memory_card,parent,false)
       val layoutParams= view.findViewById<CardView>(R.id.cardView).layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.width=CardSideLength
        layoutParams.height=CardSideLength
        layoutParams.setMargins(MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

    holder.bind(position)

    }

    override fun getItemCount()=boardSize.numCards

}

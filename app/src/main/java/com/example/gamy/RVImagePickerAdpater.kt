package com.example.gamy

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.gamy.models.BoardSize
import kotlin.math.min
private const val TAG="RVImagePickerAdapter"
class RVImagePickerAdpater(private var context: Context,
                           private var chossenImageURL: List<Uri>,
                           private var boardSize: BoardSize,
                           private val imageClickListener: ImageClickListener
) : RecyclerView.Adapter<RVImagePickerAdpater.ViewHolder>() {
    interface ImageClickListener{
        fun  foo()
//            class do1(){
//fun click(){
//
//}
//
//
//        }


        fun click(){}
    }

    class inner class ViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView) {


        val customimage=itemView.findViewById<ImageView>(R.id.imageView)

        fun bind(uri:Uri) {
            customimage.setImageURI(uri)
            customimage.setOnClickListener(null)
        }
        fun bind(imageClickListener: ImageClickListener) {
            Log.i(TAG, "CLICKED")
            customimage.setOnClickListener {

                imageClickListener.foo()

            }
        }



    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view=LayoutInflater.from(context).inflate(R.layout.card_image,parent,false)
        val card_width=parent.width/boardSize.getWidth()
        val card_height=parent.height/boardSize.getHeight()
        val cardSideLenght=min(card_width,card_height)
        val layoutParams= view.findViewById<ImageView>(R.id.imageView).layoutParams
        layoutParams.width=cardSideLenght
        layoutParams.height=cardSideLenght
        return ViewHolder(view)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(position<chossenImageURL.size){
            holder.bind(chossenImageURL[position])
        }else{
            holder.bind(imageClickListener)
        }
    }

    override fun getItemCount()=boardSize.getNumPairs()

}

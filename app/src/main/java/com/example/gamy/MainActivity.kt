package com.example.gamy

import android.animation.ArgbEvaluator
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gamy.models.BoardSize
import com.example.gamy.models.MemoryGame
import com.example.gamy.models.Memorycard
import com.example.gamy.models.UsageImageList
import com.example.gamy.utils.DEFAULT__iCONS
import com.example.gamy.utils.EXTRA_BOARD_SIZE
import com.example.gamy.utils.EXTRA_GAME_NAME
import com.github.jinatonic.confetti.CommonConfetti
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {
    private lateinit var clRoot:CoordinatorLayout
    private lateinit var Recycler:RecyclerView
    private lateinit var text2:TextView
    private  lateinit var text1:TextView
    private  var boardSize:BoardSize=BoardSize.EASY
    private lateinit var memoryGame:MemoryGame
    private lateinit var  adapter:MemoryBoardAdapter
private val db=Firebase.firestore
    private var GameName:String?=null
    private var customGameImages:List<String>?=null
    companion object{
    private const val TAG="MainActivity"
    private const val CREATE_REQUEST_CODE=248
}


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        clRoot=findViewById(R.id.clRoot)
        Recycler=findViewById(R.id.Recycle)
        text1=findViewById(R.id.text1)
        text2=findViewById(R.id.text2)
        val intent=Intent(this,CreateActivity::class.java)
        intent.putExtra(EXTRA_BOARD_SIZE,BoardSize.EASY)
        startActivity(intent)
        setUpBoard()

    }


    private fun updateGamewithflip(position: Int) {
        if(memoryGame.haveWonGame()){
            Snackbar.make(clRoot,"ypu already won1",Snackbar.LENGTH_LONG).show()
            return

        }
        if(memoryGame.isCardFaceUp(position)) {
            Snackbar.make(clRoot, "ypu already won1", Snackbar.LENGTH_LONG).show()
            return

        }


if(memoryGame.flipCard(position)){
    Log.i(TAG,"Found match!Num pairs found: ${memoryGame.numPairsfound}")
    val color=ArgbEvaluator().evaluate(memoryGame.numPairsfound.toFloat()/boardSize.getNumPairs(),
    ContextCompat.getColor(this,R.color.color_progress_home),
    ContextCompat.getColor(this,R.color.color_progress_full))as Int
text1.setTextColor(color)
    text1.text="Pairs:${memoryGame.numPairsfound}/${boardSize.getNumPairs()}"
    if(memoryGame.haveWonGame()){
        Snackbar.make(clRoot,"you won congralution",Snackbar.LENGTH_LONG).show()
        CommonConfetti.rainingConfetti(clRoot, intArrayOf(Color.YELLOW,Color.GREEN,Color.MAGENTA)).oneShot()
    }
}
        text2.text="Moves:${memoryGame.getNumMoves1()}"
        adapter.notifyDataSetChanged()
    }
    private fun setUpBoard(){
        supportActionBar?.title=GameName?:getString(R.string.app_name)
        when(boardSize){
            BoardSize.EASY ->{
                text2.text="Easy:4X2"
                text1.text="Pairs:0/4"
            }
            BoardSize.MEDIUM -> {
                text2.text="Easy:6X3"
                text1.text="Pairs:0/9"
            }
            BoardSize.HARD ->{
                text2.text="Easy:6X4"
                text1.text="Pairs:0/12"
            }
        }
        text1.setTextColor(ContextCompat.getColor(this,R.color.color_progress_home))
        val choosen_image=DEFAULT__iCONS.shuffled().take(boardSize.getNumPairs())
        val randomImage=(choosen_image+choosen_image).shuffled()
        val memorycards=randomImage.map{Memorycard(it)}
        memoryGame=MemoryGame(boardSize,customGameImages)
        adapter=MemoryBoardAdapter(this,boardSize,randomImage,memoryGame.cards,object:MemoryBoardAdapter.CardClickListener{

            override fun onClickPosition(position: Int) {
                Log.i(TAG,"card position $position")
//                super.onClickPosition(position)
                updateGamewithflip(position)
            }
        })
        Recycler.adapter=adapter
        Recycler.setHasFixedSize(true)
        Recycler.layoutManager=GridLayoutManager(this,boardSize.getWidth())
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.refresh->{
                setUpBoard()
                if(memoryGame.getNumMoves1()>0 && !memoryGame.haveWonGame() ){
                    showAlertDialog("you want to want Quit game?",null,View.OnClickListener {
                        setUpBoard()
                    })
                }else{
                    setUpBoard()
                }
            }
            R.id.mi_new_size->{
                showNewSizeDialog()
                    return true

            }
            R.id.mi_custom->{
showCreationButton()
        }
            R.id.mi_download->{
                showDownloadDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showDownloadDialog() {
       val boardDownView=LayoutInflater.from(this).inflate(R.layout.dialog_board_board,null)
        showAlertDialog("Fetch memory game",boardDownView,View.OnClickListener {
            val etDownloadGame=boardDownView.findViewById<EditText>(R.id.etDownloadGame)
            val gameToDownload=etDownloadGame.text.toString().trim()
            downloadGame(gameToDownload)

        })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode==CREATE_REQUEST_CODE && resultCode==Activity.RESULT_OK){
            val CustomGameName=data?.getStringExtra(EXTRA_GAME_NAME)
            if (CustomGameName==null){
                Log.e(TAG,"got null custom game from Createactivity")
                return
            }
            downloadGame(CustomGameName)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun downloadGame(customGameName: String) {
db.collection("games").document(customGameName).get().addOnSuccessListener {document->
    val userImageList=document.toObject(UsageImageList::class.java)
    if(userImageList?.images==null){
        Log.e(TAG,"invalid custom game data from firestore")
        Snackbar.make(clRoot,"Sorry ,we couldn't find any  such game $customGameName",Snackbar.LENGTH_LONG).show()
        return@addOnSuccessListener
    }
val numCards=userImageList.images.size*2
    boardSize=BoardSize.getByValue(numCards)
    customGameImages=userImageList.images
    for(imageUrl in userImageList.images){
        Picasso.get().load(imageUrl).fetch()
    }
    Snackbar.make(clRoot,"You are playing $customGameName",Snackbar.LENGTH_LONG).show()
    GameName=customGameName
setUpBoard()


}.addOnFailureListener{exception->
    Log.e(TAG,"Exception when retriving the game",exception)
}
    }

    private fun showCreationButton() {
        val boardSizeView= LayoutInflater.from(this).inflate(R.layout.dialog_board_size,null)
        val radioGroupsize=boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)
        showAlertDialog("choose new size",boardSizeView,View.OnClickListener {
            val desireboardSize=when(radioGroupsize.checkedRadioButtonId){
                R.id.radioButton->BoardSize.EASY
                R.id.radioButton2->BoardSize.MEDIUM

                else->BoardSize.HARD
            }
            val intent= Intent(this,CreateActivity::class.java)
            intent.putExtra(EXTRA_BOARD_SIZE,desireboardSize)
            startActivityForResult(intent,CREATE_REQUEST_CODE)
        })
    }

    private fun showNewSizeDialog() {

       val boardSizeView= LayoutInflater.from(this).inflate(R.layout.dialog_board_size,null)
        val radioGroupsize=boardSizeView.findViewById<RadioGroup>(R.id.radioGroup)
        when(boardSize){
            BoardSize.EASY->radioGroupsize.check(R.id.radioButton)
            BoardSize.MEDIUM->radioGroupsize.check(R.id.radioButton2)
            BoardSize.HARD->radioGroupsize.check(R.id.radioButton3)
        }
      showAlertDialog("choose new size",boardSizeView,View.OnClickListener {
boardSize=when(radioGroupsize.checkedRadioButtonId){
    R.id.radioButton->BoardSize.EASY
    R.id.radioButton2->BoardSize.MEDIUM

    else->BoardSize.HARD
}
          GameName=null
          customGameImages=null
          setUpBoard()
      })
    }


    private fun showAlertDialog(title:String,view: View?,positiveClickListener: View.OnClickListener) {
       AlertDialog.Builder(this ).setTitle(title).setView(view).setNegativeButton("cancel",null)
           .setPositiveButton("Ok"){
               _,_ ->
               positiveClickListener.onClick(null)
           }.show()
    }
}
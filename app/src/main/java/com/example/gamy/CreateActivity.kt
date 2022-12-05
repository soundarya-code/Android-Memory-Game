package com.example.gamy

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gamy.models.BoardSize
import com.example.gamy.utils.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream

private const val TAG = "CreateActivity "

class CreateActivity : AppCompatActivity() {

    private lateinit var RVImagePicker: RecyclerView
    private lateinit var editText: EditText
    private lateinit var buttonSafe: Button
    private lateinit var boardSize: BoardSize
    private var numImageRequired = -1
    private var PICK_PHOTOS_CODE = 655
    private var READ_EXTERNAL_PHOTOS = 248
    private val READ_PHOTOS_PERMISSION = android.Manifest.permission.READ_EXTERNAL_STORAGE
    private lateinit var adapter: RVImagePickerAdpater
    private val chosenImagesUri = mutableListOf<Uri>()
    private val storage = Firebase.storage
    private val db = Firebase.firestore
    private val MAX_NAME_LENGHT = 14
    private val MIN_NAME_LENGHT = 3
    private var chossenImageURL = mutableListOf<Uri>()
    private lateinit var progressBar:ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

        RVImagePicker = findViewById(R.id.ImagePicker)
        editText = findViewById(R.id.EditText)
        buttonSafe = findViewById(R.id.buttonSafe)
        progressBar= findViewById(R.id.progressBar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val boardSize = intent.getSerializableExtra(EXTRA_BOARD_SIZE) as BoardSize
        numImageRequired = boardSize.getNumPairs()
        supportActionBar?.title = "Choose pics (0/${numImageRequired})"
        buttonSafe.setOnClickListener {

                savedataToFirebase()

        }
        editText.filters = arrayOf(InputFilter.LengthFilter(MAX_NAME_LENGHT))
        editText.addTextChangedListener((object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                buttonSafe.isEnabled = shouldEnable()
            }

        }))

       adapter = RVImagePickerAdpater(this,
            chossenImageURL,
            boardSize,
            object : RVImagePickerAdpater.ImageClickListener {
                override fun foo() {
                    if (isPermissionGranted(this@CreateActivity, READ_PHOTOS_PERMISSION)) {
                        lauchPhotoActivty()
                    } else {
                        requestPermission(
                            this@CreateActivity, READ_PHOTOS_PERMISSION, READ_EXTERNAL_PHOTOS
                        )
                    }
                }

            })
        RVImagePicker.adapter = adapter
        RVImagePicker.setHasFixedSize(true)
        RVImagePicker.layoutManager = GridLayoutManager(this, boardSize.getWidth())
    }

    private fun savedataToFirebase() {

        val customGameName=editText.text.toString()
        buttonSafe.isEnabled=false
        db.collection("games").document(customGameName).get().addOnSuccessListener { document->
            if(document!=null && document.data!=null){
                AlertDialog.Builder(this).setTitle("name taken").setMessage("A game already taken with name $customGameName")
                    .setPositiveButton("Ok",null).show()
                buttonSafe.isEnabled=true
            }else{
                handleImageUploaded(customGameName)
            }
        }.addOnFailureListener{exception->
            Log.e(TAG,"Encounter error while saving memory game",exception)
            Toast.makeText(this,"Encounter error while saving memory game",Toast.LENGTH_SHORT).show()
            buttonSafe.isEnabled=true
        }
    }

    private fun handleImageUploaded(gameName: String) {
var   didEncounterError=false
progressBar.visibility=View.VISIBLE

        var uploadImageUrls= mutableListOf<String>()
        Log.i(TAG, "SAVEdATAINfIREBASE")
        for ((index, photoUri) in chossenImageURL.withIndex()) {
            val imageByteArray = getImageByArray(photoUri)
            val filepath="images/$gameName/${System.currentTimeMillis()}-${index}.jpg"
            val photoreference=storage.reference.child(filepath)
            photoreference.putBytes(imageByteArray).continueWithTask {
                    photoUploadTask->
                Log.i(TAG,"Upload bytes:${photoUploadTask.result?.bytesTransferred}")
                photoreference.downloadUrl
            }.addOnCompleteListener { downloadUrlTask->
                if(!downloadUrlTask.isSuccessful){
                    Log.e(TAG,"EXCEPTION WITH FIREBASE STORAGE",downloadUrlTask.exception)
                    Toast.makeText(this,"failed to upload image",Toast.LENGTH_SHORT).show()
                    didEncounterError=true
                    return@addOnCompleteListener
                }
                if(didEncounterError){
                    progressBar.visibility=View.GONE
                    return@addOnCompleteListener
                }
                val downloadUrl=downloadUrlTask.result.toString()
                progressBar.progress=uploadImageUrls.size*100/chossenImageURL.size
                uploadImageUrls.add(downloadUrl)
                Log.i(TAG,"finished Uploaded $photoUri,num uploaded ${uploadImageUrls.size}")
                if(uploadImageUrls.size==chossenImageURL.size){
                    handleAllImageUploaded(gameName,uploadImageUrls)
                    Log.i(TAG,"DONE!!")
                }
            }

        }
    }

    private fun handleAllImageUploaded(gameName: String, imageUrls: MutableList<String>) {
        db.collection("games").document(gameName)
            .set(mapOf("images" to imageUrls))
            .addOnCompleteListener { gameCreationTask ->
//                pbUploading.visibility = View.GONE
                progressBar.visibility=View.GONE
                if (!gameCreationTask.isSuccessful) {
                    Log.e(TAG, "Exception with game creation", gameCreationTask.exception)
                    Toast.makeText(this, "Failed game creation", Toast.LENGTH_SHORT).show()
                    return@addOnCompleteListener
                }

                Log.i(TAG, "Successfully created game $gameName")
                AlertDialog.Builder(this)
                    .setTitle("Upload complete! Let's play your game '$gameName'")
                    .setPositiveButton("OK") { _, _ ->
                        val resultData = Intent()
                        resultData.putExtra(EXTRA_GAME_NAME, gameName)
                        setResult(Activity.RESULT_OK, resultData)
                        finish()
                    }.show()
            }
    }


    private fun getImageByArray(photoUri: Uri): ByteArray {
        val originalBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(contentResolver, photoUri)
            ImageDecoder.decodeBitmap(source)

        } else {
            MediaStore.Images.Media.getBitmap(contentResolver, photoUri)
        }
        Log.i(TAG, "ORIGINAL WIDTH ${originalBitmap.width} and height ${originalBitmap.height}")
        val scaledBitmap = BitmapScaler.scaleToFitHeight(originalBitmap, 250)
        Log.i(TAG, "SCALED WIDTH ${scaledBitmap.width} and height ${scaledBitmap.height}")
        val byteOutputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, byteOutputStream)
        return byteOutputStream.toByteArray()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        if (requestCode == READ_EXTERNAL_PHOTOS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                lauchPhotoActivty()
            } else {
                Toast.makeText(
                    this,
                    "in order to create a custom game,you need to provide access to your photos",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun lauchPhotoActivty() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(Intent.createChooser(intent, "Choose pics"), PICK_PHOTOS_CODE)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return true

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != PICK_PHOTOS_CODE || resultCode != Activity.RESULT_OK || data == null) {
            Log.w(TAG, "DID NOT GET DATA BACK FROM THE LAUNCHED ACTIVITY,user likely canceled flow")
            return
        }
        val selectedUri = data.data
        val clipdata = data.clipData
        if (clipdata != null) {
            Log.i(TAG, "clipData Images ${clipdata.itemCount}:$clipdata")
            for (i in 0 until clipdata.itemCount) {
                val clipItem = clipdata.getItemAt(i)
                if (chossenImageURL.size < numImageRequired) {
                    chossenImageURL.add(clipItem.uri)
                }
            }

        } else if (selectedUri != null) {
            Log.i(TAG, "DATA:$selectedUri")
            chossenImageURL.add(selectedUri)
        }
        adapter.notifyDataSetChanged()
        supportActionBar?.title = "Choose  pics(${chossenImageURL.size}/$numImageRequired)"
        buttonSafe.isEnabled = shouldEnable()

    }

    private fun shouldEnable(): Boolean {
        if (chossenImageURL.size != numImageRequired) {
            return false
        }
        if (editText.text.isBlank() || editText.text.length < 3) {
            return false
        }
        return true
    }

}


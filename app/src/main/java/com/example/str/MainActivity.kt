package com.example.str

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import java.io.*
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {

    private var isSerializedDataStored = false

    private val SERIALIZED_DATA_FILENAME = "image_data"

    private val SHARED_PREF_IS_DATA_STORED_KEY = "is_data_stored"

    private val CAMERA_REQUEST = 1888

    var count=0

    lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    lateinit var frameAnalyser  : FrameAnalyse
    lateinit var faceNetModel : FaceModel
    lateinit var fileReader : FileReader
    val modelInfo = Model.FACENET
    lateinit var sharedPreferences: SharedPreferences
    lateinit var filee:File


    companion object {

        lateinit var logTextView : TextView
        lateinit var abspath:String

        fun setName(message: String) {
            logTextView.text = message
            logTextView.setBackgroundColor(Color.GRAY)
        }

    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

      /*  val path = Environment.getDataDirectory().absolutePath.toString() + "/storage/emulated/0/appFolder"
        val mFolder = File(path)
        if (!mFolder.exists()) {
            mFolder.mkdir()
            Toast.makeText(this,"Created",Toast.LENGTH_SHORT).show()
        }
        val Directory = File("/Internal storage/myappFolder/")
        Directory.mkdirs()*/

        logTextView = findViewById<TextView>(R.id.res)

        sharedPreferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)
        isSerializedDataStored = sharedPreferences.getBoolean(SHARED_PREF_IS_DATA_STORED_KEY, false)

//        Log.w("a", this.getExternalFilesDir(null).toString())
       //  this.getExternalFilesDir(DOW)

        val file = File(this.getExternalFilesDir(null).toString() + "/images")
        if (!file.exists()) {
            file.mkdirs()
        }
        filee=file
        abspath=file.absolutePath

        val boundingBoxOverlay = findViewById<OverlayBox>(R.id.bbo)
        boundingBoxOverlay.setWillNotDraw(false)
        boundingBoxOverlay.setZOrderOnTop(true)


        faceNetModel = FaceModel(this, modelInfo, false, true)
        frameAnalyser = FrameAnalyse(this, boundingBoxOverlay, faceNetModel)
        fileReader = FileReader(faceNetModel)





          opendir()
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestCameraPermission()
            }
           // btn.isClickable=false
           // btn.setBackgroundColor(Color.GRAY)


        var addPerson=findViewById<Button>(R.id.adduser)
        addPerson.setOnClickListener{
            var intent=Intent(this, AddPerson::class.java)
            startActivityForResult(intent, RESULT_FIRST_USER)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode== RESULT_FIRST_USER)
        {
            if(resultCode== RESULT_OK)
            {
                load()
            }
        }
    }
    private fun requestCameraPermission() {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    fun startCam()
    {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(this))
    }





    var cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (!isGranted) {
            val alertDialog = AlertDialog.Builder(this).apply {
                setTitle("Camera Permission")
                setMessage("The app couldn't function without the camera permission.")
                setCancelable(false)
                setPositiveButton("ALLOW") { dialog, which ->
                    dialog.dismiss()
                    requestCameraPermission()
                }
                setNegativeButton("CLOSE") { dialog, which ->
                    dialog.dismiss()
                    finish()
                }
                create()
            }
            alertDialog.show()
        }

    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider?) {
        var preview: Preview = Preview.Builder()
                .build()

        var cameraSelector: CameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build()
        var previewww = findViewById<PreviewView>(R.id.previewView)
        preview.setSurfaceProvider(previewww.surfaceProvider)

        val imageFrameAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(Size(480, 640))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
        imageFrameAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), frameAnalyser)
        if (cameraProvider != null&& count==0) {
            cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, preview, imageFrameAnalysis)
            count=1
        }


    }


    /*Directory Selecting*/

   // @RequiresApi(Build.VERSION_CODES.N)
    /*private val directoryAccessLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val dirUri = it.data?.data ?: return@registerForActivityResult
        val childrenUri =
                DocumentsContract.buildChildDocumentsUriUsingTree(
                        dirUri,
                        DocumentsContract.getTreeDocumentId(dirUri)
                )*/
        //val tree = DocumentFile.fromTreeUri(this, childrenUri)
   @RequiresApi(Build.VERSION_CODES.N)
   fun load() {
       val images = ArrayList<Pair<String, Bitmap>>()
       val tree = DocumentFile.fromFile(filee)
       var errorFound = false

       if (tree != null) {
           Log.w("Aa", tree.name.toString())
           for (doc in tree.listFiles()) {
               if (doc.isDirectory && !errorFound) {
                   val name = doc.name!!
                   for (imageDocFile in doc.listFiles()) {
                       try {
               //            Toast.makeText(this, name.toString(), Toast.LENGTH_SHORT).show()
               //            Toast.makeText(this, imageDocFile.listFiles().size.toString(), Toast.LENGTH_SHORT).show()
                           images.add(Pair(name, getFixedBitmap(imageDocFile.uri)))
                       } catch (e: Exception) {
                           errorFound = true
                           break
                       }
                   }
                   Log.w("Size", name.toString() + " " + images.size.toString())
               } else {
                   errorFound = true
               }
           }
       }
       if (images.size != 0){
           fileReader.run(images, fileReaderCallback)
   }
       else
       {
           Toast.makeText(this, "Please add a user first", Toast.LENGTH_LONG).show()
       }
   //     frameAnalyser.faceList = loadSerializedImageData()

    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun getFixedBitmap(uri: Uri): Bitmap {
        var imageBitmap = BM.getBitmapFromUri(contentResolver, uri)
        val exifInterface = ExifInterface(contentResolver.openInputStream(uri)!!)
        imageBitmap =
            when (exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> BM.rotateBitmap(imageBitmap, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> BM.rotateBitmap(imageBitmap, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> BM.rotateBitmap(imageBitmap, 270f)
                else -> imageBitmap
            }
        return imageBitmap
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun opendir() {
        load()
    }




    private val fileReaderCallback = object : FileReader.ProcessCallback {
        override fun onProcessCompleted(data: ArrayList<Pair<String, FloatArray>>, numImagesWithNoFaces: Int) {
            Log.w("OPC", data.size.toString() + " " + numImagesWithNoFaces.toString())
            frameAnalyser.faceList = data
            saveSerializedImageData(data)
            startCam()
        }
    }


    private fun saveSerializedImageData(data: ArrayList<Pair<String, FloatArray>>) {
        val serializedDataFile = File(filesDir, SERIALIZED_DATA_FILENAME)
        ObjectOutputStream(FileOutputStream(serializedDataFile)).apply {
            writeObject(data)
            flush()
            close()
        }
        sharedPreferences.edit().putBoolean(SHARED_PREF_IS_DATA_STORED_KEY, true).apply()
    }


    private fun loadSerializedImageData() : ArrayList<Pair<String, FloatArray>> {
        val serializedDataFile = File(filesDir, SERIALIZED_DATA_FILENAME)
        val objectInputStream = ObjectInputStream(FileInputStream(serializedDataFile))
        val data = objectInputStream.readObject() as ArrayList<Pair<String, FloatArray>>
        objectInputStream.close()
        return data
    }

}



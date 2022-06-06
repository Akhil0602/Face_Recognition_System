package com.example.str

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.util.TypedValue
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.core.impl.utils.executor.CameraXExecutors
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


class AddPerson : AppCompatActivity(){


    lateinit var add:Button
    lateinit var namedir:String
    lateinit var name:EditText
    lateinit var cpt:ImageButton
    lateinit var view:PreviewView
    lateinit var img:ImageView

    private val CAMERA_REQUEST = 1888
    lateinit var retake:Button
    var count=0
    lateinit var imageCapture: ImageCapture
    lateinit var confirm:Button
    lateinit var addimg: Button

    lateinit var saveimage: MutableList<Bitmap>

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var intent=intent
        setContentView(R.layout.add_person)
        add=findViewById<Button>(R.id.add)
        name=findViewById<EditText>(R.id.username)
        //cpt=findViewById<ImageButton>(R.id.cpt_btn)
      //  view=findViewById<PreviewView>(R.id.capture)
        img=findViewById<ImageView>(R.id.person)
     ////   retake=findViewById<Button>(R.id.retake)
      //  confirm=findViewById(R.id.confirm)
        addimg=findViewById(R.id.addimg)
        name.isEnabled=true
        saveimage= mutableListOf()

        /*val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, CAMERA_REQUEST)*/
     //   startCam()

    /*    confirm.setOnClickListener {
            val dimensionInDp2 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0f, resources.displayMetrics).toInt()
            val dimensionInDp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60f, resources.displayMetrics).toInt()
            val dimensionInDp3 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 200f, resources.displayMetrics).toInt()
            retake.layoutParams.height = dimensionInDp2
            retake.layoutParams.width = dimensionInDp2
            retake.requestLayout()

            addimg.layoutParams.height = dimensionInDp
            addimg.layoutParams.width = dimensionInDp3
            addimg.requestLayout()

            confirm.layoutParams.height = dimensionInDp2
            confirm.layoutParams.width = dimensionInDp2
            confirm.requestLayout()
            name.isEnabled=true
        }
*/
          addimg.setOnClickListener {

              val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
              startActivityForResult(cameraIntent, CAMERA_REQUEST)

        }

  /*      retake.setOnClickListener { reset() }*/

        add.setOnClickListener{
            var x:String= name.text.toString()
            if(x.length==0)
            {
                Toast.makeText(this, "Please Enter a valid name", Toast.LENGTH_SHORT).show()
            }
            else if(saveimage.size==0){
                Toast.makeText(this,"Please add an image first",Toast.LENGTH_SHORT).show()
            }
            else {
                val abspth=MainActivity.abspath

                val file = File(abspth + "/" + x)

                if (!file.exists()) {
                    Toast.makeText(this, "User added Successfully",
                            Toast.LENGTH_LONG).show()
                 /*   Toast.makeText(this, "Restart the app to sync data",
                            Toast.LENGTH_LONG).show()*/
                    file.mkdirs()
                    Log.w("dirPAth", file.absolutePath)
                  //  Toast.makeText(this, saveimage.size.toString() + " SIZE", Toast.LENGTH_LONG).show()
                    var cc=0
                    for(image in saveimage) {
                        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
                        val fname = x + "_$timeStamp.jpg"
                     //   Toast.makeText(this, cc.toString(), Toast.LENGTH_SHORT).show()
                        cc++
                        val file1: File = File(file, fname)
                        try {
                            val out = FileOutputStream(file1)
                            image.compress(Bitmap.CompressFormat.JPEG, 100, out)
                            out.flush()
                            out.close()
                            Log.w("Image", file1.absolutePath)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    saveimage.clear()
                     /*   var intent= Intent(this, MainActivity::class.java)
                        startActivity(intent)*/
                        setResult(Activity.RESULT_OK)
                        finish()
                }
                else
                {
                    Toast.makeText(this, "User already exists.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode== CAMERA_REQUEST)
        {
            if(resultCode== Activity.RESULT_OK)
            {
                var imagee= data?.extras?.get("data")as Bitmap
              //  Toast.makeText(this,"Inside",Toast.LENGTH_SHORT).show()
                img.setImageBitmap(imagee)
                saveimage.add(imagee)
            }
        }
    }

  /*  fun startCam()
    {
        var cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(this))

    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider?) {
        var preview: Preview = Preview.Builder()
                .build()

        var cameraSelector: CameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build()
        var previewww = findViewById<PreviewView>(R.id.capture)
        preview.setSurfaceProvider(previewww.surfaceProvider)

        val imageFrameAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(480, 640))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
        if (cameraProvider != null) {
            cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, preview, imageFrameAnalysis)
        }
    }
*/
//    @SuppressLint("RestrictedApi")
   /* fun capt(){

        if(count==0)
        {
        imageCapture = ImageCapture.Builder().build()

// SETUP CAPTURE MODE
// to optimize photo capture for quality

                imageCapture = ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                            // We request aspect ratio but no resolution to match preview config, but letting
                            // CameraX optimize for whatever specific resolution best fits our use cases
                            // Set initial target rotation, we will have to call this again if rotation changes
                            // during the lifecycle of this use case
                            .setFlashMode(ImageCapture.FLASH_MODE_OFF)
                            .build();


/*
        imageCapture = ImageCapture.Builder()
                .setCaptureMode(captureMode)
                .build()


// flash will never be used when taking a picture (default)
        val flashMode = ImageCapture.FLASH_MODE_OFF

        imageCapture = ImageCapture.Builder()
                .setFlashMode(flashMode)
                .build()


 /*       val aspectRatio = Rational(1,1).toInt()

        imageCapture = ImageCapture.Builder()
                .setTargetAspectRatio(aspectRatio)
                .build()*/

// SETUP TARGET RESOLUTION
        val metrics = DisplayMetrics().also { view.display.getRealMetrics(it) }
            metrics.widthPixels=350
            metrics.heightPixels=350
        val screenSize = Size(metrics.widthPixels, metrics.heightPixels)

        imageCapture = ImageCapture.Builder()
                .setTargetResolution(screenSize)
                .setTargetName("CameraConference")
                .build()*/


      /*  val orientationEventListener = object : OrientationEventListener(this as Context) {
            override fun onOrientationChanged(orientation: Int) {

                }

                // default => Display.getRotation()
         //       imageCapture.targetRotation = rotation
            }
        }
        orientationEventListener.enable()*/

      //  Toast.makeText(this, "Capture1", Toast.LENGTH_SHORT).show()


            val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> = ProcessCameraProvider.getInstance(this)
            val processCameraProvider = cameraProviderFuture.get()
            processCameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_FRONT_CAMERA, imageCapture)
            count=1
        }

        imageCapture.takePicture(CameraXExecutors.mainThreadExecutor(), object : ImageCapture.OnImageCapturedCallback() {
            @SuppressLint("UnsafeOptInUsageError")
            override fun onCaptureSuccess(image: ImageProxy) {
                //    Toast.makeText(this@AddPerson, "Capture2", Toast.LENGTH_SHORT).show()
                super.onCaptureSuccess(image)

                var src = image.convertImageProxyToBitmap()

                //    BM.rotateBitmap(src,90f)
                img.setImageBitmap(src)
                saveimage.add(src)
                val dimensionInPixel = 350.0f
                val dimensionInDp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dimensionInPixel, resources.displayMetrics).toInt()
                img.layoutParams.height = dimensionInDp
                img.layoutParams.width = dimensionInDp
                img.requestLayout()
                //        Toast.makeText(this@AddPerson, src.toString(), Toast.LENGTH_SHORT).show()
                changeDim(0f)

                if (image != null) {
                    image.close()
                }
            }

            override fun onError(exception: ImageCaptureException) {
                val errorType = exception.getImageCaptureError()
                Log.w("Error", exception.toString())
                //    Toast.makeText(this@AddPerson, errorType.toString(), Toast.LENGTH_SHORT).show()
            }
        })

        //      Toast.makeText(this, "Capture", Toast.LENGTH_SHORT).show()
    }
*/
   /* fun ImageProxy.convertImageProxyToBitmap(): Bitmap {
        val buffer = planes[0].buffer
        buffer.rewind()
        val bytes = ByteArray(buffer.capacity())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }*/

/*    fun changeDim(dimensionInPixel: Float)
    {
        val dimensionInDp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dimensionInPixel, resources.displayMetrics).toInt()
        view.layoutParams.height = dimensionInDp
        view.layoutParams.width = dimensionInDp
        view.requestLayout()

        if(dimensionInDp==0)
        {
            cpt.layoutParams.height = dimensionInDp
            cpt.layoutParams.width = dimensionInDp
            cpt.requestLayout()

            val dimensionInDp2 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60f, resources.displayMetrics).toInt()
            val dimensionInDp3 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120f, resources.displayMetrics).toInt()

            retake.layoutParams.height = dimensionInDp2
            retake.layoutParams.width = dimensionInDp3
            retake.requestLayout()

            confirm.layoutParams.height = dimensionInDp2
            confirm.layoutParams.width = dimensionInDp3
            confirm.requestLayout()
        }
        else
        {
            cpt.layoutParams.height = 72
            cpt.layoutParams.width = 72
            cpt.requestLayout()
        }
    }*/

  /*  fun reset()
    {
        val dimensionInDp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 350f, resources.displayMetrics).toInt()
        view.layoutParams.height = dimensionInDp
        view.layoutParams.width = dimensionInDp
        view.requestLayout()

        val dimensionInDp2 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0f, resources.displayMetrics).toInt()
        img.layoutParams.height = dimensionInDp2
        img.layoutParams.width = dimensionInDp2
        img.requestLayout()
        retake.layoutParams.height = dimensionInDp2
        retake.layoutParams.width = dimensionInDp2
        retake.requestLayout()

        confirm.layoutParams.height = dimensionInDp2
        confirm.layoutParams.width = dimensionInDp2
        confirm.requestLayout()

        val dimensionInDp3 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 70f, resources.displayMetrics).toInt()
        cpt.layoutParams.height = dimensionInDp3
        cpt.layoutParams.width = dimensionInDp3
        cpt.requestLayout()




    }*/
}
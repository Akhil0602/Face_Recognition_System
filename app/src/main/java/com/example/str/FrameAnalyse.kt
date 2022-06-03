@file:Suppress("UnusedImport")

package com.example.str

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.pow
import kotlin.math.sqrt


class FrameAnalyse(
        private var context: Context,
        private var boundingBoxOverlay: OverlayBox,
        private var model: FaceModel
): ImageAnalysis.Analyzer {

    lateinit var bestScoreUserName:String

    private val realTimeOpts = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .build()
    private val detector = FaceDetection.getClient(realTimeOpts)

    private val nameScoreHashmap = HashMap<String, ArrayList<Float>>()
    private var subject = FloatArray(model.embeddingDim)

    private var isProcessing = false

    private val metricToBeUsed = "l2"


    // Store the face embeddings
    // String -> name of the person and FloatArray -> Embedding of the face.
    var faceList = ArrayList<Pair<String, FloatArray>>()


    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(image: ImageProxy) {
        //Log.w("Faces",faceList.size.toString())
        if (isProcessing || faceList.size == 0) {
            image.close()
            return
        } else {
            isProcessing = true
         //   Log.w("fun","analyse")
            val frameBitmap = BM.imageToBitmap(image.image!!, image.imageInfo.rotationDegrees)

            val inputImage = InputImage.fromMediaImage(image.image, image.imageInfo.rotationDegrees)
            detector.process(inputImage)
                .addOnSuccessListener { faces ->
                    CoroutineScope(Dispatchers.Default).launch {
               //         ma.getText()
                        runModel(faces, frameBitmap)

                    }
                }
                .addOnCompleteListener {
                    image.close()
                }
        }
    }


    private suspend fun runModel(faces: List<Face>, cameraFrameBitmap: Bitmap){
        withContext(Dispatchers.Default) {
           // Log.w("run","Model")
            if(faces.size==0)
            {
                Handler(Looper.getMainLooper()).post(Runnable { //do stuff like remove view etc
                    SetText.set("No Face Found")
                })
            }
            for (face in faces) {
        //        Log.w("1", "000")
                try {
                    val croppedBitmap = BM.cropRectFromBitmap(cameraFrameBitmap, face.boundingBox)
                    subject = model.getFaceEmbedding(croppedBitmap)
                 //   Log.w("1", "-1")

                        for ( i in 0 until faceList.size ) {
                        //    Log.w("1", "0")
                            if ( nameScoreHashmap[faceList[i].first] == null ) {
                              //  Log.w("1", "1")
                                // Compute the L2 norm and then append it to the ArrayList.
                                val p = ArrayList<Float>()
                                if ( metricToBeUsed == "cosine" ) {
                                    p.add(cosineSimilarity(subject, faceList[i].second))
                                }
                                else {
                                    p.add(L2Norm(subject, faceList[i].second))
                                }
                                nameScoreHashmap[faceList[i].first] = p
                            }
                            // If this cluster exists, append the L2 norm/cosine score to it.
                            else {
                              //  Log.w("1", "2")
                                if ( metricToBeUsed == "cosine" ) {
                                    nameScoreHashmap[faceList[i].first]?.add(
                                            cosineSimilarity(
                                                    subject,
                                                    faceList[i].second
                                            )
                                    )
                                }
                                else {
                                   // Log.w("1", "3")
                                    nameScoreHashmap[faceList[i].first]?.add(
                                            L2Norm(
                                                    subject,
                                                    faceList[i].second
                                            )
                                    )
                                }
                            }
                        }

                        // Compute the average of all scores norms for each cluster.
                        val avgScores = nameScoreHashmap.values.map{ scores -> scores.toFloatArray().average() }
//                        Logger.log( "Average score for each user : $nameScoreHashmap" )

                        val names = nameScoreHashmap.keys.toTypedArray()
                        nameScoreHashmap.clear()

                        // Calculate the minimum L2 distance from the stored average L2 norms.
                          bestScoreUserName = if ( metricToBeUsed == "cosine" ) {
                            // In case of cosine similarity, choose the highest value.
                            if ( avgScores.maxOrNull()!! > model.model.cosineThreshold ) {
                                names[avgScores.indexOf(avgScores.maxOrNull()!!)]
                            }
                            else {
                                "Unknown"
                            }
                        } else {
                            // In case of L2 norm, choose the lowest value.
                            if ( avgScores.minOrNull()!! > model.model.l2Threshold ) {
                                "Unknown"
                            }
                            else {
                                names[avgScores.indexOf(avgScores.minOrNull()!!)]
                            }
                        }
                        Log.w("User", bestScoreUserName)
                        SetText.set(bestScoreUserName)

                }
                catch (e: Exception) {
                    // If any exception occurs with this box and continue with the next boxes.
                    Log.w("Model", "Exception in FrameAnalyser : ${e.message}")
                    continue
                }
            }
            withContext(Dispatchers.Main) {
                isProcessing = false
            }

        }
    }




    // Compute the L2 norm of ( x2 - x1 )
    fun L2Norm(x1: FloatArray, x2: FloatArray) : Float {
        return sqrt(x1.mapIndexed { i, xi -> (xi - x2[i]).pow(2) }.sum())
    }


    // Compute the cosine of the angle between x1 and x2.
    fun cosineSimilarity(x1: FloatArray, x2: FloatArray) : Float {
        val mag1 = sqrt(x1.map { it * it }.sum())
        val mag2 = sqrt(x2.map { it * it }.sum())
        val dot = x1.mapIndexed{ i, xi -> xi * x2[i] }.sum()
        return dot / (mag1 * mag2)
    }
}
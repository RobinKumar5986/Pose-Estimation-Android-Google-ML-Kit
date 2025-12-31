package com.evobi.posefindernative

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import java.nio.Buffer
import java.nio.ByteBuffer

class MainActivity : AppCompatActivity() {

    //UI Components
    lateinit var defaultTextView: TextView


    // Base pose detector with streaming frames, when depending on the pose-detection sdk
    val optionsBase = PoseDetectorOptions.Builder()
        .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
        .build()

    // Accurate pose detector on static images, when depending on the pose-detection-accurate sdk
    val optionsAccurate = AccuratePoseDetectorOptions.Builder()
        .setDetectorMode(AccuratePoseDetectorOptions.SINGLE_IMAGE_MODE)
        .build()
    val poseDetector = PoseDetection.getClient(optionsAccurate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        UiLinker()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        startPoseEstimation()
    }
    private fun UiLinker(){
        defaultTextView = findViewById<TextView>(R.id.helloWorld)
    }
    private fun startPoseEstimation(){
        val bitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.person)
        val image = InputImage.fromBitmap(bitmap, 0)

        getPoseEstimation(image)
    }
    private fun imageFromBuffer(byteBuffer: ByteBuffer, rotationDegrees: Int) {
        val image = InputImage.fromByteBuffer(
            byteBuffer,
            /* image width */ 480,
            /* image height */ 360,
            rotationDegrees,
            InputImage.IMAGE_FORMAT_NV21 // or IMAGE_FORMAT_YV12
        )
        getPoseEstimation(image)
    }
    private fun getPoseEstimation(image: InputImage) {
        poseDetector.process(image)
            .addOnSuccessListener { pose: Pose ->
                logAllPoseLandmarks(pose)
            }
            .addOnFailureListener { e: Exception ->
                Log.e("Pose Error",e.message.toString())
            }
    }
    override fun onDestroy() {
        super.onDestroy()
        poseDetector.close()
    }

    private fun logAllPoseLandmarks(pose: Pose) {
        val allLandmarks = pose.allPoseLandmarks

        if (allLandmarks.isEmpty()) {
            Log.d("PoseDetection", "No person/pose detected in the image.")
            return
        }

        Log.d("PoseDetection", "Pose detected! Found ${allLandmarks.size} landmarks:\n")

        for (landmark in allLandmarks) {
            val type = when (landmark.landmarkType) {
                PoseLandmark.NOSE -> "NOSE"
                PoseLandmark.LEFT_EYE_INNER -> "LEFT_EYE_INNER"
                PoseLandmark.LEFT_EYE -> "LEFT_EYE"
                PoseLandmark.LEFT_EYE_OUTER -> "LEFT_EYE_OUTER"
                PoseLandmark.RIGHT_EYE_INNER -> "RIGHT_EYE_INNER"
                PoseLandmark.RIGHT_EYE -> "RIGHT_EYE"
                PoseLandmark.RIGHT_EYE_OUTER -> "RIGHT_EYE_OUTER"
                PoseLandmark.LEFT_EAR -> "LEFT_EAR"
                PoseLandmark.RIGHT_EAR -> "RIGHT_EAR"
                PoseLandmark.LEFT_MOUTH -> "LEFT_MOUTH"
                PoseLandmark.RIGHT_MOUTH -> "RIGHT_MOUTH"
                PoseLandmark.LEFT_SHOULDER -> "LEFT_SHOULDER"
                PoseLandmark.RIGHT_SHOULDER -> "RIGHT_SHOULDER"
                PoseLandmark.LEFT_ELBOW -> "LEFT_ELBOW"
                PoseLandmark.RIGHT_ELBOW -> "RIGHT_ELBOW"
                PoseLandmark.LEFT_WRIST -> "LEFT_WRIST"
                PoseLandmark.RIGHT_WRIST -> "RIGHT_WRIST"
                PoseLandmark.LEFT_PINKY -> "LEFT_PINKY"
                PoseLandmark.RIGHT_PINKY -> "RIGHT_PINKY"
                PoseLandmark.LEFT_INDEX -> "LEFT_INDEX"
                PoseLandmark.RIGHT_INDEX -> "RIGHT_INDEX"
                PoseLandmark.LEFT_THUMB -> "LEFT_THUMB"
                PoseLandmark.RIGHT_THUMB -> "RIGHT_THUMB"
                PoseLandmark.LEFT_HIP -> "LEFT_HIP"
                PoseLandmark.RIGHT_HIP -> "RIGHT_HIP"
                PoseLandmark.LEFT_KNEE -> "LEFT_KNEE"
                PoseLandmark.RIGHT_KNEE -> "RIGHT_KNEE"
                PoseLandmark.LEFT_ANKLE -> "LEFT_ANKLE"
                PoseLandmark.RIGHT_ANKLE -> "RIGHT_ANKLE"
                PoseLandmark.LEFT_HEEL -> "LEFT_HEEL"
                PoseLandmark.RIGHT_HEEL -> "RIGHT_HEEL"
                PoseLandmark.LEFT_FOOT_INDEX -> "LEFT_FOOT_INDEX"
                PoseLandmark.RIGHT_FOOT_INDEX -> "RIGHT_FOOT_INDEX"
                else -> "UNKNOWN"
            }

            val position = landmark.position
            val position3D = landmark.position3D
            val inFrameLikelihood = landmark.inFrameLikelihood

            Log.d(
                "PoseDetection",
                "$type:\n" +
                        "  - 2D Position: x = ${position.x}, y = ${position.y}\n" +
                        "  - 3D Position: x = ${position3D.x}, y = ${position3D.y}, z = ${position3D.z}\n" +
                        "  - InFrameLikelihood: $inFrameLikelihood\n"
            )
        }
    }
}
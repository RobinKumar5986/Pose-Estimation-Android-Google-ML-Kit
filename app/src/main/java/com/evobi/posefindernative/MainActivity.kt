package com.evobi.posefindernative

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    // UI Components
    private lateinit var poseImage: ImageView
    private lateinit var btnFlip: ImageButton
    private lateinit var cameraManager: CameraManager
    private  lateinit var poseEstimationManager: PoseEstimationManager
    private val mainHandler = Handler(Looper.getMainLooper())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        uiLinker()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupPoseEstimation()
        setupCamera()
    }

    private fun uiLinker() {
        poseImage = findViewById(R.id.imgPoseEstimation)
        btnFlip = findViewById(R.id.btnFlipCamera)

        btnFlip.setOnClickListener {
            cameraManager.flipCamera()
        }
    }

    private fun setupPoseEstimation() {
        poseEstimationManager = PoseEstimationManager { resultBitmap ->
            mainHandler.post {
                poseImage.setImageBitmap(resultBitmap)
            }
        }
    }

    private fun setupCamera() {
        cameraManager = CameraManager(this, this) { imageProxy ->
            var bitmap = imageProxy.toBitmap()
            if(cameraManager.getCameraState() == CameraManager.CameraState.FRONT_CAMERA){
                bitmap = cameraManager.rotateBitmap(bitmap,-90f)
                bitmap = cameraManager.mirrorBitmap(bitmap)
            }else{
                bitmap = cameraManager.rotateBitmap(bitmap,90f)
            }

            mainHandler.post {
                poseEstimationManager.startPosePredication(bitmap)
//                poseImage.setImageBitmap(bitmap)
            }
            imageProxy.close()
        }

        if (cameraManager.checkPermissions()) {
            cameraManager.startCamera()
        } else {
            cameraManager.requestPermissions(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        poseEstimationManager.close()
        cameraManager.shutdown()
    }
}
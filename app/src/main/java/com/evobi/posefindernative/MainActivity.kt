package com.evobi.posefindernative

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    // UI Components
    private lateinit var poseImage: ImageView
    private lateinit var btnFlip: ImageButton
    private lateinit var btnBluetooth: ImageButton

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
        btnBluetooth = findViewById(R.id.btnBluetooth)
        buttonClickHandler()
    }

    private fun buttonClickHandler() {
        btnFlip.setOnClickListener {
            cameraManager.flipCamera()
        }

        btnBluetooth.setOnClickListener {
            Toast.makeText(this, "Bluetooth connecting...", Toast.LENGTH_SHORT).show()
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
            }
            imageProxy.close()
        }

        if (cameraManager.checkPermissions()) {
            cameraManager.startCamera()
        } else {
            cameraManager.requestPermissions(this)
        }
    }

    /**
     * This triggers as soon as the user responds to the permission dialog
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Assuming CameraManager uses a standard request code, or checking if result is granted
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Camera Permission Granted", Toast.LENGTH_SHORT).show()
            cameraManager.startCamera()
        } else {
            Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        poseEstimationManager.close()
        cameraManager.shutdown()
    }
}
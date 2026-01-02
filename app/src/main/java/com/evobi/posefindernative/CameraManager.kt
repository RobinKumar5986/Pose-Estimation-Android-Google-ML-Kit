package com.evobi.posefindernative

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraManager(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val onFrameAvailable: (ImageProxy) -> Unit
) {
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    private var targetFps = 24L //FPS
    private var lastAnalysisTimestamp = 0L

    companion object {
        const val CAMERA_PERMISSION_REQUEST_CODE = 101
    }
    /**
     * Starts the camera. Call this after permissions are granted.
     */
    fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCameraUseCases()
        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * Flips between front and back camera
     */
    fun flipCamera() {
        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
        bindCameraUseCases()
    }

    /**
     * Binds the ImageAnalysis use case with portrait orientation
     */
    private fun bindCameraUseCases() {
        val cameraProvider = this.cameraProvider ?: return

        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
            val currentTime = System.currentTimeMillis()
            val intervalInMs = 1000L / targetFps

            if (currentTime - lastAnalysisTimestamp >= intervalInMs) {
                lastAnalysisTimestamp = currentTime

                onFrameAvailable(imageProxy)
            } else {
                imageProxy.close()
            }
        }

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, imageAnalysis)
        } catch (exc: Exception) {
            Log.e("PoseFinder", "Use case binding failed", exc)
        }
    }

    /**
     * Check if CAMERA permission is granted
     */
    fun checkPermissions(): Boolean =
        ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED

    /**
     * Request CAMERA permission
     */
    fun requestPermissions(activity: androidx.activity.ComponentActivity) {
        androidx.core.app.ActivityCompat.requestPermissions(
            activity,
            arrayOf(android.Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    /**
     * Clean up resources (call from onDestroy())
     */
    fun shutdown() {
        cameraExecutor.shutdown()
        cameraProvider?.unbindAll()
        Log.d("PoseFinder", "CameraManager shutdown")
    }

    /**
     * Sets the desired frames per second for analysis
     */
    fun setTargetFps(fps: Int) {
        targetFps = fps.toLong()
    }

    // Helper function to rotate the bitmap
    fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degrees)
        return Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
        )
    }

    fun mirrorBitmap(bitmap: Bitmap): Bitmap {
        val matrix = Matrix()
        matrix.preScale(-1f, 1f)  // Negative X-scale flips horizontally
        return Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
        )
    }
    fun getCameraState():CameraState{
        if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            return CameraState.BACK_CAMERA
        }else{
            return CameraState.FRONT_CAMERA
        }
    }

    enum class CameraState{
        FRONT_CAMERA,
        BACK_CAMERA
    }
}
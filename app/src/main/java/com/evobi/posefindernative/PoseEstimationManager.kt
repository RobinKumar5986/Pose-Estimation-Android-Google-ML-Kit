package com.evobi.posefindernative

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import kotlin.math.abs
import kotlin.math.atan2

class PoseEstimationManager(
    private val onPoseBitmapReady: (Bitmap) -> Unit
) {
    val options = PoseDetectorOptions.Builder()
        .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
        .build()
    val poseDetector = PoseDetection.getClient(options)

    // Paints
    val linePaint = Paint().apply { color = Color.WHITE; strokeWidth = 2f; isAntiAlias = true }
    val dotPaint = Paint().apply { color = Color.CYAN; style = Paint.Style.FILL; isAntiAlias = true }
    val textPaint = Paint().apply {
        color = Color.YELLOW; textSize = 10f; isAntiAlias = true
        setShadowLayer(5f, 0f, 0f, Color.BLACK)
    }
    val anglePointPadding  = 10f
    fun startPosePredication(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        poseDetector.process(image)
            .addOnSuccessListener { pose ->
                LogHelper.logAllPoseData(pose)
                onPoseBitmapReady(drawPoseOnBitmap(bitmap,pose))
            }
            .addOnFailureListener { e ->
                 Log.e("PoseFinder",e.message.toString())
            }
    }
    /**
     * @Info: this function is use for mapping the pose position to the image and then return the bit map of updated image
     * */
    private fun drawPoseOnBitmap(originalBitmap: Bitmap, pose: Pose): Bitmap {
        val resultBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(resultBitmap)



        // Upper Body
        drawLine(canvas, pose, PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER, linePaint)
        drawLine(canvas, pose, PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_ELBOW, linePaint)
        drawLine(canvas, pose, PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_WRIST, linePaint)
        drawLine(canvas, pose, PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_ELBOW, linePaint)
        drawLine(canvas, pose, PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_WRIST, linePaint)

        // Torso & Legs
        drawLine(canvas, pose, PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_HIP, linePaint)
        drawLine(canvas, pose, PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_HIP, linePaint)
        drawLine(canvas, pose, PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP, linePaint)
        drawLine(canvas, pose, PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_KNEE, linePaint)
        drawLine(canvas, pose, PoseLandmark.LEFT_KNEE, PoseLandmark.LEFT_ANKLE, linePaint)
        drawLine(canvas, pose, PoseLandmark.RIGHT_HIP, PoseLandmark.RIGHT_KNEE, linePaint)
        drawLine(canvas, pose, PoseLandmark.RIGHT_KNEE, PoseLandmark.RIGHT_ANKLE, linePaint)


        // Shoulder (Elbow, Shoulder, Hip)
        drawJointAngle(canvas, pose, PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_HIP, textPaint)
        drawJointAngle(canvas, pose, PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_HIP, textPaint)

        // Elbow (Wrist, Elbow, Shoulder)
        drawJointAngle(canvas, pose, PoseLandmark.LEFT_WRIST, PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_SHOULDER, textPaint)
        drawJointAngle(canvas, pose, PoseLandmark.RIGHT_WRIST, PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_SHOULDER, textPaint)

        // Hip (Knee, Hip, Shoulder)
        drawJointAngle(canvas, pose, PoseLandmark.LEFT_KNEE, PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_SHOULDER, textPaint)
        drawJointAngle(canvas, pose, PoseLandmark.RIGHT_KNEE, PoseLandmark.RIGHT_HIP, PoseLandmark.RIGHT_SHOULDER, textPaint)

        // Knee (Ankle, Knee, Hip)
        drawJointAngle(canvas, pose, PoseLandmark.LEFT_ANKLE, PoseLandmark.LEFT_KNEE, PoseLandmark.LEFT_HIP, textPaint)
        drawJointAngle(canvas, pose, PoseLandmark.RIGHT_ANKLE, PoseLandmark.RIGHT_KNEE, PoseLandmark.RIGHT_HIP, textPaint)

        for (landmark in pose.allPoseLandmarks) {
            canvas.drawCircle(landmark.position.x, landmark.position.y, 3.5f, dotPaint)
        }

        return resultBitmap
    }

    fun getAngle(first: PoseLandmark, mid: PoseLandmark, last: PoseLandmark): Double {
        val angle1 = atan2(first.position.y - mid.position.y, first.position.x - mid.position.x)
        val angle2 = atan2(last.position.y - mid.position.y, last.position.x - mid.position.x)

        var result = Math.toDegrees((angle2 - angle1).toDouble())

        // Normalize to 0-360 range
        if (result < 0) {
            result += 360.0
        }

        return result
    }
    fun drawJointAngle(canvas: Canvas, pose: Pose, p1: Int, p2: Int, p3: Int, paint: Paint) {
        val start = pose.getPoseLandmark(p1)
        val mid = pose.getPoseLandmark(p2)
        val end = pose.getPoseLandmark(p3)

        if (start != null && mid != null && end != null) {
            val angle = getAngle(start, mid, end)
            canvas.drawText("${angle.toInt()}°", mid.position.x, mid.position.y - anglePointPadding, paint)
        }
    }
    fun drawLine(canvas: Canvas, pose: Pose, startType: Int, endType: Int, paint: Paint) {
        val start = pose.getPoseLandmark(startType)
        val end = pose.getPoseLandmark(endType)
        if (start != null && end != null) {
            canvas.drawLine(start.position.x, start.position.y, end.position.x, end.position.y, paint)
        }
    }
    /**
     * Close the detector when done (call from Activity onDestroy)
     */
    fun close() {
        poseDetector.close()
    }
}
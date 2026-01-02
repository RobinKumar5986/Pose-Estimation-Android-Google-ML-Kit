package com.evobi.posefindernative

import android.util.Log
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark

object LogHelper {
    /**
     * Logs all 33 pose landmarks with their type, position, z-depth, and confidence
     */
    fun logAllPoseData(pose: Pose) {
        Log.d("PoseFinder", "=== Pose Detection Results ===")
        Log.d("PoseFinder", "All landmarks detected: ${pose.allPoseLandmarks.size}")

        // List of all landmark types for readable names
        val landmarkTypes = mapOf(
            PoseLandmark.NOSE to "NOSE",
            PoseLandmark.LEFT_EYE_INNER to "LEFT_EYE_INNER",
            PoseLandmark.LEFT_EYE to "LEFT_EYE",
            PoseLandmark.LEFT_EYE_OUTER to "LEFT_EYE_OUTER",
            PoseLandmark.RIGHT_EYE_INNER to "RIGHT_EYE_INNER",
            PoseLandmark.RIGHT_EYE to "RIGHT_EYE",
            PoseLandmark.RIGHT_EYE_OUTER to "RIGHT_EYE_OUTER",
            PoseLandmark.LEFT_EAR to "LEFT_EAR",
            PoseLandmark.RIGHT_EAR to "RIGHT_EAR",
            PoseLandmark.LEFT_MOUTH to "LEFT_MOUTH",
            PoseLandmark.RIGHT_MOUTH to "RIGHT_MOUTH",
            PoseLandmark.LEFT_SHOULDER to "LEFT_SHOULDER",
            PoseLandmark.RIGHT_SHOULDER to "RIGHT_SHOULDER",
            PoseLandmark.LEFT_ELBOW to "LEFT_ELBOW",
            PoseLandmark.RIGHT_ELBOW to "RIGHT_ELBOW",
            PoseLandmark.LEFT_WRIST to "LEFT_WRIST",
            PoseLandmark.RIGHT_WRIST to "RIGHT_WRIST",
            PoseLandmark.LEFT_PINKY to "LEFT_PINKY",
            PoseLandmark.RIGHT_PINKY to "RIGHT_PINKY",
            PoseLandmark.LEFT_INDEX to "LEFT_INDEX",
            PoseLandmark.RIGHT_INDEX to "RIGHT_INDEX",
            PoseLandmark.LEFT_THUMB to "LEFT_THUMB",
            PoseLandmark.RIGHT_THUMB to "RIGHT_THUMB",
            PoseLandmark.LEFT_HIP to "LEFT_HIP",
            PoseLandmark.RIGHT_HIP to "RIGHT_HIP",
            PoseLandmark.LEFT_KNEE to "LEFT_KNEE",
            PoseLandmark.RIGHT_KNEE to "RIGHT_KNEE",
            PoseLandmark.LEFT_ANKLE to "LEFT_ANKLE",
            PoseLandmark.RIGHT_ANKLE to "RIGHT_ANKLE",
            PoseLandmark.LEFT_HEEL to "LEFT_HEEL",
            PoseLandmark.RIGHT_HEEL to "RIGHT_HEEL",
            PoseLandmark.LEFT_FOOT_INDEX to "LEFT_FOOT_INDEX",
            PoseLandmark.RIGHT_FOOT_INDEX to "RIGHT_FOOT_INDEX"
        )

        for (landmark in pose.allPoseLandmarks) {
            val typeName = landmarkTypes[landmark.landmarkType] ?: "UNKNOWN_${landmark.landmarkType}"
            val position = landmark.position
            val position3D = landmark.position3D
            val likelihood = landmark.inFrameLikelihood

            Log.d(
                "PoseFinder",
                String.format(
                    "%-20s | x: %.1f  y: %.1f  z: %.2f  | confidence: %.3f",
                    typeName,
                    position.x,
                    position.y,
                    position3D.z,
                    likelihood
                )
            )
        }

        Log.d("PoseFinder", "=== End of Pose Data ===\n")
    }
}
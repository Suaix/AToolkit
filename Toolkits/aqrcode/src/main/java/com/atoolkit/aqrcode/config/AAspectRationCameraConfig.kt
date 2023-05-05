package com.atoolkit.aqrcode.config

import androidx.camera.core.ImageAnalysis
import com.atoolkit.aqrcode.ASPECT_RATIO_16_9
import com.atoolkit.aqrcode.ASPECT_RATIO_4_3
import com.atoolkit.aqrcode.application
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


/**
 * Author:summer
 * Time: 2023/5/4 19:22
 * Description: AAspectRationCameraConfig是
 */
class AAspectRationCameraConfig : ICameraConfig {

    private val mAspectRatio: Float

    init {
        val dm = application.resources.displayMetrics
        val width = dm.widthPixels
        val height = dm.heightPixels
        val ratio = max(width, height) / min(width, height)
        mAspectRatio = if (abs(ratio - ASPECT_RATIO_4_3) < abs(ratio - ASPECT_RATIO_16_9)) {
            ASPECT_RATIO_4_3
        } else {
            ASPECT_RATIO_16_9
        }
    }

    override fun optionImageAnalysis(builder: ImageAnalysis.Builder): ImageAnalysis {
        builder.setTargetAspectRatio(mAspectRatio.toInt())
        return builder.build()
    }
}
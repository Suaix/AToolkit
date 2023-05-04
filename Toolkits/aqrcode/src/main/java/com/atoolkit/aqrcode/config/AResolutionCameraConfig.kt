package com.atoolkit.aqrcode.config

import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import com.atoolkit.aqrcode.ASPECT_RATIO_16_9
import com.atoolkit.aqrcode.ASPECT_RATIO_4_3
import com.atoolkit.aqrcode.IMAGE_QUALITY_1080P
import com.atoolkit.aqrcode.TAG
import com.atoolkit.aqrcode.aLog
import com.atoolkit.aqrcode.application
import kotlin.math.abs
import kotlin.math.round


/**
 * Author:summer
 * Time: 2023/5/4 17:50
 * Description: AResolutionCameraConfig是适配手机屏幕分辨率的相机配置，尽量接近屏幕尺寸
 */
class AResolutionCameraConfig(private val quality: Int = IMAGE_QUALITY_1080P) : ICameraConfig {

    private var mTargetSize: Size

    init {
        val dm = application.resources.displayMetrics
        val width = dm.widthPixels
        val height = dm.heightPixels
        aLog?.v(TAG, "AResolutionCameraConfig: width=$width, height=$height")
        val ration: Float
        val size: Int
        if (width < height) {
            ration = height * 1.0f / width
            size = width.coerceAtMost(quality)
            mTargetSize = if (abs(ration - ASPECT_RATIO_4_3) < abs(ration - ASPECT_RATIO_16_9)) {
                Size(size, round(size * ASPECT_RATIO_4_3).toInt())
            } else {
                Size(size, round(size * ASPECT_RATIO_16_9).toInt())
            }
        } else {
            ration = width * 1.0f / height
            size = height.coerceAtMost(quality)
            mTargetSize = if (abs(ration - ASPECT_RATIO_4_3) < abs(ration - ASPECT_RATIO_16_9)) {
                Size(round(size * ASPECT_RATIO_4_3).toInt(), size)
            } else {
                Size(round(size * ASPECT_RATIO_16_9).toInt(), size)
            }
        }
        aLog?.v(TAG, "AResolutionCameraConfig: targetSize=$mTargetSize")
    }

    override fun optionImageAnalysis(builder: ImageAnalysis.Builder): ImageAnalysis {
        builder.setTargetResolution(mTargetSize)
        return builder.build()
    }
}
package com.atoolkit.aqrcode.config

import androidx.camera.core.ImageAnalysis


/**
 * Author:summer
 * Time: 2023/5/4 17:46
 * Description: ADefaultCameraConfig是默认的相机配置
 */
class ADefaultCameraConfig : ICameraConfig {

    override fun optionImageAnalysis(builder: ImageAnalysis.Builder): ImageAnalysis {
        return builder.build()
    }
}
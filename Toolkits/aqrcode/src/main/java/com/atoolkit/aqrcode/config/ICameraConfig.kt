package com.atoolkit.aqrcode.config

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview


/**
 * Author:summer
 * Time: 2023/5/4 17:43
 * Description: ICameraConfig是camerax的配置项
 */
interface ICameraConfig {
    /**
     * Description: 获取相机预览配置
     * Author: summer
     */
    fun optionPreview(builder: Preview.Builder): Preview {
        return builder.build()
    }

    /**
     * Description: 获取相机选择配置
     * Author: summer
     */
    fun optionSelector(builder: CameraSelector.Builder): CameraSelector {
        return builder.build()
    }

    /**
     * Description: 获取图片分析配置
     * Author: summer
     */
    fun optionImageAnalysis(builder: ImageAnalysis.Builder): ImageAnalysis
}
package com.atoolkit.aqrcode.config

import android.graphics.Rect
import com.google.zxing.DecodeHintType


/**
 * Author:summer
 * Time: 2023/5/5 14:50
 * Description: AScanDecodeConfig是扫描解码的配置类
 */
data class AScanDecodeConfig(
    val hints: Map<DecodeHintType, Any>,
    val isMultiDecode: Boolean = true,
    val isSupportReverseCode: Boolean = false,
    val isSupportReverseCodeMulti: Boolean = false,
    val isSupportVerticalCode: Boolean = false,
    val isSupportVerticalCodeMulti: Boolean = false,
    val analyzeAreaRect: Rect? = null,
    val isFullAreaScan: Boolean = false,
    val areaRectRation: Float = 0.8f,
    val areaRectVerticalOffset: Int = 0,
    val areaRectHorizontalOffset: Int = 0
)
package com.atoolkit.aqrcode.config

import android.graphics.Rect
import com.google.zxing.DecodeHintType


/**
 * Author:summer
 * Time: 2023/5/5 14:50
 * Description: AScanDecodeConfig是扫描解码的配置类
 */
data class AScanDecodeConfig(
    // 解码配置，参考[ADecodeFormat]，其中列出了常用的解码配置；
    val hints: Map<DecodeHintType, Any>,
    // 是否支持多解码模式
    val isMultiDecode: Boolean = true,
    // 是否支持黑白反转码解码
    val isSupportReverseCode: Boolean = false,
    // 是否支持黑白分转码多模式解码
    val isSupportReverseCodeMulti: Boolean = false,
    // 是否支持竖向码解码
    val isSupportVerticalCode: Boolean = false,
    // 是否支持竖向码多模式解码
    val isSupportVerticalCodeMulti: Boolean = false,
    /**
     * isFullAreaScan、analyzeAreaRect、areaRectRation互斥，优先判断isFullAreaScan，其次判断analyzeAreaRect，最后使用areaRectRation
     */
    // 是否是全屏扫码，全屏扫码下会分析整个区域的图片
    val isFullAreaScan: Boolean = false,
    // 分析码的区域范围
    val analyzeAreaRect: Rect? = null,
    // 识别区域的有效比例，默认是80%
    val areaRectRation: Float = 0.8f,
    // 识别区域的竖向偏移量
    val areaRectVerticalOffset: Int = 0,
    // 识别区域的横向偏移量
    val areaRectHorizontalOffset: Int = 0
)
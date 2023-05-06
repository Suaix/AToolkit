package com.atoolkit.aqrcode.analyer

import android.content.res.Configuration
import android.graphics.ImageFormat
import android.graphics.Rect
import androidx.camera.core.ImageProxy
import com.atoolkit.aqrcode.TAG
import com.atoolkit.aqrcode.aLog
import com.atoolkit.aqrcode.config.AScanDecodeConfig
import com.atoolkit.aqrcode.yuv420ThreePlanesToNv21
import com.google.zxing.Reader
import com.google.zxing.Result
import kotlin.math.min


/**
 * Author:summer
 * Time: 2023/5/5 11:19
 * Description: AImageAnalyzer是将图像解析成字节数组的抽象类
 */

abstract class AImageAnalyzer<T : Reader>(private val decodeConfig: AScanDecodeConfig) : IAnalyzer {

    internal val mReader: T

    init {
        mReader = createReader()
    }

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    override fun analyze(image: ImageProxy, orientation: Int): Result? {
        if (image.format != ImageFormat.YUV_420_888) {
            aLog?.w(TAG, "image format: ${image.format}")
            return null
        }
        var width = image.width
        var height = image.height
        // 将图像转化成字节数组
        val nv21Bytes = yuv420ThreePlanesToNv21(image.image?.planes, width, height)?.array() ?: return null
        val dataBytes = if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            // 是竖向，将图像数据翻转
            val rotatedData = ByteArray(nv21Bytes.size)
            for (y in 0 until height) {
                for (x in 0 until width) {
                    rotatedData[x * height + height - y - 1] = nv21Bytes[x + y * width]
                }
            }
            // 交换宽高
            val temp = width
            width = height
            height = temp
            rotatedData
        } else {
            nv21Bytes
        }
        // 获取数据解析区域
        val decodeArea = if (decodeConfig.isFullAreaScan) {
            Rect(0, 0, width, height)
        } else {
            decodeConfig.analyzeAreaRect ?: run {
                val size = (min(width, height) * decodeConfig.areaRectRation).toInt()
                val left = (width - size) / 2 + decodeConfig.areaRectHorizontalOffset
                val top = (height - size) / 2 + decodeConfig.areaRectVerticalOffset
                Rect(left, top, left + size, top + size)
            }
        }
        // 根据数据解析区域解析图像字节数组
        return analyzeArea(dataBytes, width, height, decodeArea)
    }

    /**
     * Description: 根据解析区域对数据内容进行解析
     * Author: summer
     *
     * @param data 图像字节数组
     * @param dataWidth 图像宽度
     * @param dataHeight 图像高度
     * @param decodeArea 解析区域
     *
     * @return 返回解析结果
     */
    abstract fun analyzeArea(data: ByteArray, dataWidth: Int, dataHeight: Int, decodeArea: Rect): Result?

    /**
     * Description: 创建用于读取图像字节流的读取器，用来解析二维码或条形码
     * Author: summer
     *
     * @return 图像读取器
     */
    abstract fun createReader(): T
}
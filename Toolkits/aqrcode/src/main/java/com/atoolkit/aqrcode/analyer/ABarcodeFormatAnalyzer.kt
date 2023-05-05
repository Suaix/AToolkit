package com.atoolkit.aqrcode.analyer

import android.graphics.Rect
import com.atoolkit.aqrcode.TAG
import com.atoolkit.aqrcode.aLog
import com.atoolkit.aqrcode.config.AScanDecodeConfig
import com.google.zxing.BinaryBitmap
import com.google.zxing.LuminanceSource
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.Result
import com.google.zxing.common.GlobalHistogramBinarizer
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader


/**
 * Author:summer
 * Time: 2023/5/5 15:50
 * Description: ABarcodeFormatAnalyzer是二维码和条形码格式的解析器
 */

class ABarcodeFormatAnalyzer(private val decodeConfig: AScanDecodeConfig) : AImageAnalyzer<QRCodeReader>(decodeConfig) {

    override fun analyzeArea(data: ByteArray, dataWidth: Int, dataHeight: Int, decodeArea: Rect): Result? {
        var result: Result? = null
        try {
            // 先正常解析
            val source = PlanarYUVLuminanceSource(
                data,
                dataWidth,
                dataHeight,
                decodeArea.left,
                decodeArea.top,
                decodeArea.width(),
                decodeArea.height(),
                false
            )
            result = decodeInternal(source, decodeConfig.isMultiDecode)

            // 如果正常解析不出结果，则判断是否支持竖向二维码解析，支持的话将图像数据翻转，然后再尝试解析
            if (result == null && decodeConfig.isSupportVerticalCode) {
                val rotatedData = ByteArray(data.size)
                for (y in 0 until dataHeight) {
                    for (x in 0 until dataWidth) {
                        rotatedData[x * dataHeight + dataHeight - y - 1] = data[x + y * dataWidth]
                    }
                }
                val rotatedSource = PlanarYUVLuminanceSource(
                    rotatedData,
                    dataHeight,
                    dataWidth,
                    decodeArea.top,
                    decodeArea.left,
                    decodeArea.height(),
                    decodeArea.width(),
                    false
                )
                result = decodeInternal(rotatedSource, decodeConfig.isSupportVerticalCodeMulti)
            }
            // 若竖向解析还是失败，则判断是否支持黑白反色码解码，支持的话将数据黑白转换，然后再尝试解析
            if (result == null && decodeConfig.isSupportReverseCode) {
                result = decodeInternal(source.invert(), decodeConfig.isSupportReverseCodeMulti)
            }
        } catch (e: Exception) {
            aLog?.v(TAG, "ABarcodeFormatAnalyzer: analyze exception, msg=${e.message}")
        } finally {
            mReader.reset()
        }
        return result
    }

    override fun createReader(): QRCodeReader {
        return QRCodeReader()
    }

    /**
     * Description: 内部解析二维码数据
     * Author: summer
     *
     * @param source 图像数据源
     * @param isMultiDecode 是否支持多解码模式
     */
    private fun decodeInternal(source: LuminanceSource, isMultiDecode: Boolean): Result? {
        var result: Result? = null
        try {
            result = mReader.decode(BinaryBitmap(HybridBinarizer(source)), decodeConfig.hints)
            if (result == null && isMultiDecode) {
                result = mReader.decode(BinaryBitmap(GlobalHistogramBinarizer(source)), decodeConfig.hints)
            }
        } catch (e: Exception) {
            aLog?.v(TAG, "ABarcodeFormatAnalyzer: decode internal exception, msg=${e.message}")
        }
        return result
    }
}
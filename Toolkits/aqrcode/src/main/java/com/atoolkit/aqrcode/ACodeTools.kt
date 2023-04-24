package com.atoolkit.aqrcode

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/**
 * Description: 根据内容创建二维码
 * Author: summer
 *
 * @param content 二维码内容
 * @param size 二维码的大小（作为二维码的宽高），默认是500，单位是px
 * @param color 二维码的色值，默认为黑色
 * @param safeMargin 二维码边缘安全区域，默认是1，单位px
 * @param centerLogo 二维码中间的logo，默认为null
 * @param logoRatio logo相对与二维码大小的缩放比例，取值(0, 1.0)之间，只有在centerLogo不为null的情况下有效
 *
 * @return 二维码Bitmap，如果创建时出现了异常则会返回null
 */
@JvmOverloads
fun createQRCode(
    content: String,
    size: Int = 500,
    @ColorInt color: Int = Color.BLACK,
    safeMargin: Int = 1,
    centerLogo: Bitmap? = null,
    @FloatRange(from = 0.0, to = 1.0) logoRatio: Float = 0.2F,
): Bitmap? {
    val hints = mapOf(
        EncodeHintType.CHARACTER_SET to "utf-8",
        EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
        EncodeHintType.MARGIN to safeMargin
    )
    try {
        val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
        val pixels = IntArray(size * size)
        for (i in 0 until size) {
            for (j in 0 until size) {
                pixels[i + j * size] = if (bitMatrix.get(i, j)) color else Color.WHITE
            }
        }
        val qrBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        qrBitmap.setPixels(pixels, 0, size, 0, 0, size, size)
        centerLogo?.let { logo ->
            addLogo(qrBitmap, logo, logoRatio)
        }
        return qrBitmap
    } catch (e: Exception) {
        e.printStackTrace()
        aLog?.w(TAG, msg = "createQRCode exception ${e.message}")
    }
    return null
}

/**
 * Description: 将logo添加到目标二维码图片里
 * Author: summer
 *
 * @param srcBitmap 目标图片bitmap
 * @param logo logo图片
 * @param ratio 缩放比例
 */
private fun addLogo(srcBitmap: Bitmap, logo: Bitmap, ratio: Float) {
    if (ratio <= 0f) {
        aLog?.i(TAG, msg = "The logo's ratio is zero, drop the logo")
        return
    }
    val srcWidth = srcBitmap.width
    val srcHeight = srcBitmap.height
    val logoWidth = logo.width
    val logoHeight = logo.height
    if (srcWidth == 0 || srcHeight == 0 || logoWidth == 0 || logoHeight == 0) {
        aLog?.i(TAG, msg = "qrBitmap or logoBitmap width or height not correct, drop the logo")
        return
    }

    val scale = srcWidth * ratio / logoWidth
    val mixBitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(mixBitmap)
    canvas.drawBitmap(srcBitmap, 0f, 0f, null)
    canvas.scale(scale, scale, srcWidth / 2.0f, srcHeight / 2.0f)
    canvas.drawBitmap(logo, (srcWidth - logoWidth) / 2.0f, (srcHeight - logoHeight) / 2.0f, null)
    canvas.save()
    canvas.restore()
}
package com.atoolkit.aqrcode

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.TextPaint
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/**
 * Description: 根据内容创建二维码，耗时操作请放在协程或线程中执行
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
            return addLogo(qrBitmap, logo, logoRatio)
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
private fun addLogo(srcBitmap: Bitmap, logo: Bitmap, ratio: Float): Bitmap {
    if (ratio <= 0f) {
        aLog?.i(TAG, msg = "The logo's ratio is zero, drop the logo")
        return srcBitmap
    }
    val srcWidth = srcBitmap.width
    val srcHeight = srcBitmap.height
    val logoWidth = logo.width
    val logoHeight = logo.height
    if (srcWidth == 0 || srcHeight == 0 || logoWidth == 0 || logoHeight == 0) {
        aLog?.i(TAG, msg = "qrBitmap or logoBitmap width or height not correct, drop the logo")
        return srcBitmap
    }

    val scale = srcWidth * ratio / logoWidth
    val mixBitmap = Bitmap.createBitmap(srcWidth, srcHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(mixBitmap)
    canvas.drawBitmap(srcBitmap, 0f, 0f, null)
    canvas.scale(scale, scale, srcWidth / 2.0f, srcHeight / 2.0f)
    canvas.drawBitmap(logo, (srcWidth - logoWidth) / 2.0f, (srcHeight - logoHeight) / 2.0f, null)
    canvas.save()
    canvas.restore()
    return mixBitmap
}

/**
 * Description: 根据内容创建条形码，耗时操作，请放在协程或线程中执行
 * Author: summer
 *
 * @param content 条形码内容
 * @param width 条形码宽度
 * @param height 条形码高度
 * @param codeColor 条形码颜色，默认为黑色；不可设置为白色；
 * @param isShowText 条形码上是否展示内容文本，默认为false，不展示
 * @param textSize 条形码展示内容文本的文字大小，单位是px
 *
 * @return Bitmap， 条形码的图片，如果发生异常返回null
 */
@JvmOverloads
fun createBarCode(
    content: String,
    width: Int,
    height: Int,
    @ColorInt codeColor: Int = Color.BLACK,
    isShowText: Boolean = false,
    textSize: Int = 20
): Bitmap? {
    val formatWriter = MultiFormatWriter()
    try {
        val bitMatrix = formatWriter.encode(content, BarcodeFormat.CODE_128, width, height, null)
        val w = bitMatrix.width
        val h = bitMatrix.height
        val pixels = IntArray(w * h)
        for (y in 0 until height) {
            for (x in 0 until width) {
                pixels[x + y * width] = if (bitMatrix.get(x, y)) codeColor else Color.WHITE
            }
        }
        val barCodeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        barCodeBitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        if (isShowText) {
            return addCodeToBitmap(barCodeBitmap, content, textSize, codeColor)
        }
        return barCodeBitmap
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

/**
 * Description: 将文本添加到条形码上
 * Author: summer
 *
 * @param barCodeBitmap 条形码图片
 * @param content 条形码内容
 * @param textSize 内容文字大小
 * @param textColor 文本字体颜色
 *
 * @return 添加文字后的图片，如果添加失败则返回原图片
 */
fun addCodeToBitmap(barCodeBitmap: Bitmap, content: String, textSize: Int, @ColorInt textColor: Int): Bitmap {
    if (textSize <= 0) {
        return barCodeBitmap
    }
    val width = barCodeBitmap.width
    val height = barCodeBitmap.height

    try {
        val mixBitmap = Bitmap.createBitmap(width, height + textSize * 2, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(mixBitmap)
        canvas.drawBitmap(barCodeBitmap, 0f, 0f, null)
        val paint = TextPaint()
        paint.apply {
            this.textSize = textSize.toFloat()
            this.color = textColor
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(content, width / 2f, (height + textSize).toFloat(), paint)
        canvas.save()
        canvas.restore()
        return mixBitmap
    } catch (e: Exception) {
        e.printStackTrace()
        return barCodeBitmap
    }
}

package com.atoolkit.aqrcode

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image.Plane
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer


/**
 * Description: 将CameraX捕获到的图像（格式是YUV_420_888）转化为Bitmap对象
 * Author: summer
 */
@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
internal fun getBitmap(imageProxy: ImageProxy): Bitmap? {
    val nv21Buffer = yuv420ThreePlanesToNv21(imageProxy.image?.planes, imageProxy.width, imageProxy.height) ?: return null
    return getBitmap(nv21Buffer, imageProxy.width, imageProxy.height, imageProxy.imageInfo.rotationDegrees.toFloat())
}

/**
 * Description: 将ByteBuffer转化为Bitmap对象
 * Author: summer
 *
 * @param data 字节缓存对象
 * @param width 图像宽度
 * @param height 图像高度
 * @param rotationDegrees 旋转角度
 *
 * @return Bitmap对象，如果发生异常则返回null。
 */
internal fun getBitmap(
    data: ByteBuffer,
    width: Int,
    height: Int,
    rotationDegrees: Float
): Bitmap? {
    data.rewind()
    val imageBuffer = ByteArray(data.limit())
    data.get(imageBuffer, 0, imageBuffer.size)
    var result: Bitmap? = null
    var outputStream: ByteArrayOutputStream? = null
    try {
        val yuvImage = YuvImage(imageBuffer, ImageFormat.NV21, width, height, null)
        outputStream = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, width, height), 80, outputStream)
        val bmp = BitmapFactory.decodeByteArray(outputStream.toByteArray(), 0, outputStream.size())
        result = rotateBitmap(bmp, rotationDegrees)
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        outputStream?.close()
    }
    return result
}

/**
 * Description: 根据角度将目标Bitmap进行旋转
 * Author: summer
 *
 * @param bitmap 目标Bitmap图像
 * @param rotationDegrees 旋转的角度
 * @param flipX 是否水平翻转
 * @param flipY 是否垂直翻转
 *
 * @return 旋转后的Bitmap
 */
internal fun rotateBitmap(
    bitmap: Bitmap,
    rotationDegrees: Float,
    flipX: Boolean = false,
    flipY: Boolean = false
): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(rotationDegrees)
    matrix.postScale(
        if (flipX) -1.0f else 1.0f,
        if (flipY) -1.0f else 1.0f
    )
    val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    if (rotatedBitmap != bitmap) {
        bitmap.recycle()
    }
    return rotatedBitmap
}

/**
 * Converts YUV_420_888 to NV21 bytebuffer.
 *
 * <p>The NV21 format consists of a single byte array containing the Y, U and V values. For an
 * image of size S, the first S positions of the array contain all the Y values. The remaining
 * positions contain interleaved V and U values. U and V are subsampled by a factor of 2 in both
 * dimensions, so there are S/4 U values and S/4 V values. In summary, the NV21 array will contain
 * S Y values followed by S/4 VU values: YYYYYYYYYYYYYY(...)YVUVUVUVU(...)VU
 *
 * <p>YUV_420_888 is a generic format that can describe any YUV image where U and V are subsampled
 * by a factor of 2 in both dimensions. {@link Image#getPlanes} returns an array with the Y, U and
 * V planes. The Y plane is guaranteed not to be interleaved, so we can just copy its values into
 * the first part of the NV21 array. The U and V planes may already have the representation in the
 * NV21 format. This happens if the planes share the same buffer, the V buffer is one position
 * before the U buffer and the planes have a pixelStride of 2. If this is case, we can just copy
 * them to the NV21 array.
 */
internal fun yuv420ThreePlanesToNv21(
    yuv420888Planes: Array<Plane>?,
    width: Int,
    height: Int
): ByteBuffer? {
    if (yuv420888Planes == null) {
        return null
    }
    val imageSize = width * height
    val out = ByteArray(imageSize + 2 * (imageSize / 4))
    if (areUVPlanesNV21(yuv420888Planes, width, height)) {
        // Copy the Y values.
        yuv420888Planes[0].buffer.get(out, 0, imageSize)
        val uBuffer = yuv420888Planes[1].buffer
        val vBuffer = yuv420888Planes[2].buffer
        // Get the first V value from the V buffer, since the U buffer does not contain it.
        vBuffer.get(out, imageSize, 1)
        // Copy the first U value and the remaining VU values from the U buffer.
        uBuffer.get(out, imageSize + 1, 2 * (imageSize / 4) - 1)
    } else {
        // Fallback to copying the UV values one by one, which is slower but also works.
        // Unpack Y.
        unpackPlane(yuv420888Planes[0], width, height, out, 0, 1)
        // Unpack U.
        unpackPlane(yuv420888Planes[1], width, height, out, imageSize + 1, 2)
        // Unpack V.
        unpackPlane(yuv420888Planes[2], width, height, out, imageSize, 2)
    }
    return ByteBuffer.wrap(out)
}

/**
 * Checks if the UV plane buffers of a YUV_420_888 image are in the NV21 format.
 */
private fun areUVPlanesNV21(
    planes: Array<Plane>,
    width: Int,
    height: Int
): Boolean {
    val imageSize = width * height
    val uBuffer = planes[1].buffer
    val vBuffer = planes[2].buffer

    // Backup buffer properties.
    val vBufferPosition = vBuffer.position()
    val uBufferLimit = uBuffer.limit()

    // Advance the V buffer by 1 byte, since the U buffer will not contain the first V value.
    vBuffer.position(vBufferPosition + 1)
    // Chop off the last byte of the U buffer, since the V buffer will not contain the last U value.
    uBuffer.limit(uBufferLimit - 1)

    // Check that the buffers are equal and have the expected number of elements.
    val areNV21 = vBuffer.remaining() == 2 * imageSize / 4 - 2 && vBuffer.compareTo(uBuffer) == 0

    // Restore buffers to their initial state.
    vBuffer.position(vBufferPosition)
    uBuffer.limit(uBufferLimit)
    return areNV21
}

/**
 * Unpack an image plane into a byte array.
 *
 * <p>The input plane data will be copied in 'out', starting at 'offset' and every pixel will be
 * spaced by 'pixelStride'. Note that there is no row padding on the output.
 */
private fun unpackPlane(
    plane: Plane,
    width: Int,
    height: Int,
    out: ByteArray,
    offset: Int,
    pixelStride: Int
) {
    val buffer = plane.buffer
    buffer.rewind()

    // Compute the size of the current plane.
    // We assume that it has the aspect ratio as the original image.
    val numRow = (buffer.limit() + plane.rowStride - 1) / plane.rowStride
    if (numRow == 0) {
        return
    }
    val scaleFactor = height / numRow
    val numCol = width / scaleFactor

    // Extract the data in the output buffer.
    var outputPos = offset
    var rowStart = 0
    for (row in 0 until numRow) {
        var inputPos = rowStart
        for (col in 0 until numCol) {
            out[outputPos] = buffer[inputPos]
            outputPos += pixelStride
            inputPos += plane.pixelStride
        }
        rowStart += plane.rowStride
    }
}
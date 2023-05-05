package com.atoolkit.aqrcode.analyer

import com.google.zxing.Result
import androidx.camera.core.ImageProxy
import com.google.zxing.Reader


/**
 * Author:summer
 * Time: 2023/5/5 11:04
 * Description: IAnalyzer是图像解析器的接口，规范图像解析行为
 */
interface IAnalyzer<T: Reader> {

    /**
     * Description: 解析相机拍摄的图像，返回二维码解析结果
     * Author: summer
     *
     * @param image 相机拍摄图像对象
     * @param orientation 相机拍摄方向，取值为 [Configuration#ORIENTATION_LANDSCAPE], [Configuration#ORIENTATION_PORTRAIT].
     * @return 解析的结果
     */
    fun analyze(image: ImageProxy, orientation: Int): Result?

    /**
     * Description: 创建用于读取图像字节流的读取器，用来解析二维码或条形码
     * Author: summer
     *
     * @return 图像读取器
     */
    fun createReader(): T

}
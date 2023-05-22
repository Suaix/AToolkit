package com.atoolkit.aqrcode.scan


/**
 * Author:summer
 * Time: 2023/5/16 11:32
 * Description: ICameraControl是控制相机的接口，用来规范相机相关操作控制
 */
interface ICameraControl {
    /**
     * Description: 放大相机拍摄画面
     * Author: summer
     */
    fun zoomInCamera()

    /**
     * Description: 缩小相机拍摄画面
     * Author: summer
     */
    fun zoomOutCamera()

    /**
     * Description: 将相机拍摄画面缩放到指定比率，其值应大于等于支持的最小缩放值，小于等于最大缩放值
     * Author: summer
     */
    fun zoomTo(ratio: Float)

    /**
     * Description: 线性缩放
     * Author: summer
     *
     * @param linearZoom 取值[0..1]，取0时为相机支持的最小缩放比，取1时为相机支持的最大缩放比
     */
    fun lineZoomTo(linearZoom: Float)

    /**
     * Description: 当前相机是否有闪光灯单元
     * Author: summer
     */
    fun hasFlashUnit(): Boolean

    /**
     * Description: 控制闪光灯打开或关闭
     * Author: summer
     */
    fun toggleFlush(isTurnOn: Boolean)
}
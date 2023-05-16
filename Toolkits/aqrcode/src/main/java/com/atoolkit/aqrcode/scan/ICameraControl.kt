package com.atoolkit.aqrcode.scan


/**
 * Author:summer
 * Time: 2023/5/16 11:32
 * Description: ICameraControl是控制相机的接口，用来规范相机相关操作控制
 */
interface ICameraControl {
    fun zoomInCamera()
    fun zoomOutCamera()
    fun toggleFlush()
}
package com.atoolkit.aqrcode.scan


/**
 * Author:summer
 * Time: 2023/5/27 16:12
 * Description: ILightChangedListener是手机感光发生变化的回调
 */
interface ILightChangedListener {

    /**
     * Description: 感光条件发生变化
     * Author: summer
     *
     * @param lightState 光线状态，光线值小于等于灰暗边界值（默认是45）时为[LightState.DARK]，
     * 大于等于命令边界值时（默认是100）为[LightState.BRIGHT]，其他情况为[LightState.NORMAL]
     * @param lightLux 当前的感光光线值
     */
    fun onLightChanged(lightState: LightState, lightLux: Float)

}

enum class LightState {
    // 光线灰暗
    DARK,

    // 光线正常
    NORMAL,

    // 光线明亮
    BRIGHT
}

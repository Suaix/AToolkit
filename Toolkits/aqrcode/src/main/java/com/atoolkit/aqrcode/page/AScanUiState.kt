package com.atoolkit.aqrcode.page

import com.google.zxing.Result

internal sealed interface AScanUiState {
    /**
     * Description: 初始状态
     * Author: summer
     */
    object InitUiState : AScanUiState

    /**
     * Description: 请求权限状态
     * Author: summer
     */
    class PermissionUiState(val permission: String) : AScanUiState

    /**
     * Description: 扫码结果状态
     * Author: summer
     */
    class ResultUiState(val result: Result) : AScanUiState

    /**
     * Description: 光线状态变更状态
     * Author: summer
     *
     * @param isDark 当前环境光是否太暗了
     * @param isShowFlashView 是否展示闪光灯view
     */
    class LightUiState(val isDark: Boolean, val isShowFlashView: Boolean) : AScanUiState
}
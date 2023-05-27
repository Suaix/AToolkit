package com.atoolkit.aqrcode.page

import android.Manifest
import android.graphics.Rect
import androidx.camera.core.TorchState
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.atoolkit.apermission.isPermissionGranted
import com.atoolkit.aqrcode.TAG
import com.atoolkit.aqrcode.aLog
import com.atoolkit.aqrcode.config.AScanDecodeConfig
import com.atoolkit.aqrcode.config.DefaultFormats
import com.atoolkit.aqrcode.scan.ACameraScanHandler
import com.atoolkit.aqrcode.scan.ILightChangedListener
import com.atoolkit.aqrcode.scan.LightState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


/**
 * Author:summer
 * Time: 2023/4/27 20:45
 * Description: AScanViewModel是扫码Fragment的ViewModel，用来处理扫码相关业务逻辑
 */
internal class AScanViewModel : ViewModel() {
    private val mScanHandler = ACameraScanHandler()
    private var lifecycleOwner: LifecycleOwner? = null
    private var mCurrentLightState: LightState = LightState.NORMAL
    private val _uiState: MutableStateFlow<AScanUiState> = MutableStateFlow(AScanUiState.InitUiState)
    val uiState = _uiState.asStateFlow()

    /**
     * Description: 初始化
     * Author: summer
     */
    fun init(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        this.lifecycleOwner = lifecycleOwner
        mScanHandler.init(lifecycleOwner, previewView, object : ILightChangedListener {
            override fun onLightChanged(lightState: LightState, lightLux: Float) {
                // 环境光线变化监听
                if (mCurrentLightState == lightState) {
                    return
                }
                mCurrentLightState = lightState
                if (lightState == LightState.DARK) {
                    // 太暗或太亮并且有闪光灯的情况下，通知页面展示闪光灯view
                    _uiState.value = AScanUiState.LightUiState(true, isShowFlashView = true)
                }
            }
        })
    }

    /**
     * Description: 开启相机，开始扫描图像
     * Author: summer
     */
    fun startScan(isFullAreaScan: Boolean, scanArea: Rect? = null) {
        // 首先检查相机权限是否授予了
        if (!isPermissionGranted(Manifest.permission.CAMERA)) {
            _uiState.value = AScanUiState.PermissionUiState(Manifest.permission.CAMERA)
            return
        }
        mScanHandler.startScan(
            AScanDecodeConfig(
                hints = DefaultFormats().hints,
                isFullAreaScan = isFullAreaScan,
                analyzeAreaRect = scanArea
            )
        )
        lifecycleOwner?.let {
            mScanHandler.getScanResultLiveData().observe(it) { result ->
                _uiState.value = AScanUiState.ResultUiState(result)
            }
        }
    }

    /**
     * Description: 开关闪光灯
     * Author: summer
     */
    fun toggleFlash() {
        val isFlashOpen = mScanHandler.getTorchState()?.value == TorchState.ON
        mScanHandler.toggleFlush(!isFlashOpen)
        // 开关闪光灯后，取现在闪光灯的反状态（isFlashOpen）
        _uiState.value = AScanUiState.LightUiState(isDark = isFlashOpen, true)
    }

    override fun onCleared() {
        super.onCleared()
        mScanHandler.release()
    }

}
package com.atoolkit.aqrcode.widget

import android.Manifest
import android.graphics.Rect
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.atoolkit.apermission.isPermissionGranted
import com.atoolkit.aqrcode.TAG
import com.atoolkit.aqrcode.aLog
import com.atoolkit.aqrcode.config.AScanDecodeConfig
import com.atoolkit.aqrcode.config.DefaultFormats
import com.atoolkit.aqrcode.config.QrDecodeFormat
import com.atoolkit.aqrcode.scan.ACameraScanHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


/**
 * Author:summer
 * Time: 2023/4/27 20:45
 * Description: AScanViewModel是扫码Fragment的ViewModel，用来处理扫码相关业务逻辑
 */
internal class AScanViewModel : ViewModel() {
    private val _uiState: MutableStateFlow<AScanUiState> = MutableStateFlow(AScanUiState.InitUiState)
    val uiState = _uiState.asStateFlow()
    private val mScanHandler = ACameraScanHandler()

    /**
     * Description: 初始化
     * Author: summer
     */
    fun init(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        mScanHandler.init(lifecycleOwner, previewView)
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
        ) { isSuccess, result ->
            if (isSuccess && result != null) {
                _uiState.value = AScanUiState.ResultUiState(result)
            } else {
//                aLog?.i(TAG, "未识别到二维码或条形码！isSuccess=$isSuccess")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        mScanHandler.release()
    }

}
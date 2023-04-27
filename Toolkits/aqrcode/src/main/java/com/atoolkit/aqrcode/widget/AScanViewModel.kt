package com.atoolkit.aqrcode.widget

import androidx.camera.core.Camera
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.atoolkit.aqrcode.application
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.ref.WeakReference


/**
 * Author:summer
 * Time: 2023/4/27 20:45
 * Description: AScanViewModel是扫码Fragment的ViewModel，用来处理扫码相关业务逻辑
 */
internal class AScanViewModel : ViewModel() {
    private val _uiState: MutableStateFlow<AScanUiState> = MutableStateFlow(AScanUiState.InitUiState)
    val uiState = _uiState.asStateFlow()

    private var mPreviewView: WeakReference<PreviewView>? = null
    private var mCamera: Camera? = null

    fun init(previewView: PreviewView) {
        mPreviewView = WeakReference(previewView)
    }

    fun startCamera() {
        // TODO: 检查相机权限

        val listenableFuture = ProcessCameraProvider.getInstance(application)
        listenableFuture.addListener({

        }, ContextCompat.getMainExecutor(application))
    }

}
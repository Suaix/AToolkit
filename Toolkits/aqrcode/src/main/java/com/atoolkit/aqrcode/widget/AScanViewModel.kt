package com.atoolkit.aqrcode.widget

import android.Manifest
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.impl.CameraConfig
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import com.atoolkit.apermission.isPermissionGranted
import com.atoolkit.aqrcode.IMAGE_QUALITY_1080P
import com.atoolkit.aqrcode.IMAGE_QUALITY_720P
import com.atoolkit.aqrcode.TAG
import com.atoolkit.aqrcode.aLog
import com.atoolkit.aqrcode.analyer.AMultiFormatAnalyzer
import com.atoolkit.aqrcode.analyer.IAnalyzer
import com.atoolkit.aqrcode.application
import com.atoolkit.aqrcode.config.AAspectRationCameraConfig
import com.atoolkit.aqrcode.config.AResolutionCameraConfig
import com.atoolkit.aqrcode.config.AScanDecodeConfig
import com.atoolkit.aqrcode.config.DefaultFormats
import com.atoolkit.aqrcode.config.ICameraConfig
import com.google.zxing.MultiFormatReader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.ref.WeakReference
import java.util.concurrent.Executors


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
    private var mCameraConfig: ICameraConfig? = null
    private lateinit var mLifecycleOwner: LifecycleOwner
    private var mOrientation = application.resources.configuration.orientation

    @Volatile
    private var isAnalyze = true

    @Volatile
    private var isAnalyzeResult = false

    private var mAnalyzer: IAnalyzer<MultiFormatReader>? = null

    fun init(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        mLifecycleOwner = lifecycleOwner
        mPreviewView = WeakReference(previewView)
    }

    fun startCamera() {
        // 首先检查相机权限是否授予了
        if (!isPermissionGranted(Manifest.permission.CAMERA)) {
            _uiState.value = AScanUiState.PermissionUiState(Manifest.permission.CAMERA)
            return
        }
        // 初始化相机配置
        initCameraConfig()
        if (mAnalyzer == null) {
            mAnalyzer = AMultiFormatAnalyzer(
                AScanDecodeConfig(
                    hints = DefaultFormats().hints
                )
            )
        }
        // 构建并配置camerax
        val listenableFuture = ProcessCameraProvider.getInstance(application)
        listenableFuture.addListener({
            val preview = mCameraConfig?.optionPreview(Preview.Builder()) ?: Preview.Builder().build()
            val cameraSelector =
                mCameraConfig?.optionSelector(CameraSelector.Builder()) ?: CameraSelector.Builder().build()
            preview.setSurfaceProvider(mPreviewView?.get()?.surfaceProvider)
            val imageAnalysisBuilder = ImageAnalysis.Builder()
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            val imageAnalysis = mCameraConfig?.optionImageAnalysis(imageAnalysisBuilder) ?: imageAnalysisBuilder.build()
            imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor()) { proxy ->
                val imageWidth = proxy.width
                val imageHeight = proxy.height
                // 分析图片结果
                if (isAnalyze && !isAnalyzeResult) {
                    val result = mAnalyzer?.analyze(proxy, mOrientation)
                    if (result != null) {
                        aLog?.i(TAG, "result: ${result.text}")
                        isAnalyzeResult = true
                    }
                }
                proxy.close()
            }
            if (mCamera != null) {
                listenableFuture.get().unbindAll()
            }
            mCamera = listenableFuture.get().bindToLifecycle(mLifecycleOwner, cameraSelector, preview, imageAnalysis)
        }, ContextCompat.getMainExecutor(application))
    }

    private fun initCameraConfig() {
        if (mCameraConfig != null) {
            return
        }
        val dm = application.resources.displayMetrics
        val size = dm.widthPixels.coerceAtMost(dm.heightPixels)
        mCameraConfig = if (size > IMAGE_QUALITY_1080P) {
            AResolutionCameraConfig(IMAGE_QUALITY_1080P)
        } else if (size > IMAGE_QUALITY_720P) {
            AResolutionCameraConfig(IMAGE_QUALITY_720P)
        } else {
            AAspectRationCameraConfig()
        }
    }

}
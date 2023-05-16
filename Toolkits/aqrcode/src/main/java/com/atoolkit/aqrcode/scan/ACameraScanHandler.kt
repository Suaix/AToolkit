package com.atoolkit.aqrcode.scan

import android.Manifest
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
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
import com.atoolkit.aqrcode.config.ICameraConfig
import com.google.common.util.concurrent.ListenableFuture
import com.google.zxing.Result
import java.lang.ref.WeakReference
import java.util.concurrent.Executors


/**
 * Author:summer
 * Time: 2023/5/16 11:07
 * Description: ACameraScan是相机扫描处理类，用来管理相机相关操作、获取拍摄图像、转化成数据流并进行解析处理
 */
open class ACameraScanHandler {
    // CameraX预览view视图
    private var mPreviewView: WeakReference<PreviewView>? = null

    // CameraX相机提供者
    private var mCameraProvider: ListenableFuture<ProcessCameraProvider>? = null

    // CameraX相机实例
    private var mCamera: Camera? = null

    // 相机初始化配置
    private var mCameraConfig: ICameraConfig? = null

    // 生命周期拥有者
    private lateinit var mLifecycleOwner: LifecycleOwner

    // 是否已经初始化过，调用其他操作方法前需要确保已经初始化
    private var isInited = false

    @Volatile
    private var isAnalyze = true

    // 是否已经分析出结果
    @Volatile
    private var isAnalyzeResult = false

    // 图像分析器
    private var mAnalyzer: IAnalyzer? = null

    // 手机的方向：竖向 or 横向
    private var mOrientation = application.resources.configuration.orientation

    /**
     * Description: 初始化相机扫描，配置相关属性
     * Author: summer
     *
     * @param lifecycleOwner 生命周期拥有者，用来初始化CameraX
     * @param previewView 相机页面预览控件，用来展示相机拍摄到的画面
     */
    fun init(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        mLifecycleOwner = lifecycleOwner
        mPreviewView = WeakReference(previewView)
        isInited = true
    }

    /**
     * Description: 开始扫描，外部调用前请检查是否授予了相机权限，如果未授予相机权限下调用该方法会抛出异常；
     * Author: summer
     *
     * @param callback 回调扫描结果，回调的中第一个参数表示是否识别成功，第二个参数为识别结果（只有在识别成功时有值）
     */
    fun startScan(scanDecodeConfig: AScanDecodeConfig, callback: (Boolean, Result?) -> Unit) {
        check(isInited) {
            "Please call init method first before startScan"
        }
        if (!isPermissionGranted(Manifest.permission.CAMERA)) {
            throw IllegalStateException("has not granted the permission of CAMERA, can't stat scan")
        }
        // 根据配置创建图像解析器
        if (mAnalyzer == null) {
            mAnalyzer = createAnalyzer(scanDecodeConfig)
        }
        // 初始化相机配置
        initCameraConfig()
        // 构建并配置camerax
        mCameraProvider = ProcessCameraProvider.getInstance(application)
        mCameraProvider?.addListener({
            val preview = mCameraConfig?.optionPreview(Preview.Builder()) ?: Preview.Builder().build()
            val cameraSelector =
                mCameraConfig?.optionSelector(CameraSelector.Builder()) ?: CameraSelector.Builder().build()
            preview.setSurfaceProvider(mPreviewView?.get()?.surfaceProvider)
            val imageAnalysisBuilder = ImageAnalysis.Builder()
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            val imageAnalysis = mCameraConfig?.optionImageAnalysis(imageAnalysisBuilder) ?: imageAnalysisBuilder.build()
            // 设置图像解析运行线程及回调处理，注意其在指定的线程执行器中运行
            imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor()) { proxy ->
                // 分析图片结果
                if (isAnalyze && !isAnalyzeResult) {
                    val result = mAnalyzer?.analyze(proxy, mOrientation)
                    if (result != null) {
                        aLog?.i(TAG, "result: ${result.text}")
                        callback.invoke(true, result)
                        isAnalyzeResult = true
                    } else {
                        callback.invoke(false, null)
                    }
                }
                proxy.close()
            }
            if (mCamera != null) {
                mCameraProvider?.get()?.unbindAll()
            }
            mCamera = mCameraProvider?.get()?.bindToLifecycle(mLifecycleOwner, cameraSelector, preview, imageAnalysis)
        }, ContextCompat.getMainExecutor(application))
    }

    /**
     * Description: 初始化相机配置，主要是配置相机的清晰度或缩放比
     * Author: summer
     */
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

    open fun createAnalyzer(decodeConfig: AScanDecodeConfig): IAnalyzer {
        return AMultiFormatAnalyzer(decodeConfig)
    }
}
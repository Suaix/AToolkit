package com.atoolkit.aqrcode.scan

import android.Manifest
import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.View.OnTouchListener
import androidx.annotation.FloatRange
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
import com.google.zxing.common.detector.MathUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import java.util.concurrent.Executors


/**
 * Author:summer
 * Time: 2023/5/16 11:07
 * Description: ACameraScan是相机扫描处理类，用来管理相机相关操作、获取拍摄图像、转化成数据流并进行解析处理
 */
open class ACameraScanHandler : ICameraControl {
    private val zoomStepSize = 0.1f

    // 扫码结果观察数据
    private val scanResult = MutableLiveData<Result>()

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

    // 手势落下时的x位置
    private var mDownX: Float = 0f

    // 手势落下时的y位置
    private var mDownY: Float = 0f

    // 上一次手势落下的时间
    private var mLastTapTime = 0L

    // 是否是点击
    private var isTap = false

    // 判断是点击的滑动区域范围
    private val hoverTapSlop = 20

    // 判断是否点击超时时间
    private val hoverTimeOut = 150

    // 手机的方向：竖向 or 横向
    private val mOrientation = application.resources.configuration.orientation

    // 开始分析图片的时间点
    private var mAnalysisTime = 0L

    // 处理图像预览页面缩放手势事件
    private val scaleListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scaleFactor = detector.scaleFactor
            val zoomRatio = mCamera?.cameraInfo?.zoomState?.value?.zoomRatio
            if (zoomRatio != null) {
                zoomTo(zoomRatio * scaleFactor)
                return true
            }
            return super.onScale(detector)
        }
    }

    /**
     * Description: 初始化相机扫描，配置相关属性
     * Author: summer
     *
     * @param lifecycleOwner 生命周期拥有者，用来初始化CameraX
     * @param previewView 相机页面预览控件，用来展示相机拍摄到的画面
     */
    @SuppressLint("ClickableViewAccessibility")
    fun init(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        mLifecycleOwner = lifecycleOwner
        mPreviewView = WeakReference(previewView)
        val gestureDetector = ScaleGestureDetector(application, scaleListener)
        previewView.setOnTouchListener(object : OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (event == null) {
                    return false
                }
                handleClickTap(event)
                if (isNeedTouchZoom()) {
                    return gestureDetector.onTouchEvent(event)
                }
                return false
            }

        })
        isInited = true
    }

    /**
     * Description: 开始扫描，外部调用前请检查是否授予了相机权限，如果未授予相机权限下调用该方法会抛出异常；
     * Author: summer
     *
     * @param scanDecodeConfig 扫码解码配置
     */
    fun startScan(scanDecodeConfig: AScanDecodeConfig) {
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
                    if (mAnalysisTime == 0L) {
                        mAnalysisTime = System.currentTimeMillis()
                    }
                    val result = mAnalyzer?.analyze(proxy, mOrientation)
                    if (result != null) {
                        aLog?.i(TAG, "result: ${result.text}")
                        runBlocking {
                            withContext(Dispatchers.Main){
                                scanResult.value = result
                            }
                        }
                        isAnalyzeResult = true
                    }
                    if (mAnalysisTime != -1L && System.currentTimeMillis() - mAnalysisTime >= autoZoomTime()) {
                        lineZoomTo(0.5f)
                        mAnalysisTime = -1L
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

    /**
     * Description: 处理Preview的点击事件
     * Author: summer
     */
    private fun handleClickTap(event: MotionEvent) {
        if (event.pointerCount != 1) {
            return
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isTap = true
                mDownX = event.x
                mDownY = event.y
                mLastTapTime = System.currentTimeMillis()
            }
            MotionEvent.ACTION_MOVE -> {
                isTap = MathUtils.distance(mDownX, mDownY, event.x, event.y) < hoverTapSlop
            }
            MotionEvent.ACTION_UP -> {
                aLog?.i(TAG, "action up, isTap=$isTap")
                if (isTap && System.currentTimeMillis() - mLastTapTime < hoverTimeOut) {
                    aLog?.i(TAG, "start focus and metering.....")
                    // 执行对焦和聚光操作
                    startFocusAndMetering(event.x, event.y)
                }
                isTap = false
            }
        }
    }

    /**
     * Description: 开始对焦和聚光操作
     * Author: summer
     * @param x 聚焦的x位置
     * @param y 聚焦的y位置
     */
    private fun startFocusAndMetering(x: Float, y: Float) {
        val previewView = mPreviewView?.get()
        previewView?.let {
            val point = it.meteringPointFactory.createPoint(x, y)
            val action = FocusMeteringAction.Builder(point).build()
            if (mCamera?.cameraInfo?.isFocusMeteringSupported(action) == true) {
                aLog?.i(TAG, "call camera control startFocusAndMetering")
                mCamera?.cameraControl?.startFocusAndMetering(action)
            }
        }
    }

    /**
     * Description: 创建解码分析器，用来解析扫描到的图像中码信息，默认采用多格式分析器
     * Author: summer
     */
    open fun createAnalyzer(decodeConfig: AScanDecodeConfig): IAnalyzer {
        return AMultiFormatAnalyzer(decodeConfig)
    }

    /**
     * Description: 是否支持手势缩放
     * Author: summer
     */
    open fun isNeedTouchZoom(): Boolean {
        return true
    }

    /**
     * Description: 是否支持自动缩放，如果支持：在相机启动一段时间内如果还解析不到结果，则会按照50%拉近摄像头距离
     * Author: summer
     */
    open fun isSupportAutoZoom(): Boolean {
        return true
    }

    /**
     * Description: 支持自动缩放时，在多长时间间隔内缩放相机
     * Author: summer
     */
    open fun autoZoomTime(): Long {
        return 3000
    }

    /**
     * Description: 用来控制是否解析相机拍摄到的图像
     * Author: summer
     */
    fun setIsAnalyze(isAnalyze: Boolean) {
        this.isAnalyze = isAnalyze
    }

    /**
     * Description: 扫码结果LiveData，供外部监听结果使用
     * Author: summer
     */
    fun getScanResultLiveData(): LiveData<Result> {
        return scanResult
    }

    /**
     * Description: 释放扫码相关资源
     * Author: summer
     */
    fun release() {
        mCameraProvider?.get()?.unbindAll()
        mPreviewView = null
    }

    override fun zoomInCamera() {
        val currentRatio = mCamera?.cameraInfo?.zoomState?.value?.zoomRatio
        if (currentRatio != null) {
            val targetRatio = currentRatio + zoomStepSize
            zoomTo(targetRatio)
        }
    }

    override fun zoomOutCamera() {
        val currentRatio = mCamera?.cameraInfo?.zoomState?.value?.zoomRatio
        if (currentRatio != null) {
            val targetRatio = currentRatio - zoomStepSize
            zoomTo(targetRatio)
        }
    }

    override fun zoomTo(ratio: Float) {
        val maxRation = mCamera?.cameraInfo?.zoomState?.value?.maxZoomRatio ?: 0f
        val minRation = mCamera?.cameraInfo?.zoomState?.value?.minZoomRatio ?: 0f
        if (ratio in minRation..maxRation) {
            mCamera?.cameraControl?.setZoomRatio(ratio)
        }
    }

    override fun lineZoomTo(@FloatRange(from = 0.0, to = 1.0) linearZoom: Float) {
        mCamera?.cameraControl?.setLinearZoom(linearZoom)
    }

    override fun hasFlashUnit(): Boolean {
        return mCamera?.cameraInfo?.hasFlashUnit() ?: false
    }

    override fun toggleFlush(isTurnOn: Boolean) {
        if (!hasFlashUnit()) {
            return
        }
        mCamera?.cameraControl?.enableTorch(isTurnOn)
    }
}
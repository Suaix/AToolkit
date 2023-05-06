package com.atoolkit.aqrcode.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Paint.Style
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.atoolkit.aqrcode.R
import com.atoolkit.aqrcode.TAG
import com.atoolkit.aqrcode.aLog
import kotlin.math.min


/**
 * Author:summer
 * Time: 2023/4/25 19:31
 * Description: AScanView是自定义扫码view，用来渲染扫描效果
 */
class AScanView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    View(context, attrs, defStyleAttr) {

    // 绘制图像的画笔
    private var mPaint: Paint

    // 扫描线的颜色
    private var mScanLineColor: Int

    // 扫描区域占比(扫码区域高度 / 竖向区域高度)，在全屏模式下建议是0.5（扫描屏幕中间的一半区域），在经典模式下建议设置为1（扫码框所有区域）
    private var mScanAreaRatio = 0.5f

    // 扫描线的高度
    private var mScanLineHeight = 0f

    // 扫描线的左、上、右、下距边框间距
    private val mScanLineMargin = RectF()

    // 扫描线的区域
    private val mScanRect = RectF()

    // 扫描线纵向开始的位置
    private var mScanLineStartPosition = 0f

    // 扫描线每次移动的距离
    private var mScanLineMoveDistance = 0f

    // 当扫描线样式为image时，扫描线的图片对象
    private var mScanLineBitmap: Bitmap? = null

    // 绘制扫描线oval椭圆形状的区域
    private val mLineOvalRectF = RectF()

    // 扫码框外部遮罩颜色
    private var mMaskColor: Int

    // 经典模式下，扫码框区域
    private val mFrame = RectF()

    // 经典模式下，扫码框大小（正方形）
    private var mFrameSize = -1f

    // 扫码边框的颜色
    private var mFrameColor: Int
    private var mFrameStrokeWidth: Float

    // 边框四个角的颜色
    private var mCornerColor: Int
    private var mCornerWidth = 0f
    private var mCornerHeight = 0f
    private var mCornerMargin = 0f

    // 扫码模式，默认为全屏
    private var mScanModel = AScanModel.FULLSCREEN

    // 扫描线的样式，默认为线样式
    private var mScanLineStyle = AScanLineStyle.LINE
    private var mAnimationInterval = 20L

    init {
        // 处理自定义属性
        val ta = context.obtainStyledAttributes(attrs, R.styleable.AScanView)
        mScanModel = if (ta.getInt(R.styleable.AScanView_aqr_scan_model, 0) == 0) {
            AScanModel.FULLSCREEN
        } else {
            AScanModel.CLASSIC
        }
        val scanLineStyle = ta.getInt(R.styleable.AScanView_aqr_scan_line_style, 1)
        mScanLineStyle = when (scanLineStyle) {
            AScanLineStyle.NONE.value -> AScanLineStyle.NONE
            AScanLineStyle.IMAGE.value -> AScanLineStyle.IMAGE
            else -> AScanLineStyle.LINE
        }
        mScanLineColor = ta.getColor(
            R.styleable.AScanView_aqr_scan_line_color, ContextCompat.getColor(context, R.color.aqr_ff1fb3e2)
        )
        // 全屏模式下默认扫码比率为0.5，经典模式下为1
        val defaultRation = if (mScanModel == AScanModel.FULLSCREEN) 0.5f else 1f
        mScanAreaRatio = ta.getFloat(R.styleable.AScanView_aqr_scan_line_ratio, defaultRation)
        mScanLineHeight = ta.getDimension(R.styleable.AScanView_aqr_scan_line_height, 2f)
        mScanLineMoveDistance = ta.getDimension(R.styleable.AScanView_aqr_scan_line_move_distance, 4f)
        val defaultMargin = if (mScanModel == AScanModel.FULLSCREEN) 24f else 0f
        val marginLeft = ta.getDimension(R.styleable.AScanView_aqr_scan_line_margin_left, defaultMargin)
        val marginTop = ta.getDimension(R.styleable.AScanView_aqr_scan_line_margin_top, 0f)
        val marginRight = ta.getDimension(R.styleable.AScanView_aqr_scan_line_margin_right, defaultMargin)
        val marginBottom = ta.getDimension(R.styleable.AScanView_aqr_scan_line_margin_bottom, 0f)
        mScanLineMargin.set(marginLeft, marginTop, marginRight, marginBottom)
        val scanLineDrawable = ta.getDrawable(R.styleable.AScanView_aqr_scan_line_drawable)
        if (scanLineDrawable != null) {
            mScanLineBitmap = covertDrawableToBitmap(scanLineDrawable)
        } else if (mScanLineStyle == AScanLineStyle.IMAGE) {
            // 设置的模式是图片模式，但未配置图片，降级为线样式
            aLog?.w(
                TAG,
                "aqr_scan_line_style is image, but aqr_scan_line_drawable is not a Drawable, change the style to AScanLineStyle.LINE"
            )
            mScanLineStyle = AScanLineStyle.LINE
        }
        mAnimationInterval = ta.getInt(R.styleable.AScanView_aqr_scan_line_animation_interval, 20).toLong()

        mMaskColor = ta.getColor(
            R.styleable.AScanView_aqr_mask_color, ContextCompat.getColor(context, R.color.aqr_60000000)
        )

        mFrameColor = ta.getColor(
            R.styleable.AScanView_aqr_frame_color, ContextCompat.getColor(context, R.color.aqr_7f1fb3e2)
        )
        mFrameSize = ta.getDimension(R.styleable.AScanView_aqr_frame_size, 600f)
        mFrameStrokeWidth = ta.getDimension(R.styleable.AScanView_aqr_frame_stroke_width, 4f)

        mCornerColor = ta.getColor(
            R.styleable.AScanView_aqr_corner_color, ContextCompat.getColor(context, R.color.aqr_ff1fb3e2)
        )
        mCornerWidth = ta.getDimension(R.styleable.AScanView_aqr_corner_width, 32f)
        mCornerHeight = ta.getDimension(R.styleable.AScanView_aqr_corner_height, 8f)
        mCornerMargin = ta.getDimension(R.styleable.AScanView_aqr_corner_margin, 8f)
        ta.recycle()
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint.isAntiAlias = true
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        // 初始化扫码边框的位置
        initFrame(width, height)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (mScanModel == AScanModel.FULLSCREEN) {
            drawFullScreen(canvas)
        } else {
            drawClassic(canvas)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return super.onTouchEvent(event)
    }

    /**
     * Description: 初始化扫码框的区域范围
     * Author: summer
     *
     * @param viewWidth View的宽度
     * @param viewHeight View的高度
     */
    private fun initFrame(viewWidth: Int, viewHeight: Int) {
        when (mScanModel) {
            AScanModel.CLASSIC -> {
                // 经典模式
                val size = min(viewWidth, viewHeight)
                if (mFrameSize > size) {
                    // 扫描区域大于最小边或未指定，将其默认为最小边
                    mFrameSize = size.toFloat()
                }
                // 确定框的区域
                mFrame.set(
                    (viewWidth - mFrameSize) / 2,
                    (viewHeight - mFrameSize) / 2,
                    (viewWidth + mFrameSize) / 2,
                    (viewHeight + mFrameSize) / 2
                )
                // 确定扫描线的区域
                mScanRect.set(
                    mFrame.left + mScanLineMargin.left,
                    mFrame.top + mScanLineMargin.top,
                    mFrame.right - mScanLineMargin.right,
                    mFrame.bottom - mScanLineMargin.bottom
                )
            }
            else -> {
                // 全屏模式
                // 扫描线的宽度 = view宽度 - 扫描线左边距 - 扫描线右边距
                val scanLineWidth = viewWidth - mScanLineMargin.left - mScanLineMargin.right
                // 扫描距离= view高度 * 扫描占比
                val verticalDistance = viewHeight * mScanAreaRatio
                mScanRect.set(
                    (viewWidth - scanLineWidth) / 2,
                    (viewHeight - verticalDistance) / 2 + mScanLineMargin.top,
                    (viewWidth + scanLineWidth) / 2,
                    (viewHeight + verticalDistance) / 2 - mScanLineMargin.bottom
                )
            }
        }
        if (mScanLineStyle == AScanLineStyle.IMAGE && mScanLineBitmap != null) {
            scaleScanLineBitmap()
        }
        mScanLineStartPosition = mScanRect.top
    }

    /**
     * Description: 绘制全屏模式扫码样式
     * Author: summer
     */
    private fun drawFullScreen(canvas: Canvas?) {
        if (canvas == null) {
            return
        }
        when (mScanLineStyle) {
            AScanLineStyle.LINE -> {
                drawScanLine(canvas)
                postInvalidateDelayed(mAnimationInterval)
            }
            AScanLineStyle.IMAGE -> {
                drawScanLineBitmap(canvas)
                postInvalidateDelayed(mAnimationInterval)
            }
            else -> {
                aLog?.i(TAG, "scan line style is none, draw nothing")
            }
        }
    }

    /**
     * Description: 绘制经典模式扫码样式
     * Author: summer
     */
    private fun drawClassic(canvas: Canvas?) {
        if (canvas == null) {
            return
        }
        // 绘制蒙层
        drawMasker(canvas)
        // 绘制扫描线
        if (mScanLineStyle == AScanLineStyle.LINE){
            drawScanLine(canvas)
        } else if (mScanLineStyle == AScanLineStyle.IMAGE){
            drawScanLineBitmap(canvas)
        }
        // 绘制扫码框和角标
        drawFrameAndCorner(canvas)
        // 更新扫码框区域
        postInvalidateDelayed(
            mAnimationInterval,
            mFrame.left.toInt(),
            mFrame.top.toInt(),
            mFrame.right.toInt(),
            mFrame.bottom.toInt()
        )
    }

    /**
     * Description: 绘制蒙层区域
     * Author: summer
     */
    private fun drawMasker(canvas: Canvas) {
        mPaint.color = mMaskColor
        mPaint.style = Style.FILL
        val w = width
        val h = height
        canvas.drawRect(0f, 0f, w.toFloat(), mFrame.top, mPaint)
        canvas.drawRect(0f, mFrame.top, mFrame.left, mFrame.bottom, mPaint)
        canvas.drawRect(mFrame.right, mFrame.top, w.toFloat(), mFrame.bottom, mPaint)
        canvas.drawRect(0f, mFrame.bottom, w.toFloat(), h.toFloat(), mPaint)
    }

    /**
     * Description: 画扫描线
     * Author: summer
     */
    private fun drawScanLine(canvas: Canvas) {
        val lg = LinearGradient(
            mScanRect.left, mScanLineStartPosition,
            mScanRect.left, mScanLineStartPosition + mScanLineHeight,
            mScanLineColor, mScanLineColor, Shader.TileMode.MIRROR
        )
        mPaint.color = mScanLineColor
        mPaint.style = Style.FILL
        mPaint.shader = lg
        if (mScanLineStartPosition < mScanRect.bottom) {
            mLineOvalRectF.set(
                mScanRect.left,
                mScanLineStartPosition,
                mScanRect.right,
                mScanLineStartPosition + mScanLineHeight
            )
            canvas.drawOval(mLineOvalRectF, mPaint)
            mScanLineStartPosition += mScanLineMoveDistance
        } else {
            mScanLineStartPosition = mScanRect.top
        }
        mPaint.shader = null
    }

    /**
     * Description: 绘制图片扫描线
     * Author: summer
     */
    private fun drawScanLineBitmap(canvas: Canvas) {
        mScanLineBitmap?.let {
            mPaint.color = Color.WHITE
            if (mScanLineStartPosition < mScanRect.bottom) {
                canvas.drawBitmap(it, mScanRect.left, mScanLineStartPosition, mPaint)
                mScanLineStartPosition += mScanLineMoveDistance
            } else {
                mScanLineStartPosition = mScanRect.top
            }
        }
    }

    /**
     * Description: 绘制扫描框和角标
     * Author: summer
     */
    private fun drawFrameAndCorner(canvas: Canvas) {
        // 绘制扫描框
        mPaint.color = mFrameColor
        mPaint.style = Style.STROKE
        mPaint.strokeWidth = mFrameStrokeWidth
        canvas.drawRect(mFrame, mPaint)

        // 绘制四个角
        mPaint.color = mCornerColor
        mPaint.style = Style.FILL
        // 左上
        canvas.drawRect(
            mFrame.left + mCornerMargin,
            mFrame.top + mCornerMargin,
            mFrame.left + mCornerMargin + mCornerWidth,
            mFrame.top + mCornerMargin + mCornerHeight,
            mPaint
        )
        canvas.drawRect(
            mFrame.left + mCornerMargin,
            mFrame.top + mCornerMargin,
            mFrame.left + mCornerMargin + mCornerHeight,
            mFrame.top + mCornerMargin + mCornerWidth,
            mPaint
        )
        // 右上
        canvas.drawRect(
            mFrame.right - mCornerMargin - mCornerWidth,
            mFrame.top + mCornerMargin,
            mFrame.right - mCornerMargin,
            mFrame.top + mCornerMargin + mCornerHeight,
            mPaint
        )
        canvas.drawRect(
            mFrame.right - mCornerMargin - mCornerHeight,
            mFrame.top + mCornerMargin,
            mFrame.right - mCornerMargin,
            mFrame.top + mCornerMargin + mCornerWidth,
            mPaint
        )
        // 右下
        canvas.drawRect(
            mFrame.right - mCornerMargin - mCornerHeight,
            mFrame.bottom - mCornerMargin - mCornerWidth,
            mFrame.right - mCornerMargin,
            mFrame.bottom - mCornerMargin,
            mPaint
        )
        canvas.drawRect(
            mFrame.right - mCornerMargin - mCornerWidth,
            mFrame.bottom - mCornerMargin - mCornerHeight,
            mFrame.right - mCornerMargin,
            mFrame.bottom - mCornerMargin,
            mPaint
        )
        // 左下
        canvas.drawRect(
            mFrame.left + mCornerMargin,
            mFrame.bottom - mCornerMargin - mCornerHeight,
            mFrame.left + mCornerMargin + mCornerWidth,
            mFrame.bottom - mCornerMargin,
            mPaint
        )
        canvas.drawRect(
            mFrame.left + mCornerMargin,
            mFrame.bottom - mCornerMargin - mCornerWidth,
            mFrame.left + mCornerMargin + mCornerHeight,
            mFrame.bottom - mCornerMargin,
            mPaint
        )
    }

    /**
     * Description: 将Drawable对象转化成Bitmap对象
     * Author: summer
     */
    private fun covertDrawableToBitmap(drawable: Drawable): Bitmap {
        val bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, bitmap.width, bitmap.height)
        drawable.draw(canvas)
        return bitmap
    }

    /**
     * Description: 缩放扫描线图片
     * Author: summer
     */
    private fun scaleScanLineBitmap() {
        mScanLineBitmap?.let {
            val bitmapWidth = it.width
            val scanLineWidth = mScanRect.width()
            val ratio = scanLineWidth / bitmapWidth
            val matrix = Matrix()
            matrix.postScale(ratio, ratio)
            val w = it.width
            val h = it.height
            mScanLineBitmap = Bitmap.createBitmap(it, 0, 0, w, h, matrix, true)
        }
    }
}

/**
 * Description: 扫描的模式，枚举类
 * Author: summer
 * Date: 2023/4/25 20:00
 * LastModifyTime:
 */
enum class AScanModel(val value: Int) {
    // 全屏模式扫描
    FULLSCREEN(0),

    // 经典模式，四方块+扫描
    CLASSIC(1)
}

enum class AScanLineStyle(val value: Int) {
    // 没有样式，不画任何扫描标识
    NONE(0),

    // 线样式，根据颜色画线
    LINE(1),

    // 图片样式，需要配置线图片
    IMAGE(2)
}
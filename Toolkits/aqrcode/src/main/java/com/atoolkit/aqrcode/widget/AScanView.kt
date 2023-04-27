package com.atoolkit.aqrcode.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Paint.Style
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.atoolkit.aqrcode.R
import com.atoolkit.autils.dp2Px


/**
 * Author:summer
 * Time: 2023/4/25 19:31
 * Description: AScanView是自定义扫码view，用来渲染扫描效果
 */
class AScanView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 绘制图像的画笔
    private var mPaint: Paint

    // 扫描线的颜色
    private var mScanLineColor: Int

    // 扫描线相对于扫描框宽度的缩放比，在全屏模式下为屏幕宽度的缩放比，在经典模式下为扫码框宽度的缩放比，默认是90%；
    private var mScanLineRatio = 0.9f

    // 扫描线的高度
    private var mScanLineHeight = 0f

    // 扫描线的区域
    private var mScanLineRect = RectF()

    // 扫描线纵向开始的位置
    private var mScanLineStartPosition = 0f

    // 扫描线每次移动的距离
    private var mScanLineMoveDistance = 0f

    // 绘制扫描线oval椭圆形状的区域
    private val mLineOvalRectF = RectF()

    // 扫码框外部遮罩颜色
    private var mMaskColor: Int

    // 扫码边框的颜色
    private var mFrameColor: Int

    // 边框四个角的颜色
    private var mCornerColor: Int
    private var mCornerWidth = 0f
    private var mCornerHeight = 0f
    private var mCornerMargin = 0f

    // 经典模式下，扫码框默认宽高都是100dp，居中展示
    private val mFrame = RectF()
    private var mFrameWidth = 0f
    private var mFrameHeight = 0f

    // 扫码框距横向偏移量，其值大于0时向右偏移，其值小于0时向左偏移
    private var mFrameHorizontalOffset = 0f

    // 扫码框纵向偏移量，其值大于0时向下偏移，其值小于0时向上偏移
    private var mFrameVerticalOffset = 0f

    // 扫码模式，默认为全屏
    private var mScanModel = AScanModel.FULLSCREEN
    private var mAnimationInterval = 20L

    init {
        // 处理自定义属性
        val ta = context.obtainStyledAttributes(attrs, R.styleable.AScanView)
        mScanLineColor = ta.getColor(
            R.styleable.AScanView_aqr_scan_line_color, ContextCompat.getColor(context, R.color.aqr_ff1fb3e2)
        )
        mScanLineRatio = ta.getFloat(R.styleable.AScanView_aqr_scan_line_ratio, 0.9f)
        mScanLineHeight = ta.getDimension(R.styleable.AScanView_aqr_scan_line_height, dp2Px(2f))
        mScanLineMoveDistance = ta.getDimension(R.styleable.AScanView_aqr_scan_line_move_distance, dp2Px(2f))

        mMaskColor = ta.getColor(
            R.styleable.AScanView_aqr_mask_color, ContextCompat.getColor(context, R.color.aqr_60000000)
        )

        mFrameColor = ta.getColor(
            R.styleable.AScanView_aqr_frame_color, ContextCompat.getColor(context, R.color.aqr_7f1fb3e2)
        )
        mFrameWidth = ta.getDimension(R.styleable.AScanView_aqr_frame_width, dp2Px(300f))
        mFrameHeight = ta.getDimension(R.styleable.AScanView_aqr_frame_height, dp2Px(300f))
        mFrameHorizontalOffset = ta.getDimension(R.styleable.AScanView_aqr_frame_horizontal_offset, 0f)
        mFrameVerticalOffset = ta.getDimension(R.styleable.AScanView_aqr_frame_vertical_offset, 0f)

        mCornerColor = ta.getColor(
            R.styleable.AScanView_aqr_corner_color, ContextCompat.getColor(context, R.color.aqr_ff1fb3e2)
        )
        mCornerWidth = ta.getDimension(R.styleable.AScanView_aqr_corner_width, dp2Px(16f))
        mCornerHeight = ta.getDimension(R.styleable.AScanView_aqr_corner_height, dp2Px(4f))
        mCornerMargin = ta.getDimension(R.styleable.AScanView_aqr_corner_margin, dp2Px(4f))

        mScanModel = if (ta.getInt(R.styleable.AScanView_aqr_scan_model, 0) == 0) {
            AScanModel.FULLSCREEN
        } else {
            AScanModel.CLASSIC
        }
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
                if (mFrameWidth > viewWidth) {
                    mFrameWidth = viewWidth.toFloat()
                }
                if (mFrameHeight > viewHeight) {
                    mFrameHeight = viewHeight.toFloat()
                }
                mFrame.set(
                    (viewWidth - mFrameWidth) / 2 + mFrameHorizontalOffset,
                    (viewHeight - mFrameHeight) / 2 + mFrameVerticalOffset,
                    (viewWidth + mFrameWidth) / 2 + mFrameHorizontalOffset,
                    (viewHeight + mFrameHeight) / 2 + mFrameVerticalOffset
                )
                val scanLineWidth = mFrameWidth * mScanLineRatio
                mScanLineRect.set(
                    mFrame.left + (mFrameWidth - scanLineWidth) / 2,
                    mFrame.top,
                    mFrame.right - (mFrameWidth - scanLineWidth) / 2,
                    mFrame.bottom
                )
            }
            else -> {
                // 全屏模式
                val scanLineWidth = viewWidth * mScanLineRatio
                val verticalDistance = viewHeight * mScanLineRatio
                mScanLineRect.set(
                    (viewWidth - scanLineWidth) / 2,
                    (viewHeight - verticalDistance) / 2,
                    (viewWidth + scanLineWidth) / 2,
                    (viewHeight + verticalDistance) / 2
                )
            }
        }
        mScanLineStartPosition = mScanLineRect.top
    }

    /**
     * Description: 绘制全屏模式扫码样式
     * Author: summer
     */
    private fun drawFullScreen(canvas: Canvas?) {
        if (canvas == null) {
            return
        }
        drawScanLine(canvas)
        postInvalidateDelayed(mAnimationInterval)
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
        drawScanLine(canvas)
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
            mScanLineRect.left, mScanLineStartPosition,
            mScanLineRect.left, mScanLineStartPosition + mScanLineHeight,
            mScanLineColor, mScanLineColor, Shader.TileMode.MIRROR
        )
        mPaint.color = mScanLineColor
        mPaint.style = Style.FILL
        mPaint.shader = lg
        if (mScanLineStartPosition < mScanLineRect.bottom) {
            mLineOvalRectF.set(
                mScanLineRect.left,
                mScanLineStartPosition,
                mScanLineRect.right,
                mScanLineStartPosition + mScanLineHeight
            )
            canvas.drawOval(mLineOvalRectF, mPaint)
            mScanLineStartPosition += mScanLineMoveDistance
        } else {
            mScanLineStartPosition = mScanLineRect.top
        }
        mPaint.shader = null
    }

    /**
     * Description: 绘制扫描框和角标
     * Author: summer
     */
    private fun drawFrameAndCorner(canvas: Canvas) {
        // 绘制扫描框
        mPaint.color = mFrameColor
        mPaint.style = Style.STROKE
        mPaint.strokeWidth = 4f
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
    NONE(0),
    LINE(1),
    GRID(2),
    IMAGE(3)
}
package com.atoolkit.aqrcode.scan

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.atoolkit.aqrcode.application


/**
 * Author:summer
 * Time: 2023/5/27 15:06
 * Description: ABeepControl是负责控制扫码结果时播放声音和震动效果
 */
class ABeepControl : MediaPlayer.OnErrorListener {

    private var mMediaPlayer: MediaPlayer? = null
    private var mVibrator: Vibrator? = null
    private var mVibratorManager: VibratorManager? = null

    init {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            mVibrator = application.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        } else {
            mVibratorManager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        }
        buildMediaPlayer()
    }

    /**
     * Description: 构建播放器实例，并设置音频资源
     * Author: summer
     */
    private fun buildMediaPlayer() {
        mMediaPlayer = MediaPlayer()
        try {
            val fd = application.resources.openRawResourceFd(com.atoolkit.aqrcode.R.raw.aqrcode_completed)
            mMediaPlayer?.apply {
                setDataSource(fd.fileDescriptor, fd.startOffset, fd.length)
                setOnErrorListener(this@ABeepControl)
                isLooping = false
                prepareAsync()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            release()
        }
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        release()
        buildMediaPlayer()
        return true
    }

    /**
     * Description: 播放音效
     * Author: summer
     */
    @JvmOverloads
    fun playBeep(isPlaySound: Boolean = true, isVibrate: Boolean = true) {
        if (isPlaySound) {
            mMediaPlayer?.start()
        }
        if (!isVibrate) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            mVibratorManager?.vibrate(
                CombinedVibration.createParallel(
                    VibrationEffect.createOneShot(
                        200,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            )
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mVibrator?.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            mVibrator?.vibrate(200)
        }
    }

    /**
     * Description: 释放持有的资源
     * Author: summer
     */
    fun release() {
        mMediaPlayer?.release()
        mMediaPlayer = null
    }
}
package com.atoolkit.alog

import android.content.Context
import android.util.Log

private const val TAG = "ALog"

/**
 * Author:summer
 * Time: 2023/3/17 13:48
 * Description: ALog是ILog的默认实现，使用Android自带的Log进行日志输出
 */
class ALog : IALog {
    private var isDebug = false
    override fun init(context: Context, config: ALogConfig) {
        v(TAG, "ALog init with config --> $config", null)
        isDebug = config.isDebug
    }

    override fun v(tag: String?, msg: String, t: Throwable?) {
        if (!isDebug){
            return
        }
        t?.let {
            Log.v(tag, msg, it)
        } ?: Log.v(tag, msg)
    }

    override fun d(tag: String?, msg: String, t: Throwable?) {
        if (!isDebug){
            return
        }
        t?.let {
            Log.d(tag, msg, it)
        } ?: Log.d(tag, msg)
    }

    override fun i(tag: String?, msg: String, t: Throwable?) {
        if (!isDebug){
            return
        }
        t?.let {
            Log.i(tag, msg, it)
        } ?: Log.i(tag, msg)
    }

    override fun w(tag: String?, msg: String, t: Throwable?) {
        if (!isDebug){
            return
        }
        t?.let {
            Log.w(tag, msg, it)
        } ?: Log.w(tag, msg)
    }

    override fun e(tag: String?, msg: String, t: Throwable?) {
        if (!isDebug){
            return
        }
        t?.let {
            Log.e(tag, msg, it)
        } ?: Log.e(tag, msg)
    }

    override fun clear() {
        v(TAG, "clear ALog", null)
    }
}
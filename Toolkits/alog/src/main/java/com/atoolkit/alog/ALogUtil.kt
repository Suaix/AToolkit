package com.atoolkit.alog

import android.content.Context

/**
 * Description: 日志工具的入口类，单例模式，正式调用打印日志前必须先初始化
 * Author: summer
 * Date: 2023/3/17 14:33
 * LastModifyTime:
 */
object ALogUtil {
    lateinit var application: Context
    private lateinit var log: IALog
    private var isInited = false
    private var defaultTag = "ALogUtil"

    /**
     * Description: 初始化ALogUtil
     * Author: summer
     * date: 2023/3/17 14:49
     */
    fun init(context: Context, config: ALogConfig) {
        application = context.applicationContext
        log = config.log
        log.init(context, config)
        config.defaultTag?.let {
            this.defaultTag = it
        }
        isInited = true
    }

    fun v(tag: String? = defaultTag, msg: String, t: Throwable? = null) {
        check(isInited) {
            "you must init ALogUtil by call init(context, config) method before use it"
        }
        log.v(tag, msg, t)
    }

    fun d(tag: String? = defaultTag, msg: String, t: Throwable? = null) {
        check(isInited) {
            "you must init ALogUtil by call init(context, config) method before use it"
        }
        log.d(tag, msg, t)
    }

    fun i(tag: String? = defaultTag, msg: String, t: Throwable? = null) {
        check(isInited) {
            "you must init ALogUtil by call init(context, config) method before use it"
        }
        log.i(tag, msg, t)
    }

    fun w(tag: String? = defaultTag, msg: String, t: Throwable? = null) {
        check(isInited) {
            "you must init ALogUtil by call init(context, config) method before use it"
        }
        log.w(tag, msg, t)
    }

    fun e(tag: String? = defaultTag, msg: String, t: Throwable? = null) {
        check(isInited) {
            "you must init ALogUtil by call init(context, config) method before use it"
        }
        log.e(tag, msg, t)
    }
}
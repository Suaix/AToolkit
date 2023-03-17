package com.atoolkit.alog

import android.content.Context

/**
 * Log接口，抽象Log相关行为
 */
interface ILog {
    /**
     * 初始化Log，子类根据需要自己实现
     */
    fun init(context: Context, config: ALogConfig)

    /**
     * v级别的log
     */
    fun v(tag: String?, msg: String, t: Throwable?)

    /**
     * d级别的log
     */
    fun d(tag: String?, msg: String, t: Throwable?)

    /**
     * i级别的log
     */
    fun i(tag: String?, msg: String, t: Throwable?)

    /**
     * w级别的log
     */
    fun w(tag: String?, msg: String, t: Throwable?)

    /**
     * e级别的log
     */
    fun e(tag: String?, msg: String, t: Throwable?)

    /**
     * 销毁或释放相关资源，一般调用时机在Application销毁前
     */
    fun clear()
}
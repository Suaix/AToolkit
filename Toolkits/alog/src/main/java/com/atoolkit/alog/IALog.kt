package com.atoolkit.alog

import android.content.Context
import com.atoolkit.common.ILog

/**
 * Log接口，抽象Log相关行为
 */
interface IALog : ILog {
    /**
     * 初始化Log，子类根据需要自己实现
     */
    fun init(context: Context, config: ALogConfig)

    /**
     * 销毁或释放相关资源，一般调用时机在Application销毁前
     */
    fun clear()
}
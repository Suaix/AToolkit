package com.atoolkit.apermission

import android.app.Activity
import android.content.Context
import com.atoolkit.common.ILog
import java.lang.ref.WeakReference

internal var isInited = false
internal var application: Context? = null
internal var logger: ILog? = null
internal var callback: WeakReference<((List<String>, List<String>, List<String>) -> Unit)?> = null

/**
 * 初始化permission工具类
 */
fun init(context: Context, log: ILog? = null) {
    application = context.applicationContext
    logger = log
    isInited = true
}

fun requestPermissions(
    context: Activity,
    List<APermission>,
    callback: ((List<String>, List<String>, List<String>) -> Unit)? = null
) {
    check(isInited) {
        throw IllegalStateException("请先调用init(context, log)方法进行初始化")
    }
    // TODO: 组装数据，调起权限申请页面
}
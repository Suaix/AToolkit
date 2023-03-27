package com.atoolkit.autils

import android.content.Context
import com.atoolkit.common.ILog

internal lateinit var application: Context
internal var useCache = true
internal var aLog: ILog? = null
internal const val TAG = "AUtils"
/**
 * Description: AUtils提供工具类所需的基础信息，如ApplicationContext、Log等；
 * Author: summer
 * Date: 2023/3/27 19:22
 * LastModifyTime:
 */
@JvmOverloads
fun initAUtils(context: Context, log: ILog? = null, isUseCache: Boolean = true) {
    application = context.applicationContext
    aLog = log
    useCache = isUseCache
}
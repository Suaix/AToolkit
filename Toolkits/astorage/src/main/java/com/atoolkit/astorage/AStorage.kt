package com.atoolkit.astorage

import android.content.Context
import com.atoolkit.common.ILog

internal lateinit var application: Context
internal var aLog: ILog? = null
internal const val TAG = "AStorage"

fun initAStorage(context: Context, log: ILog? = null) {
    application = context.applicationContext
    aLog = log
}
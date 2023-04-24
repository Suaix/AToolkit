package com.atoolkit.aqrcode

import android.content.Context
import com.atoolkit.common.ILog

internal const val TAG = "AQRCode"
internal lateinit var application: Context
internal var aLog: ILog? = null

fun initAQrCode(context: Context, config: AQRConfig? = null) {
    application = context.applicationContext
    aLog = config?.log
}
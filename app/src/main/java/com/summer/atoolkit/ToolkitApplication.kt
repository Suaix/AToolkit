package com.summer.atoolkit

import android.app.Application
import com.atoolkit.alog.ALogConfig
import com.atoolkit.alog.ALogUtil
import com.atoolkit.alog.LOG_LEVEL_D
import com.atoolkit.alog.write.AWritableLog
import com.atoolkit.alog.write.AWritableLogConfig
import com.atoolkit.apermission.initAPermission
import com.atoolkit.aqrcode.AQRConfig
import com.atoolkit.aqrcode.initAQrCode
import com.atoolkit.astorage.AStorageConfig
import com.atoolkit.astorage.initAStorage
import com.atoolkit.autils.initAUtils
import java.io.File


/**
 * Author:summer
 * Time: 2023/3/17 17:37
 * Description: ToolkitApplication是App的入口Application实现
 */
class ToolkitApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initToolkit()
    }

    private fun initToolkit() {
        val awConfig = AWritableLogConfig(saveLevel = LOG_LEVEL_D, logPath = "${this.filesDir}${File.separator}ToolKit")
        val log = AWritableLog()
        val config = ALogConfig(BuildConfig.DEBUG, log, extra = awConfig)
        ALogUtil.init(this, config)

        initAPermission(this, log)
        initAUtils(this, log, false)
        val storageConfig = AStorageConfig(log = log)
        initAStorage(this, storageConfig)
        val qrConfig = AQRConfig(log = log)
        initAQrCode(this, qrConfig)
    }

}
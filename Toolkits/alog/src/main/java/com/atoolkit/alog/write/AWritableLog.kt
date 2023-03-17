package com.atoolkit.alog.write

import android.content.Context
import android.util.Log
import com.atoolkit.alog.ALogConfig
import com.atoolkit.alog.ILog
import com.atoolkit.alog.LOG_DIR_NAME
import com.atoolkit.alog.LOG_LEVEL_D
import com.atoolkit.alog.LOG_LEVEL_E
import com.atoolkit.alog.LOG_LEVEL_I
import com.atoolkit.alog.LOG_LEVEL_V
import com.atoolkit.alog.LOG_LEVEL_W
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale


/**
 * Description: 可写入文件的日志实现，这里是简单实现，将日志写入到内部私有存储，默认路径是/data/data/[packageName]/files/ALog。
 * 如需写入到需要存储权限的地方，需要在调用者地方声明对应权限，并进行权限检查及申请，内部不进行权限检查。
 * Author: summer
 * Date: 2023/3/17 15:25
 * LastModifyTime:
 */
class AWritableLog : ILog {
    private var isDebug = false
    private var saveLogLevel = LOG_LEVEL_W
    private val logWriterUseCase = ALogWriterUseCase()
    private val dateFormat = SimpleDateFormat("MM-dd HH:mm:ss", Locale.CHINA)
    private val logStyle = "%s tag:%s %s %s"

    override fun init(context: Context, config: ALogConfig) {
        isDebug = config.isDebug
        val writableConfig = if (config.extra is AWritableLogConfig) {
            config.extra
        } else {
            val path = "${context.filesDir}${File.separator}$LOG_DIR_NAME"
            AWritableLogConfig(logPath = path)
        }
        saveLogLevel = writableConfig.saveLevel
        logWriterUseCase.init(writableConfig)
    }

    override fun v(tag: String?, msg: String, t: Throwable?) {
        if (isDebug) {
            t?.let {
                Log.v(tag, msg, it)
            } ?: Log.v(tag, msg)
        }
        if (saveLogLevel <= LOG_LEVEL_V) {
            logWriterUseCase.appendLog(formatLog(tag, msg, t))
        }
    }

    override fun d(tag: String?, msg: String, t: Throwable?) {
        if (isDebug) {
            t?.let {
                Log.d(tag, msg, it)
            } ?: Log.d(tag, msg)
        }
        if (saveLogLevel <= LOG_LEVEL_D) {
            logWriterUseCase.appendLog(formatLog(tag, msg, t))
        }
    }

    override fun i(tag: String?, msg: String, t: Throwable?) {
        if (isDebug) {
            t?.let {
                Log.i(tag, msg, it)
            } ?: Log.i(tag, msg)
        }
        if (saveLogLevel <= LOG_LEVEL_I) {
            logWriterUseCase.appendLog(formatLog(tag, msg, t))
        }
    }

    override fun w(tag: String?, msg: String, t: Throwable?) {
        if (isDebug) {
            t?.let {
                Log.w(tag, msg, it)
            } ?: Log.w(tag, msg)
        }
        if (saveLogLevel <= LOG_LEVEL_W) {
            logWriterUseCase.appendLog(formatLog(tag, msg, t))
        }
    }

    override fun e(tag: String?, msg: String, t: Throwable?) {
        if (isDebug) {
            t?.let {
                Log.e(tag, msg, it)
            } ?: Log.e(tag, msg)
        }
        if (saveLogLevel <= LOG_LEVEL_E) {
            logWriterUseCase.appendLog(formatLog(tag, msg, t))
        }
    }

    override fun clear() {
        logWriterUseCase.flush()
    }

    /**
     * Description: 格式化日志，按照以下规则格式化：[time]:tag:[tag] [msg] [t]
     * Author: summer
     */
    private fun formatLog(tag: String?, msg: String, t: Throwable?): String {
        val traceString = Log.getStackTraceString(t)
        val dayTime = dateFormat.format(System.currentTimeMillis())
        return String.format(logStyle, dayTime, tag, msg, traceString)
    }
}
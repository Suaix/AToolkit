package com.atoolkit.alog.write

import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executors


/**
 * Description: 用来将日志写入到文件里的用例
 * Author: summer
 * Date: 2023/3/17 16:31
 * LastModifyTime:
 */
class ALogWriterUseCase {
    private var buffer: StringBuffer = StringBuffer()
    private lateinit var logFile: File
    private var logCount = 0
    private val executor = Executors.newSingleThreadExecutor()

    /**
     * Description: 初始化用例，初始化log文件，线程
     * Author: summer
     * date: 2023/3/17 16:36
     */
    fun init(config: AWritableLogConfig) {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
        val fileName = "${format.format(Date())}.log"
        val parentDir = File(config.logPath)
        if (!parentDir.exists()) {
            parentDir.mkdirs()
        }
        logFile = File(parentDir, fileName)
        if (!logFile.exists()){
            logFile.createNewFile()
        }
    }

    /**
     * Description: 添加日志
     * Author: summer
     */
    fun appendLog(log: String) {
        buffer.append(log)
        buffer.append("\n")
        logCount++
        if (logCount >= 10) {
            flush()
        }
    }

    /**
     * Description: 刷新剩余的日志到文件中
     * Author: summer
     */
    fun flush() {
        val temp = buffer
        buffer = StringBuffer()
        logCount = 0
        appendLogToFile(temp)
    }

    /**
     * Description: 将缓存日志写入到日志文件中去
     * Author: summer
     */
    private fun appendLogToFile(logs: StringBuffer) {
        val content = "$logs\n"
        executor.submit {
            var raf: RandomAccessFile? = null
            try {
                raf = RandomAccessFile(logFile, "rw")
                raf.seek(logFile.length())
                raf.write(content.toByteArray(Charsets.UTF_8))
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    raf?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
}
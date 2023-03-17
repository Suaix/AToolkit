package com.atoolkit.alog.write

import com.atoolkit.alog.LOG_LEVEL_W

data class AWritableLogConfig(
    val saveLevel: Int = LOG_LEVEL_W,
    val logPath: String
)

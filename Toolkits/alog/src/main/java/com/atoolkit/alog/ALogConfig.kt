package com.atoolkit.alog

const val LOG_LEVEL_V = 0
const val LOG_LEVEL_D = 1
const val LOG_LEVEL_I = 2
const val LOG_LEVEL_W = 3
const val LOG_LEVEL_E = 4
internal const val LOG_DIR_NAME = "ALogs"

data class ALogConfig(
    val isDebug: Boolean, // 是否是debug
    val log: ILog = ALog(), // 具体的日志打印器
    val defaultTag: String? = null, // 默认tag
    val extra: Any? = null, // 额外信息，可在自定义ILog时可以根据需要传入自己需要的数据
)
